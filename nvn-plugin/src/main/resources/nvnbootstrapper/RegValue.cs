namespace NvnBootstrapper
{
    /// <summary>
    /// A registry value.
    /// </summary>
    internal class RegValue : RegKey
    {
        public string ValueName { get; set; }

        public string Value { get; set; }

        public string TypeName { get; set; }

        public string Comparison { get; set; }
    }
}
