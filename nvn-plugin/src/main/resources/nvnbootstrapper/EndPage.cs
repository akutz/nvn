namespace NvnBootstrapper
{
    using System.Diagnostics;
    using System.Windows.Forms;

    public partial class EndPage : BeginAndEndPage
    {
        private const string PleaseVisitUs = @"Please visit us online at ";

        public EndPage()
        {
            InitializeComponent();

            this.lnkEmc.Text = string.Format(
                @"{0}{1}", PleaseVisitUs, InstallResources.EndPageUrlText);

            this.lnkEmc.LinkArea = new LinkArea(
                PleaseVisitUs.Length, InstallResources.EndPageUrlText.Length);

            this.lnkEmc.Links[0].LinkData = InstallResources.EndPageUrlLink;

            this.lnkEmc.LinkClicked +=
                (s, e) => Process.Start((string) e.Link.LinkData);

            if (InstallManager.InstallError == null)
            {
                this.lblStatus.Text =
                    string.Format(
                        @"{0} completed successfully.",
                        InstallResources.ProductName);
                this.lblExplanation.Text = string.Empty;
            }
            else
            {
                this.lblStatus.Text =
                    string.Format(
                        @"{0} did not complete successfully",
                        InstallResources.ProductName);
                this.lblExplanation.Text = InstallManager.InstallError;
            }
        }
    }
}
