namespace NvnBootstrapper
{
    partial class Page
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
            this.pnlGlobalFooter = new System.Windows.Forms.Panel();
            this.btnAllPurpose = new System.Windows.Forms.Button();
            this.pnlGlobalHeader = new System.Windows.Forms.Panel();
            this.pnlGlobalContent = new System.Windows.Forms.Panel();
            this.pnlGlobalFooter.SuspendLayout();
            this.pnlGlobalHeader.SuspendLayout();
            this.SuspendLayout();
            // 
            // pnlGlobalFooter
            // 
            this.pnlGlobalFooter.Controls.Add(this.btnAllPurpose);
            this.pnlGlobalFooter.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.pnlGlobalFooter.Location = new System.Drawing.Point(0, 313);
            this.pnlGlobalFooter.Margin = new System.Windows.Forms.Padding(0);
            this.pnlGlobalFooter.Name = "pnlGlobalFooter";
            this.pnlGlobalFooter.Padding = new System.Windows.Forms.Padding(0, 0, 20, 0);
            this.pnlGlobalFooter.Size = new System.Drawing.Size(499, 50);
            this.pnlGlobalFooter.TabIndex = 0;
            // 
            // btnAllPurpose
            // 
            this.btnAllPurpose.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.btnAllPurpose.ImeMode = System.Windows.Forms.ImeMode.NoControl;
            this.btnAllPurpose.Location = new System.Drawing.Point(408, 15);
            this.btnAllPurpose.Name = "btnAllPurpose";
            this.btnAllPurpose.Size = new System.Drawing.Size(71, 23);
            this.btnAllPurpose.TabIndex = 999;
            this.btnAllPurpose.TabStop = false;
            this.btnAllPurpose.Text = "&Cancel";
            this.btnAllPurpose.UseVisualStyleBackColor = true;
            // 
            // pnlGlobalHeader
            // 
            this.pnlGlobalHeader.Controls.Add(this.pnlGlobalContent);
            this.pnlGlobalHeader.Dock = System.Windows.Forms.DockStyle.Fill;
            this.pnlGlobalHeader.Location = new System.Drawing.Point(0, 0);
            this.pnlGlobalHeader.Margin = new System.Windows.Forms.Padding(0);
            this.pnlGlobalHeader.Name = "pnlGlobalHeader";
            this.pnlGlobalHeader.Padding = new System.Windows.Forms.Padding(0, 0, 20, 0);
            this.pnlGlobalHeader.Size = new System.Drawing.Size(499, 313);
            this.pnlGlobalHeader.TabIndex = 1;
            // 
            // pnlGlobalContent
            // 
            this.pnlGlobalContent.BackColor = System.Drawing.Color.Transparent;
            this.pnlGlobalContent.Dock = System.Windows.Forms.DockStyle.Fill;
            this.pnlGlobalContent.Location = new System.Drawing.Point(0, 0);
            this.pnlGlobalContent.Margin = new System.Windows.Forms.Padding(0);
            this.pnlGlobalContent.Name = "pnlGlobalContent";
            this.pnlGlobalContent.Size = new System.Drawing.Size(479, 313);
            this.pnlGlobalContent.TabIndex = 0;
            // 
            // Page
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.pnlGlobalHeader);
            this.Controls.Add(this.pnlGlobalFooter);
            this.Font = new System.Drawing.Font("Tahoma", 8.25F);
            this.Margin = new System.Windows.Forms.Padding(0);
            this.Name = "Page";
            this.Size = new System.Drawing.Size(499, 363);
            this.pnlGlobalFooter.ResumeLayout(false);
            this.pnlGlobalHeader.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        protected System.Windows.Forms.Panel pnlGlobalFooter;
        protected System.Windows.Forms.Panel pnlGlobalHeader;
        protected System.Windows.Forms.Panel pnlGlobalContent;
        protected System.Windows.Forms.Button btnAllPurpose;


    }
}
