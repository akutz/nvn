namespace NvnBootstrapper
{
    partial class BeginAndEndPage
    {
        /// <summary> 
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary> 
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.pnlLicense = new System.Windows.Forms.Panel();
            this.pnlGlobalFooter.SuspendLayout();
            this.pnlGlobalHeader.SuspendLayout();
            this.SuspendLayout();
            // 
            // pnlGlobalHeader
            // 
            this.pnlGlobalHeader.BackgroundImage = global::NvnBootstrapper.Properties.Resources.LicensePageBackground;
            this.pnlGlobalHeader.BackgroundImageLayout = System.Windows.Forms.ImageLayout.None;
            this.pnlGlobalHeader.Controls.Add(this.pnlLicense);
            this.pnlGlobalHeader.Controls.SetChildIndex(this.pnlLicense, 0);
            this.pnlGlobalHeader.Controls.SetChildIndex(this.pnlGlobalContent, 0);
            // 
            // pnlGlobalContent
            // 
            this.pnlGlobalContent.Size = new System.Drawing.Size(164, 390);
            // 
            // pnlLicense
            // 
            this.pnlLicense.BackColor = System.Drawing.Color.Transparent;
            this.pnlLicense.Dock = System.Windows.Forms.DockStyle.Right;
            this.pnlLicense.Location = new System.Drawing.Point(164, 0);
            this.pnlLicense.Name = "pnlLicense";
            this.pnlLicense.Size = new System.Drawing.Size(451, 390);
            this.pnlLicense.TabIndex = 0;
            // 
            // BeginAndEndPage
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Name = "BeginAndEndPage";
            this.pnlGlobalFooter.ResumeLayout(false);
            this.pnlGlobalHeader.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        protected System.Windows.Forms.Panel pnlLicense;

    }
}
