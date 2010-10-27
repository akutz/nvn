namespace NvnBootstrapper
{
    partial class InstallPage
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
            this.pnlHeader = new System.Windows.Forms.Panel();
            this.lblInstallingHeader = new System.Windows.Forms.Label();
            this.pnlProgress = new System.Windows.Forms.Panel();
            this.lblProgress = new System.Windows.Forms.Label();
            this.progressBar = new System.Windows.Forms.ProgressBar();
            this.pnlGlobalFooter.SuspendLayout();
            this.pnlGlobalHeader.SuspendLayout();
            this.pnlGlobalContent.SuspendLayout();
            this.pnlHeader.SuspendLayout();
            this.pnlProgress.SuspendLayout();
            this.SuspendLayout();
            // 
            // pnlGlobalHeader
            // 
            this.pnlGlobalHeader.BackgroundImage = global::NvnBootstrapper.Properties.Resources.InstallPageBackground;
            this.pnlGlobalHeader.BackgroundImageLayout = System.Windows.Forms.ImageLayout.None;
            // 
            // pnlGlobalContent
            // 
            this.pnlGlobalContent.Controls.Add(this.pnlProgress);
            this.pnlGlobalContent.Controls.Add(this.pnlHeader);
            // 
            // pnlHeader
            // 
            this.pnlHeader.Controls.Add(this.lblInstallingHeader);
            this.pnlHeader.Dock = System.Windows.Forms.DockStyle.Top;
            this.pnlHeader.Location = new System.Drawing.Point(0, 0);
            this.pnlHeader.Margin = new System.Windows.Forms.Padding(0);
            this.pnlHeader.Name = "pnlHeader";
            this.pnlHeader.Size = new System.Drawing.Size(479, 58);
            this.pnlHeader.TabIndex = 0;
            // 
            // lblInstallingHeader
            // 
            this.lblInstallingHeader.AutoEllipsis = true;
            this.lblInstallingHeader.Font = new System.Drawing.Font("Tahoma", 10F, System.Drawing.FontStyle.Bold);
            this.lblInstallingHeader.Location = new System.Drawing.Point(5, 14);
            this.lblInstallingHeader.Name = "lblInstallingHeader";
            this.lblInstallingHeader.Size = new System.Drawing.Size(570, 31);
            this.lblInstallingHeader.TabIndex = 0;
            this.lblInstallingHeader.Text = "Installing Nvn Bootstrapper";
            this.lblInstallingHeader.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // pnlProgress
            // 
            this.pnlProgress.BackColor = System.Drawing.SystemColors.Control;
            this.pnlProgress.Controls.Add(this.lblProgress);
            this.pnlProgress.Controls.Add(this.progressBar);
            this.pnlProgress.Dock = System.Windows.Forms.DockStyle.Fill;
            this.pnlProgress.Location = new System.Drawing.Point(0, 58);
            this.pnlProgress.Margin = new System.Windows.Forms.Padding(0);
            this.pnlProgress.Name = "pnlProgress";
            this.pnlProgress.Padding = new System.Windows.Forms.Padding(5, 5, 0, 5);
            this.pnlProgress.Size = new System.Drawing.Size(479, 255);
            this.pnlProgress.TabIndex = 1;
            // 
            // lblProgress
            // 
            this.lblProgress.AutoEllipsis = true;
            this.lblProgress.Font = new System.Drawing.Font("Tahoma", 8.25F, System.Drawing.FontStyle.Bold);
            this.lblProgress.Location = new System.Drawing.Point(8, 100);
            this.lblProgress.Name = "lblProgress";
            this.lblProgress.Size = new System.Drawing.Size(467, 16);
            this.lblProgress.TabIndex = 3;
            this.lblProgress.Text = "Extracting embedded install packages...";
            // 
            // progressBar
            // 
            this.progressBar.Location = new System.Drawing.Point(8, 119);
            this.progressBar.Name = "progressBar";
            this.progressBar.Size = new System.Drawing.Size(468, 23);
            this.progressBar.TabIndex = 2;
            // 
            // InstallPage
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Name = "InstallPage";
            this.pnlGlobalFooter.ResumeLayout(false);
            this.pnlGlobalHeader.ResumeLayout(false);
            this.pnlGlobalContent.ResumeLayout(false);
            this.pnlHeader.ResumeLayout(false);
            this.pnlProgress.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Panel pnlHeader;
        private System.Windows.Forms.Panel pnlProgress;
        private System.Windows.Forms.Label lblInstallingHeader;
        private System.Windows.Forms.ProgressBar progressBar;
        private System.Windows.Forms.Label lblProgress;

    }
}
