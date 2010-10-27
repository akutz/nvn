namespace NvnBootstrapper
{
    using System.Drawing;
    using System.Windows.Forms;

    public partial class Page : UserControl
    {
        public Page()
        {
            InitializeComponent();

            Load += (s, e) =>
            {
                if (Parent is Form)
                {
                    ((Form) Parent).CancelButton = this.btnAllPurpose;
                }
            };

            Paint += (s, e) =>
            {
                var g = this.pnlGlobalFooter.CreateGraphics();
                g.DrawLine(Pens.Silver, 0, 0, Width, 0);
                g.DrawLine(Pens.White, 0, 1, Width, 1);
            };

            this.btnAllPurpose.Click +=
                (s, e) => InstallManager.OnAllPurposeClick(this);
        }
    }
}
