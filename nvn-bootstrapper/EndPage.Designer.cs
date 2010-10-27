namespace NvnBootstrapper
{
    partial class EndPage
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
            this.lnkEmc = new System.Windows.Forms.LinkLabel();
            this.lblStatus = new System.Windows.Forms.Label();
            this.lblExplanation = new System.Windows.Forms.Label();
            this.pnlLicense.SuspendLayout();
            this.pnlGlobalFooter.SuspendLayout();
            this.pnlGlobalHeader.SuspendLayout();
            this.SuspendLayout();
            // 
            // pnlLicense
            // 
            this.pnlLicense.Controls.Add(this.lblExplanation);
            this.pnlLicense.Controls.Add(this.lblStatus);
            this.pnlLicense.Controls.Add(this.lnkEmc);
            this.pnlLicense.Location = new System.Drawing.Point(166, 0);
            this.pnlLicense.Size = new System.Drawing.Size(313, 313);
            // 
            // pnlGlobalContent
            // 
            this.pnlGlobalContent.Size = new System.Drawing.Size(166, 313);
            // 
            // btnAllPurpose
            // 
            this.btnAllPurpose.TabIndex = 0;
            this.btnAllPurpose.TabStop = true;
            this.btnAllPurpose.Text = "&Finish";
            // 
            // lnkEmc
            // 
            this.lnkEmc.AutoSize = true;
            this.lnkEmc.LinkArea = new System.Windows.Forms.LinkArea(26, 45);
            this.lnkEmc.LinkBehavior = System.Windows.Forms.LinkBehavior.HoverUnderline;
            this.lnkEmc.Location = new System.Drawing.Point(19, 284);
            this.lnkEmc.Name = "lnkEmc";
            this.lnkEmc.Size = new System.Drawing.Size(235, 18);
            this.lnkEmc.TabIndex = 2;
            this.lnkEmc.TabStop = true;
            this.lnkEmc.Text = "Please visit us online at http://www.emc.com/";
            this.lnkEmc.UseCompatibleTextRendering = true;
            // 
            // lblStatus
            // 
            this.lblStatus.AutoEllipsis = true;
            this.lblStatus.Font = new System.Drawing.Font("Tahoma", 12F);
            this.lblStatus.Location = new System.Drawing.Point(15, 26);
            this.lblStatus.Name = "lblStatus";
            this.lblStatus.Size = new System.Drawing.Size(280, 64);
            this.lblStatus.TabIndex = 3;
            this.lblStatus.Text = "Congratulations!";
            // 
            // lblExplanation
            // 
            this.lblExplanation.AutoEllipsis = true;
            this.lblExplanation.Location = new System.Drawing.Point(16, 101);
            this.lblExplanation.Name = "lblExplanation";
            this.lblExplanation.Size = new System.Drawing.Size(279, 170);
            this.lblExplanation.TabIndex = 4;
            this.lblExplanation.Text = "The NVN Bootstrapper installation completed successfully.";
            // 
            // EndPage
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Name = "EndPage";
            this.pnlLicense.ResumeLayout(false);
            this.pnlLicense.PerformLayout();
            this.pnlGlobalFooter.ResumeLayout(false);
            this.pnlGlobalHeader.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.LinkLabel lnkEmc;
        private System.Windows.Forms.Label lblExplanation;
        private System.Windows.Forms.Label lblStatus;
    }
}
