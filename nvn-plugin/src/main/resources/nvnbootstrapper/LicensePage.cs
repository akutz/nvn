namespace NvnBootstrapper
{
    using System;
    using System.Drawing.Printing;
    using System.Runtime.InteropServices;
    using System.Windows.Forms;
    using NvnBootstrapper.Properties;

    public partial class LicensePage : BeginAndEndPage
    {
        private readonly PrintDialog printDialog = new PrintDialog();
        private int checkPrint;

        public LicensePage()
        {
            InitializeComponent();

            Load += (s,e) => ProcessTabKey(false);

            this.chkBoxAccept.CheckedChanged +=
                (s, e) => { this.btnInstall.Enabled = this.chkBoxAccept.Checked; };

            this.btnInstall.Click += (s, e) =>
            {
                var c = Parent;
                c.Controls.Clear();
                c.Controls.Add(new InstallPage());
            };

            this.lblPleaseRead.Text =
                string.Format(
                    @"Please read the {0}'s License Agreement",
                    InstallResources.ProductName);

            this.txtBoxLicense.Rtf = Resources.License;

            this.btnPrint.Click += (s, e) =>
            {
                if (this.printDialog.ShowDialog(ParentForm) != DialogResult.OK)
                {
                    return;
                }

                var pd = new PrintDocument();
                pd.BeginPrint += (se, ev) => this.checkPrint = 0;
                pd.PrintPage += (se, ev) =>
                {
                    this.checkPrint = this.txtBoxLicense.Print(
                        this.checkPrint, this.txtBoxLicense.TextLength, ev);
                    ev.HasMorePages = this.checkPrint <
                        this.txtBoxLicense.TextLength;
                };
                pd.Print();
            };
        }
    }

    internal static class ExtensionMethods
    {
        /// <summary>
        /// Convert the unit used by the .NET framework (1/100 inch) 
        /// and the unit used by Win32 API calls (twips 1/1440 inch)
        /// </summary>
        private const double anInch = 14.4;

        private const int WM_USER = 0x0400;
        private const int EM_FORMATRANGE = WM_USER + 57;

        [DllImport("USER32.dll")]
        private static extern IntPtr SendMessage(
            IntPtr hWnd, int msg, IntPtr wp, IntPtr lp);

        public static int Print(
            this RichTextBox textBox,
            int charFrom,
            int charTo,
            PrintPageEventArgs e)
        {
            //Calculate the area to render and print
            RECT rectToPrint;
            rectToPrint.Top = (int) (e.MarginBounds.Top*anInch);
            rectToPrint.Bottom = (int) (e.MarginBounds.Bottom*anInch);
            rectToPrint.Left = (int) (e.MarginBounds.Left*anInch);
            rectToPrint.Right = (int) (e.MarginBounds.Right*anInch);

            //Calculate the size of the page
            RECT rectPage;
            rectPage.Top = (int) (e.PageBounds.Top*anInch);
            rectPage.Bottom = (int) (e.PageBounds.Bottom*anInch);
            rectPage.Left = (int) (e.PageBounds.Left*anInch);
            rectPage.Right = (int) (e.PageBounds.Right*anInch);

            var hdc = e.Graphics.GetHdc();

            FORMATRANGE fmtRange;
            fmtRange.chrg.cpMax = charTo;
            //Indicate character from to character to 
            fmtRange.chrg.cpMin = charFrom;
            fmtRange.hdc = hdc; //Use the same DC for measuring and rendering
            fmtRange.hdcTarget = hdc; //Point at printer hDC
            fmtRange.rc = rectToPrint; //Indicate the area on page to print
            fmtRange.rcPage = rectPage; //Indicate size of page

            var res = IntPtr.Zero;

            var wparam = IntPtr.Zero;
            wparam = new IntPtr(1);

            //Get the pointer to the FORMATRANGE structure in memory
            var lparam = IntPtr.Zero;
            lparam = Marshal.AllocCoTaskMem(Marshal.SizeOf(fmtRange));
            Marshal.StructureToPtr(fmtRange, lparam, false);

            //Send the rendered data for printing 
            res = SendMessage(textBox.Handle, EM_FORMATRANGE, wparam, lparam);

            //Free the block of memory allocated
            Marshal.FreeCoTaskMem(lparam);

            //Release the device context handle obtained by a previous call
            e.Graphics.ReleaseHdc(hdc);

            //Return last + 1 character printer
            return res.ToInt32();
        }

        #region Nested type: CHARRANGE

        [StructLayout(LayoutKind.Sequential)]
        private struct CHARRANGE
        {
            /// <summary>
            /// First character of range (0 for start of doc)
            /// </summary>
            public int cpMin;

            /// <summary>
            /// Last character of range (-1 for end of doc)
            /// </summary>
            public int cpMax;
        }

        #endregion

        #region Nested type: FORMATRANGE

        [StructLayout(LayoutKind.Sequential)]
        private struct FORMATRANGE
        {
            /// <summary>
            /// Actual DC to draw on
            /// </summary>
            public IntPtr hdc;

            /// <summary>
            /// Target DC for determining text formatting
            /// </summary>
            public IntPtr hdcTarget;

            /// <summary>
            /// Region of the DC to draw to (in twips)
            /// </summary>
            public RECT rc;

            /// <summary>
            /// Region of the whole DC (page size) (in twips)
            /// </summary>
            public RECT rcPage;

            /// <summary>
            /// Range of text to draw (see earlier declaration)
            /// </summary>
            public CHARRANGE chrg;
        }

        #endregion

        #region Nested type: RECT

        [StructLayout(LayoutKind.Sequential)]
        private struct RECT
        {
            public int Left;
            public int Top;
            public int Right;
            public int Bottom;
        }

        #endregion
    }
}
