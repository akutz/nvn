namespace NvnBootstrapper
{
    using System.Windows.Forms;
    using NvnBootstrapper.Properties;

    public partial class MainForm : Form
    {
        public MainForm()
        {
            InitializeComponent();

            Icon = Resources.Icon;

            Text = InstallResources.ProductName;

            InstallManager.InstallForm = this;

            InstallManager.ProcessGlobalChecks();

            Page p;

            if (InstallManager.InstallError == null)
            {
                p = new LicensePage();
            }
            else
            {
                p = new EndPage();
            }

            Controls.Add(p);
            p.Focus();
        }
    }
}
