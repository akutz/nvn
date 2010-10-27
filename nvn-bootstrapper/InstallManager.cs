namespace NvnBootstrapper
{
    using System;
    using System.Collections.Generic;
    using System.Diagnostics;
    using System.IO;
    using System.Resources;
    using System.Text;
    using System.Text.RegularExpressions;
    using System.Threading;
    using System.Windows.Forms;
    using Microsoft.Win32;

    internal static class InstallManager
    {
        #region Delegates

        public delegate void InstallMessageEvent(string message);

        public delegate void InstallProgressEvent();

        public delegate void InstallProgressInitEvent(int total);

        #endregion

        private static readonly string TempPath = Path.GetTempPath();

        /// <summary>
        /// The resource manager for this assembly.
        /// </summary>
        public static readonly ResourceManager ResourceManager =
            new ResourceManager(
                typeof (InstallManager).Namespace + @".Properties.Resources",
                typeof (InstallManager).Assembly);

        /// <summary>
        /// This value is set to true when the installation of the embedded
        /// packages is occurring and set to false otherwise.
        /// </summary>
        private static bool installing;

        /// <summary>
        /// Gets or sets the install form.
        /// </summary>
        public static Form InstallForm { get; set; }

        /// <summary>
        /// Gets or sets the install error message.
        /// </summary>
        public static string InstallError { get; set; }

        public static void ProcessGlobalChecks()
        {
            ProcessRegKeys();

            if (InstallError != null)
            {
                return;
            }

            ProcessRegValues();

            if (InstallError != null)
            {
                return;
            }

            ProcessRunningProcess();

            if (InstallError == null)
            {
                ProcessProductRemovals();
            }
        }

        /// <summary>
        /// Processes the global registry value list.
        /// </summary>
        public static void ProcessRegValues()
        {
            foreach (var rv in InstallResources.RegValues)
            {
                if (!ProcessRegValue(rv))
                {
                    InstallError = rv.ErrorMessage;
                    break;
                }
            }
        }

        /// <summary>
        /// Processes the global registry keys list.
        /// </summary>
        public static void ProcessRegKeys()
        {
            foreach (var rk in InstallResources.RegKeys)
            {
                if (!ProcessRegKey(rk))
                {
                    InstallError = rk.ErrorMessage;
                    break;
                }
            }
        }

        /// <summary>
        /// Processes the global running processes list.
        /// </summary>
        public static void ProcessRunningProcess()
        {
            foreach (var rp in InstallResources.RunningProcesses)
            {
                if (!ProcessRunningProcess(rp))
                {
                    InstallError = rp.ErrorMessage;
                    break;
                }
            }
        }

        /// <summary>
        /// Processes a registry key.
        /// </summary>
        /// <param name="rk">A registry key.</param>
        /// <returns>
        /// True if the given registry key is found. The Inverse
        /// property may be used to invert the returned value.
        /// </returns>
        private static bool ProcessRegKey(RegKey rk)
        {
            var p = rk.Path;
            RegistryKey frk = null;

            if (p.StartsWith(@"HKCR"))
            {
                p = p.Replace(@"HKCR\", string.Empty);
                frk = Registry.ClassesRoot.OpenSubKey(p);
            }
            else if (p.StartsWith(@"HKCU"))
            {
                p = p.Replace(@"HKCU\", string.Empty);
                frk = Registry.CurrentUser.OpenSubKey(p);
            }
            else if (p.StartsWith(@"HKLM"))
            {
                p = p.Replace(@"HKLM\", string.Empty);
                frk = Registry.LocalMachine.OpenSubKey(p);
            }
            else if (p.StartsWith(@"HKU"))
            {
                p = p.Replace(@"HKU\", string.Empty);
                frk = Registry.Users.OpenSubKey(p);
            }
            else if (p.StartsWith(@"HKCC"))
            {
                p = p.Replace(@"HKCC\", string.Empty);
                frk = Registry.CurrentConfig.OpenSubKey(p);
            }
            else if (p.StartsWith(@"HKPD"))
            {
                p = p.Replace(@"HKPD\", string.Empty);
                frk = Registry.PerformanceData.OpenSubKey(p);
            }

            return rk.Inverse ? frk == null : frk != null;
        }

        /// <summary>
        /// Processes a registry value.
        /// </summary>
        /// <param name="rv">A registry value.</param>
        /// <returns>
        /// True if the value is found and meets the specified 
        /// condition. The Inverse property may be used to invert
        /// the returned value.
        /// </returns>
        private static bool ProcessRegValue(RegValue rv)
        {
            var er = EvalRegValue(rv);
            return rv.Inverse ? !er : er;
        }

        /// <summary>
        /// Evaluates a registry value.
        /// </summary>
        /// <param name="rv">A registry value.</param>
        /// <returns>
        /// True if the value is found and meets the specified 
        /// condition.
        /// </returns>
        private static bool EvalRegValue(RegValue rv)
        {
            if (rv == null)
            {
                return false;
            }

            var kp = ExpandRegRootName(rv.KeyPath);

            var ov = Registry.GetValue(kp, rv.ValueName, null);

            if (ov == null)
            {
                return false;
            }

            var tn = rv.TypeName;
            var rvv = rv.Value;

            if (tn == "string")
            {
                if (rv.Comparison == "==")
                {
                    return rvv == (string) ov;
                }

                if (rv.Comparison == "!=")
                {
                    return rvv != (string) ov;
                }

                return false;
            }

            if (tn == "match")
            {
                var rx = new Regex(rvv);
                return rx.IsMatch((string) ov);
            }

            if (tn == "version")
            {
                var rx = new Regex(@"\d+(?:\.\d+){0,2}");
                var rvVer = new Version(rx.Match(rvv).Groups[0].Value);
                var vVer = new Version(rx.Match((string) ov).Groups[0].Value);

                switch (rv.Comparison)
                {
                    case @"==":
                    {
                        return rvVer == vVer;
                    }
                    case @"!=":
                    {
                        return rvVer != vVer;
                    }
                    case @">":
                    {
                        return rvVer > vVer;
                    }
                    case @"<":
                    {
                        return rvVer < vVer;
                    }
                    case @">=":
                    {
                        return rvVer >= vVer;
                    }
                    case @"<=":
                    {
                        return rvVer <= vVer;
                    }
                    default:
                    {
                        return false;
                    }
                }
            }

            if (tn == "long")
            {
                var vLong = long.Parse(string.Format("{0}", ov));
                var rvLong = long.Parse(rvv);

                switch (rv.Comparison)
                {
                    case @"==":
                    {
                        return rvLong == vLong;
                    }
                    case @"!=":
                    {
                        return rvLong != vLong;
                    }
                    case @">":
                    {
                        return rvLong > vLong;
                    }
                    case @"<":
                    {
                        return rvLong < vLong;
                    }
                    case @">=":
                    {
                        return rvLong >= vLong;
                    }
                    case @"<=":
                    {
                        return rvLong <= vLong;
                    }
                    default:
                    {
                        return false;
                    }
                }
            }

            return false;
        }

        /// <summary>
        /// Process a running process.
        /// </summary>
        /// <param name="rp">The running process.</param>
        /// <returns>
        /// True if a process running on the system matches the 
        /// searched process name; othrwise false. The Inverse
        /// property may be used to invert the returned value.
        /// </returns>
        private static bool ProcessRunningProcess(RunningProcess rp)
        {
            var matchedProcs = new List<Process>();

            var procList = Process.GetProcesses();

            foreach (var p in procList)
            {
                if (Path.GetFileName(p.ProcessName) == rp.Name)
                {
                    matchedProcs.Add(p);
                }
            }

            var found = matchedProcs.Count > 0;

            return rp.Inverse ? !found : found;
        }

        public static void ProcessProductRemovals()
        {
            if (InstallResources.ProductRemovers.Length == 0)
            {
                return;
            }

            foreach (var pr in InstallResources.ProductRemovers)
            {
                if (!pr.ProductCode.StartsWith(@"{{"))
                {
                    pr.ProductCode = string.Format(@"{{{0}}}", pr.ProductCode);
                }
            }

            foreach (var prod in
                new MsiEnumWrapper<Product>(default(Product), EnumProducts))
            {
                var toRemove = new List<ProductRemover>();

                foreach (var pr in InstallResources.ProductRemovers)
                {
                    if (pr.ProductCode == prod.ProductCode)
                    {
                        toRemove.Add(pr);
                    }
                }

                foreach (var tr in toRemove)
                {
                    var dr = MessageBox.Show(
                        tr.Message,
                        InstallResources.ProductName,
                        MessageBoxButtons.YesNo);

                    if (dr == DialogResult.No)
                    {
                        InstallError =
                            string.Format(
                                @"You have elected to not uninstall {0} at this time",
                                tr.Name);
                        break;
                    }

                    const string argsPatt =
                        @"/uninstall ""{0}"" /passive /norestart /l ""{1}\{2}.uninstall.log""";

                    var args = string.Format(
                        argsPatt, prod.LocalPackage, TempPath, tr.Name);

                    var p = new Process
                    {
                        StartInfo =
                            new ProcessStartInfo
                            {
                                FileName = @"msiexec",
                                Arguments = args,
                                UseShellExecute = false,
                                CreateNoWindow = true,
                            }
                    };

                    p.Start();

                    p.WaitForExit();

                    var xit = p.ExitCode;

                    if (xit != 0)
                    {
                        InstallError =
                            string.Format(
                                @"An error occurred while attempting to uninstall {0}",
                                tr.Name);
                    }
                }

                if (InstallError != null)
                {
                    break;
                }
            }
        }

        private static int EnumProducts(int index, ref Product data)
        {
            var buffer = new StringBuilder(Msi.GuidLength);
            var ret = Msi.MsiEnumProducts(index, buffer);
            data.ProductCode = buffer.ToString();
            return ret;
        }

        public static void OnAllPurposeClick(Page sender)
        {
            if (!installing && sender is InstallPage)
            {
                var p = sender.Parent;
                p.Controls.Clear();
                p.Controls.Add(new EndPage());
                p.Controls[0].Focus();
            }
            else if (!installing)
            {
                Application.Exit();
            }
        }

        public static void Install()
        {
            ThreadPool.QueueUserWorkItem(
                state =>
                {
                    installing = true;
                    SyncInstall();
                    installing = false;
                });
        }

        private static void SyncInstall()
        {
            var total = InstallResources.InstallPackages.Length*2;
            InitInstallProgress(total);

            var installedPkgs = new List<InstallPackage>();

            foreach (var ip in InstallResources.InstallPackages)
            {
                if (!ValidateInstallPackage(ip))
                {
                    total -= 2;
                    InitInstallProgress(total);
                    continue;
                }

                // Prompt the user whether or not to install
                // the package if a prompt value is included.
                // The prompt's answer should always be in
                // the affirmative to install the package.
                if (!string.IsNullOrEmpty(ip.Prompt))
                {
                    var doTheInstall = true;

                    InstallForm.Invoke(
                        new MethodInvoker(
                            () =>
                            {
                                if (MessageBox.Show(
                                    InstallForm,
                                    // ReSharper disable AccessToModifiedClosure
                                    ip.Prompt,
                                    // ReSharper restore AccessToModifiedClosure
                                    InstallResources.ProductName,
                                    MessageBoxButtons.YesNo) == DialogResult.No)
                                {
                                    doTheInstall = false;
                                }
                            }));

                    if (!doTheInstall)
                    {
                        total -= 2;
                        InitInstallProgress(total);
                        continue;
                    }
                }

                ip.FilePath = string.Format(
                    @"{0}\{1}.{2}", TempPath, ip.Name, ip.Extension);
                ExtractInstallPackage(ip);
                InstallInstallPackage(ip);

                if (InstallError != null)
                {
                    DecrementInstallProgress();
                    DecrementInstallProgress();
                    break;
                }

                installedPkgs.Add(ip);
            }

            if (InstallError == null)
            {
                if (installedPkgs.Count == 0)
                {
                    InitInstallProgress(1);
                    IncrementInstallProgress();
                }

                SendInstallMessage(
                    @"Installation completed.");
            }
            else
            {
                SendInstallMessage(
                    @"An error has occurred. Rolling back installation.");

                foreach (var ip in installedPkgs)
                {
                    if (ip.SupportsUninstall)
                    {
                        UninstallInstallPackage(ip);
                    }
                    else
                    {
                        DecrementInstallProgress();
                    }

                    DecrementInstallProgress();
                }

                SendInstallMessage(
                    @"An error has occurred. Rolled back installation.");
            }

            EndInstall();
        }

        private static bool ValidateInstallPackage(InstallPackage ip)
        {
            foreach (var x in ip.RegKeys)
            {
                if (!ProcessRegKey(x))
                {
                    return false;
                }
            }

            foreach (var x in ip.RegValues)
            {
                if (!ProcessRegValue(x))
                {
                    return false;
                }
            }

            foreach (var x in ip.RunningProcesses)
            {
                if (!ProcessRunningProcess(x))
                {
                    return false;
                }
            }

            return true;
        }

        private static void ExtractInstallPackage(InstallPackage ip)
        {
            SendInstallMessage(@"Extracting " + ip.Name);

            var fs = new FileStream(ip.FilePath, FileMode.Create);

            foreach (var rk in ip.ResourceKeys)
            {
                var fd = (byte[]) ResourceManager.GetObject(rk);
                // ReSharper disable PossibleNullReferenceException
                fs.Write(fd, 0, fd.Length);
                // ReSharper restore PossibleNullReferenceException
            }

            fs.Close();

            IncrementInstallProgress();
        }

        private static void InstallInstallPackage(InstallPackage ip)
        {
            SendInstallMessage(@"Installing " + ip.Name);

            switch (ip.Extension)
            {
                case @"msi":
                {
                    InstallMsiPackage(ip);
                    break;
                }
                case @"exe":
                {
                    InstallExePackage(ip);
                    break;
                }
            }

            IncrementInstallProgress();
        }

        private static void UninstallInstallPackage(InstallPackage ip)
        {
            SendInstallMessage(@"Unnstalling " + ip.Name);

            switch (ip.Extension)
            {
                case @"msi":
                {
                    UninstallMsiPackage(ip);
                    break;
                }
                case @"exe":
                {
                    UninstallExePackage(ip);
                    break;
                }
            }

            DecrementInstallProgress();
        }

        private static void InstallMsiPackage(InstallPackage ip)
        {
            string argsPatt;

            if (!string.IsNullOrEmpty(ip.InstallArgs))
            {
                argsPatt = ip.InstallArgs;
            }
            else if (!string.IsNullOrEmpty(ip.QuietInstallArgs))
            {
                argsPatt = ip.QuietInstallArgs;
            }
            else
            {
                argsPatt = @"/i ""{0}"" /qn /norestart /l ""{1}""";
            }

            var args = string.Format(
                argsPatt, ip.FilePath, ip.FilePath + ".install.log");

            // Launch the target installer.
            var p = new Process
            {
                StartInfo =
                    new ProcessStartInfo
                    {
                        FileName = @"msiexec",
                        Arguments = args,
                        UseShellExecute = false,
                        CreateNoWindow = true,
                    }
            };

            p.Start();

            p.WaitForExit();

            var xit = p.ExitCode;

            if (xit != ip.SuccessfulExitCode)
            {
                InstallError =
                    string.Format(
                        @"An error occurred while installing {0}. An exit code of '{1}' was returned.",
                        ip.Name,
                        xit);
            }
        }

        private static void InstallExePackage(InstallPackage ip)
        {
            var argsPatt = !string.IsNullOrEmpty(ip.InstallArgs)
                ? ip.InstallArgs
                : ip.QuietInstallArgs;

            var args = string.Format(argsPatt, ip.FilePath + ".install.log");

            // Launch the target installer.
            var p = new Process
            {
                StartInfo =
                    new ProcessStartInfo
                    {
                        FileName = ip.FilePath,
                        Arguments = args,
                        UseShellExecute = false,
                        CreateNoWindow = true,
                    }
            };

            p.Start();

            p.WaitForExit();

            var xit = p.ExitCode;

            if (xit != ip.SuccessfulExitCode)
            {
                InstallError =
                    string.Format(
                        @"An error occurred while installing {0}. An exit code of '{1}' was returned.",
                        ip.Name,
                        xit);
            }
        }

        private static void UninstallMsiPackage(InstallPackage ip)
        {
            string argsPatt;

            if (!string.IsNullOrEmpty(ip.InstallArgs))
            {
                argsPatt = ip.UninstallArgs;
            }
            else if (!string.IsNullOrEmpty(ip.QuietInstallArgs))
            {
                argsPatt = ip.QuietUninstallArgs;
            }
            else
            {
                argsPatt = @"/uninstall ""{0}"" /qn /norestart /l ""{1}""";
            }

            var args = string.Format(
                argsPatt, ip.FilePath, ip.FilePath + ".uninstall.log");

            var p = new Process
            {
                StartInfo =
                    new ProcessStartInfo
                    {
                        FileName = @"msiexec",
                        Arguments = args,
                        UseShellExecute = false,
                        CreateNoWindow = true,
                    }
            };

            p.Start();

            p.WaitForExit();
        }

        private static void UninstallExePackage(InstallPackage ip)
        {
            var argsPatt = !string.IsNullOrEmpty(ip.InstallArgs)
                ? ip.UninstallArgs
                : ip.QuietUninstallArgs;

            var args = string.Format(argsPatt, ip.FilePath + ".uninstall.log");

            var p = new Process
            {
                StartInfo =
                    new ProcessStartInfo
                    {
                        FileName = ip.FilePath,
                        Arguments = args,
                        UseShellExecute = false,
                        CreateNoWindow = true,
                    }
            };

            p.Start();

            p.WaitForExit();
        }

        private static string ExpandRegRootName(string path)
        {
            if (path.StartsWith(@"HKCR\"))
            {
                return path.Replace(@"HKCR\", @"HKEY_CLASSES_ROOT\");
            }

            if (path.StartsWith(@"HKCU\"))
            {
                return path.Replace(@"HKCU\", @"HKEY_CURRENT_USER\");
            }

            if (path.StartsWith(@"HKLM\"))
            {
                return path.Replace(@"HKLM\", @"HKEY_LOCAL_MACHINE\");
            }

            if (path.StartsWith(@"HKU\"))
            {
                return path.Replace(@"HKU\", @"HKEY_USERS\");
            }

            if (path.StartsWith(@"HKCC\"))
            {
                return path.Replace(@"HKCC\", @"HKEY_CURRENT_CONFIG\");
            }

            if (path.StartsWith(@"HKPD\"))
            {
                return path.Replace(@"HKPD\", @"HKEY_PERFORMANCE_DATA\");
            }

            return path;
        }

        private static void InitInstallProgress(int total)
        {
            if (InstallProgressInit == null)
            {
                return;
            }

            InstallProgressInit(total);
        }

        private static void SendInstallMessage(string message)
        {
            if (InstallMessage == null)
            {
                return;
            }

            InstallMessage(message);
        }

        private static void IncrementInstallProgress()
        {
            if (InstallProgressIncrement == null)
            {
                return;
            }

            InstallProgressIncrement();
        }

        private static void DecrementInstallProgress()
        {
            if (InstallProgressDecrement == null)
            {
                return;
            }

            InstallProgressDecrement();
        }

        private static void EndInstall()
        {
            if (InstallEnd == null)
            {
                return;
            }

            InstallEnd();
        }

        public static event InstallProgressInitEvent InstallProgressInit;
        public static event InstallProgressEvent InstallProgressIncrement;
        public static event InstallProgressEvent InstallProgressDecrement;
        public static event InstallMessageEvent InstallMessage;
        public static event InstallProgressEvent InstallEnd;
    }
}
