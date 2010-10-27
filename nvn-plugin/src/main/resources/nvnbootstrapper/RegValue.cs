namespace NvnBootstrapper
{
    internal class RegValue : InstallCheck
    {
        public string KeyPath { get; set; }

        public string ValueName { get; set; }

        public string Value { get; set; }

        public string TypeName { get; set; }

        public string Comparison { get; set; }
    }
}
