namespace NvnBootstrapper
{
    /// <summary>
    /// An install check. If the check returns false then the installation
    /// is not allowed to proceeed.
    /// </summary>
    internal class InstallCheck
    {
        /// <summary>
        /// The error message to display if the install check fails.
        /// </summary>
        public string ErrorMessage { get; set; }

        /// <summary>
        /// Gets or sets a value that inverses how the install check is
        /// handled. If this value is set to true then a failed install
        /// check is considered successful and vice versa.
        /// </summary>
        public bool Inverse { get; set; }
    }
}
