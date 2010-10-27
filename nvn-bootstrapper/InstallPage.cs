namespace NvnBootstrapper
{
    using System.Windows.Forms;

    public partial class InstallPage : Page
    {
        public InstallPage()
        {
            InitializeComponent();
            this.lblInstallingHeader.Text = string.Format(
                @"Installing {0}", InstallResources.ProductName);

            btnAllPurpose.Enabled = false;

            InstallManager.InstallMessage +=
                m => Invoke(new MethodInvoker(() => this.lblProgress.Text = m));

            InstallManager.InstallProgressInit +=
                t =>
                    Invoke(
                        new MethodInvoker(() => this.progressBar.Maximum = t));

            InstallManager.InstallProgressIncrement +=
                () => Invoke(
                    new MethodInvoker(
                        () =>
                        {
                            if (this.progressBar.Value <
                                this.progressBar.Maximum)
                            {
                                this.progressBar.Increment(1);
                            }
                        }));

            InstallManager.InstallProgressDecrement +=
                () => Invoke(
                    new MethodInvoker(
                        () =>
                        {
                            if (this.progressBar.Value > 0)
                            {
                                this.progressBar.Increment(-1);
                            }
                        }));

            InstallManager.InstallEnd += () => Invoke(
                new MethodInvoker(
                    () =>
                    {
                        btnAllPurpose.Text = @"&Next";
                        btnAllPurpose.Enabled = true;
                        btnAllPurpose.Focus();
                    }));

            Load += (s, e) => InstallManager.Install();
        }
    }
}
