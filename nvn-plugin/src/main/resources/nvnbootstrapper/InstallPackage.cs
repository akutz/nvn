namespace NvnBootstrapper
{
    internal class InstallPackage
    {
        public string Name { get; set; }
        
        public string FileName { get; set; }

        public string Extension { get; set; }

        public string[] ResourceKeys { get; set; }

        public string InstallArgs { get; set; }

        public string UninstallArgs { get; set; }

        public string QuietInstallArgs { get; set; }

        public string QuietUninstallArgs { get; set; }

        public bool SupportsUninstall { get; set; }

        public string FilePath { get; set; }

        public int[] ExitCodes { get; set; }

        public RegValue[] RegValues { get; set; }

        public RegKey[] RegKeys { get; set; }

        public RunningProcess[] RunningProcesses { get; set; }

        public string Prompt { get; set; }
    }
}
