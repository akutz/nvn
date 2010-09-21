using System.Reflection;
using System.Runtime.InteropServices;

[assembly: AssemblyTitle(@"${AssemblyName}")]
[assembly: AssemblyDescription("The installation bootstrapper for ${AssemblyName}")]
[assembly: AssemblyCompany(@"${OrganizationName}")]
[assembly: AssemblyProduct("${AssemblyName}")]
[assembly: AssemblyCopyright(@"Copyright © ${OrganizationName} ${Year}")]
[assembly: ComVisible(false)]
[assembly: Guid(@"${AssemblyGuid}")]
[assembly: AssemblyVersion(@"${StandardVersion}")]
[assembly: AssemblyFileVersion(@"${StandardVersion}")]
[assembly: AssemblyInformationalVersion(@"${Version}")]

namespace MsiBootstrapper
{
    using System;
    using System.Collections.Generic;
    using System.Diagnostics;
    using System.Drawing;
    using System.IO;
    using System.Resources;
    using System.Threading;
    using System.Windows.Forms;

    internal static class Program
    {
        /// <summary>
        /// The resource manager for this assembly.
        /// </summary>
        public static readonly ResourceManager ResourceManager =
            new ResourceManager(
                typeof (Program).Namespace + @".Resources",
                typeof (Program).Assembly);

        public static int? ExitCode;

        /// <summary>
        /// Gets the target's data.
        /// </summary>
        private static byte[] TargetData
        {
            get { return (byte[]) ResourceManager.GetObject(@"TargetData"); }
        }

        /// <summary>
        /// Gets the target's name.
        /// </summary>
        private static string TargetName
        {
            get { return ResourceManager.GetString(@"TargetName"); }
        }

        [STAThread]
        private static void Main()
        {
            Application.ApplicationExit += OnApplicationExit;
            Application.EnableVisualStyles();
            Application.Run(new ProgressForm());
        }

        private static void OnApplicationExit(object sender, EventArgs e)
        {
            // If the exit code has not been set or if it has and it is not
            // 0 then do not install the target.
            if (!ExitCode.HasValue || ExitCode != 0)
            {
                return;
            }

            StartProcess(TargetName, TargetData, false);
        }

        public static Process StartProcess(string name, byte[] data, bool quiet)
        {
            // Write the data to a temp file.
            var tf = string.Format(@"{0}\{1}.msi", Path.GetTempPath(), name);
            File.WriteAllBytes(tf, data);

            // Get the name of the log file.
            var lf = string.Format(@"{0}\{1}.msi.log", Path.GetTempPath(), name);

            var args = string.Format(
                @"/i ""{0}"" {1} /log ""{2}""",
                tf,
                quiet ? @"/qn" : string.Empty,
                lf);

            // Launch the target installer.
            var p = new Process
            {
                StartInfo =
                    new ProcessStartInfo
                    {
                        FileName = @"msiexec",
                        Arguments = args,
                        UseShellExecute = false,
                        CreateNoWindow = quiet,
                    }
            };

            // Clean up after we're done.
            p.Exited += (s, e) => File.Delete(tf);

            p.Start();

            return p;
        }
    }

    internal class ProgressForm : Form
    {
        private readonly Label lblProgress;
        private readonly ProgressBar progressBar;
        private List<byte[]> preReqDatas;
        private List<string> preReqNames;

        /// <summary>
        /// Instantiates a new instance of the ProgressForm class.
        /// </summary>
        public ProgressForm()
        {
            Size = new Size(452, 98);
            ControlBox = false;
            MaximizeBox = false;
            SizeGripStyle = SizeGripStyle.Hide;
            StartPosition = FormStartPosition.CenterScreen;
            Text = @"Installing Prerequisites";
            ShowInTaskbar = false;

            this.lblProgress = new Label
            {
                Text = @"Installing $X of $Y: $NAME",
                Size = new Size(419, 13),
                Location = new Point(13, 13)
            };

            this.progressBar = new ProgressBar
            {
                Style = ProgressBarStyle.Marquee,
                Size = new Size(420, 23),
                Location = new Point(12, 29),
            };

            Controls.Add(this.lblProgress);
            Controls.Add(this.progressBar);

            Load += OnLoad;
        }

        /// <summary>
        /// Gets a list of the pre-requisite names.
        /// </summary>
        private List<string> PreReqNames
        {
            get
            {
                if (this.preReqNames != null)
                {
                    return this.preReqNames;
                }

                this.preReqNames = new List<string>();

                var stop = false;

                for (var x = 0; x < 99 && !stop; ++x)
                {
                    var key = string.Format(@"PreReq{0:00}Name", x);
                    var name = Program.ResourceManager.GetString(key);

                    if (string.IsNullOrEmpty(name))
                    {
                        stop = true;
                    }
                    else
                    {
                        this.preReqNames.Add(name);
                    }
                }

                return this.preReqNames;
            }
        }

        /// <summary>
        /// Gets a list of the pre-requisite data objects.
        /// </summary>
        private List<byte[]> PreReqDatas
        {
            get
            {
                if (this.preReqDatas != null)
                {
                    return this.preReqDatas;
                }

                this.preReqDatas = new List<byte[]>();

                var stop = false;

                for (var x = 0; x < 99 && !stop; ++x)
                {
                    var key = string.Format(@"PreReq{0:00}Data", x);
                    var data = (byte[]) Program.ResourceManager.GetObject(key);

                    if (data == null)
                    {
                        stop = true;
                    }
                    else
                    {
                        this.preReqDatas.Add(data);
                    }
                }

                return this.preReqDatas;
            }
        }

        private void OnLoad(object sender, EventArgs e)
        {
            for (var x = 0; x < PreReqNames.Count; ++x)
            {
                if (Program.ExitCode.HasValue && Program.ExitCode != 0)
                {
                    break;
                }

                this.lblProgress.Text =
                    string.Format(
                        @"Installing {0} of {1}: {2}",
                        x + 1,
                        PreReqNames.Count,
                        PreReqNames[x]);

                var p = Program.StartProcess(
                    PreReqNames[x], PreReqDatas[x], true);

                // While the pre-req installer has not exited keep pausing
                // the primary thread.
                while (!p.HasExited)
                {
                    Thread.Sleep(50);
                }

                var ec = p.ExitCode;

                // An exit code of 0 means the product was installed successfully.
                // An exit code of 1603 means the product is already installed.
                if (ec != 0 && ec != 1603)
                {
                    MessageBox.Show(
                        @"Error installing prerequisite. Installer will now exit.",
                        @"Install Error",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error);
                    Program.ExitCode = ec;
                }
                else
                {
                    Program.ExitCode = 0;
                }
            }

            Application.Exit();
        }
    }
}
