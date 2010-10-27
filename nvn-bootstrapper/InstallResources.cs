namespace NvnBootstrapper
{
    internal static class InstallResources
    {
        public static string ProductName =
            @"EMC VSI for vSphere4 Feature - HelloWorld";

        public static string EndPageUrlLink = @"www.emc.com";
        public static string EndPageUrlText = @"http://www.emc.com/";

        public static RegValue[] RegValues = new RegValue[0];

        public static ProductRemover[] ProductRemovers = new[]
        {
            new ProductRemover
            {
                Name = @"VSI 3.0.1",
                ProductCode = @"EEF00B26-44BE-4A44-9AFD-C4463F50FEDC",
                Message =
                    @"Installing this product will remove VSI 3.0.1. You need to download and install additional features to completely substitute VSI 3.0.1 functionality. Do you wish to proceed?",
            },
        };

        public static RegKey[] RegKeys = new[]
        {
            new RegKey
            {
                Path =
                    @"HKLM\SOFTWARE\VMware, Inc.\VMware Virtual Infrastructure Client",
                ErrorMessage = @"The VMware vSphere4 client was not found.",
            },
        };

        public static RunningProcess[] RunningProcesses = new[]
        {
            new RunningProcess
            {
                Name = @"VpxClient",
                ErrorMessage =
                    @"The VMware vSphere4 client must be closed for the installation to proceed.",
                Inverse = true,
            },
        };

        public static InstallPackage[] InstallPackages = new[]
        {
            new InstallPackage
            {
                ResourceKeys = new[] {@"InstallPackage00_00"},
                Name = @"Microsoft .NET 3.5 SP1",
                Extension = @"exe",
                SupportsUninstall = false,
                QuietInstallArgs = @"/q /norestart",
                RegKeys = new RegKey[0],
                RunningProcesses = new RunningProcess[0],
                RegValues = new[]
                {
                    new RegValue
                    {
                        KeyPath =
                            @"HKLM\SOFTWARE\Microsoft\NET Framework Setup\NDP\v3.5",
                        ValueName = @"SP",
                        Value = "1",
                        TypeName = "long",
                        Comparison = "<=",
                        Inverse = true,
                    },
                },
            },
            
            new InstallPackage
            {
                ResourceKeys = new[] {@"InstallPackage01_00"},
                Name = @"EMC Virtual Storage Integrator (VSI) for vSphere4",
                Extension = @"msi",
                SupportsUninstall = true,
                RegKeys = new RegKey[0],
                RunningProcesses = new RunningProcess[0],
                RegValues = new RegValue[0],
            },
            
            new InstallPackage
            {
                ResourceKeys = new[] {@"InstallPackage02_00"},
                Name = @"EMC VSI for vSphere4 Feature - HelloWorld",
                Extension = @"msi",
                SupportsUninstall = true,
                RegKeys = new RegKey[0],
                RunningProcesses = new RunningProcess[0],
                RegValues = new RegValue[0],
            },
        };
    }
}
