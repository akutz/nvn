namespace NvnBootstrapper
{
    partial class LicensePage
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
            this.lblPleaseRead = new System.Windows.Forms.Label();
            this.chkBoxAccept = new System.Windows.Forms.CheckBox();
            this.btnInstall = new System.Windows.Forms.Button();
            this.btnPrint = new System.Windows.Forms.Button();
            this.txtBoxLicense = new System.Windows.Forms.RichTextBox();
            this.pnlLicense.SuspendLayout();
            this.pnlGlobalFooter.SuspendLayout();
            this.pnlGlobalHeader.SuspendLayout();
            this.SuspendLayout();
            // 
            // pnlLicense
            // 
            this.pnlLicense.Controls.Add(this.txtBoxLicense);
            this.pnlLicense.Controls.Add(this.chkBoxAccept);
            this.pnlLicense.Controls.Add(this.lblPleaseRead);
            this.pnlLicense.Location = new System.Drawing.Point(166, 0);
            this.pnlLicense.Size = new System.Drawing.Size(313, 312);
            // 
            // pnlGlobalFooter
            // 
            this.pnlGlobalFooter.Controls.Add(this.btnInstall);
            this.pnlGlobalFooter.Controls.Add(this.btnPrint);
            this.pnlGlobalFooter.Location = new System.Drawing.Point(0, 312);
            this.pnlGlobalFooter.Size = new System.Drawing.Size(499, 51);
            this.pnlGlobalFooter.Controls.SetChildIndex(this.btnAllPurpose, 0);
            this.pnlGlobalFooter.Controls.SetChildIndex(this.btnPrint, 0);
            this.pnlGlobalFooter.Controls.SetChildIndex(this.btnInstall, 0);
            // 
            // pnlGlobalHeader
            // 
            this.pnlGlobalHeader.Size = new System.Drawing.Size(499, 312);
            // 
            // pnlGlobalContent
            // 
            this.pnlGlobalContent.Size = new System.Drawing.Size(166, 312);
            // 
            // btnAllPurpose
            // 
            this.btnAllPurpose.TabIndex = 2;
            this.btnAllPurpose.TabStop = true;
            // 
            // lblPleaseRead
            // 
            this.lblPleaseRead.AutoEllipsis = true;
            this.lblPleaseRead.Font = new System.Drawing.Font("Tahoma", 8.25F, System.Drawing.FontStyle.Bold);
            this.lblPleaseRead.ImeMode = System.Windows.Forms.ImeMode.NoControl;
            this.lblPleaseRead.Location = new System.Drawing.Point(10, 10);
            this.lblPleaseRead.Name = "lblPleaseRead";
            this.lblPleaseRead.Size = new System.Drawing.Size(298, 30);
            this.lblPleaseRead.TabIndex = 3;
            this.lblPleaseRead.Text = "Please read the [ProductName]\'s License Agreement";
            // 
            // chkBoxAccept
            // 
            this.chkBoxAccept.BackColor = System.Drawing.Color.Gainsboro;
            this.chkBoxAccept.ImeMode = System.Windows.Forms.ImeMode.NoControl;
            this.chkBoxAccept.Location = new System.Drawing.Point(10, 273);
            this.chkBoxAccept.Name = "chkBoxAccept";
            this.chkBoxAccept.Padding = new System.Windows.Forms.Padding(4);
            this.chkBoxAccept.Size = new System.Drawing.Size(298, 25);
            this.chkBoxAccept.TabIndex = 0;
            this.chkBoxAccept.Text = "I accept the terms in the License Aggreement";
            this.chkBoxAccept.UseVisualStyleBackColor = false;
            // 
            // btnInstall
            // 
            this.btnInstall.Enabled = false;
            this.btnInstall.ImeMode = System.Windows.Forms.ImeMode.NoControl;
            this.btnInstall.Location = new System.Drawing.Point(331, 15);
            this.btnInstall.Name = "btnInstall";
            this.btnInstall.Size = new System.Drawing.Size(71, 23);
            this.btnInstall.TabIndex = 1;
            this.btnInstall.Text = "&Install";
            this.btnInstall.UseVisualStyleBackColor = true;
            // 
            // btnPrint
            // 
            this.btnPrint.ImeMode = System.Windows.Forms.ImeMode.NoControl;
            this.btnPrint.Location = new System.Drawing.Point(176, 15);
            this.btnPrint.Name = "btnPrint";
            this.btnPrint.Size = new System.Drawing.Size(71, 23);
            this.btnPrint.TabIndex = 3;
            this.btnPrint.Text = "&Print";
            this.btnPrint.UseVisualStyleBackColor = true;
            // 
            // txtBoxLicense
            // 
            this.txtBoxLicense.Location = new System.Drawing.Point(10, 43);
            this.txtBoxLicense.Name = "txtBoxLicense";
            this.txtBoxLicense.Size = new System.Drawing.Size(298, 216);
            this.txtBoxLicense.TabIndex = 7;
            this.txtBoxLicense.TabStop = false;
            this.txtBoxLicense.Text = "";
            // 
            // LicensePage
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Name = "LicensePage";
            this.pnlLicense.ResumeLayout(false);
            this.pnlGlobalFooter.ResumeLayout(false);
            this.pnlGlobalHeader.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Label lblPleaseRead;
        private System.Windows.Forms.CheckBox chkBoxAccept;
        private System.Windows.Forms.Button btnInstall;
        private System.Windows.Forms.Button btnPrint;
        private System.Windows.Forms.RichTextBox txtBoxLicense;
    }
}
