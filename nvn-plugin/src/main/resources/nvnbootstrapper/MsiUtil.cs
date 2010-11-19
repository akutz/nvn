namespace NvnBootstrapper
{
    using System;
    using System.Collections;
    using System.Collections.Generic;
    using System.ComponentModel;
    using System.IO;
    using System.Runtime.InteropServices;
    using System.Text;
    using System.Threading;

    internal static class MsiUtil
    {
        /// <summary>
        /// The HRESULT error code for success.
        /// </summary>
        private const int ErrorSuccess = 0;

        /// <summary>
        /// The HRESULT error code that indicates there is more
        /// data available to retrieve.
        /// </summary>
        private const int ErrorMoreData = 234;

        /// <summary>
        /// The HRESULT error code that indicates there is
        /// no more data available.
        /// </summary>
        private const int ErrorNoMoreItems = 259;

        /// <summary>
        /// The expected length of a GUID.
        /// </summary>
        private const int GuidLength = 39;

        /// <summary>
        /// A format provider used to format DateTime objects.
        /// </summary>
        private static readonly IFormatProvider DateTimeFormatProvider =
            Thread.CurrentThread.CurrentCulture;

        /// <summary>
        /// Gets the the products installed or advertised on the system.
        /// </summary>
        /// <returns>An array of installed or advertised products.</returns>
        public static Product[] Products
        {
            get
            {
                var products = new List<Product>();

                foreach (var p in
                    new ProductEnumeratorWrapper<Product>(
                        default(Product), MsiEnumProductCallback))
                {
                    products.Add(p);
                }

                return products.ToArray();
            }
        }

        /**
         * http://msdn.microsoft.com/en-us/library/aa370101(VS.85).aspx
         */

        [DllImport(@"msi.dll", CharSet = CharSet.Auto)]
        [return: MarshalAs(UnmanagedType.U4)]
        private static extern int MsiEnumProducts(
            [MarshalAs(UnmanagedType.U4)] int iProductIndex,
            [Out] StringBuilder lpProductBuf);

        /**
         * http://msdn.microsoft.com/en-us/library/aa370130(VS.85).aspx
         */

        [DllImport(@"msi.dll", CharSet = CharSet.Auto)]
        [return: MarshalAs(UnmanagedType.U4)]
        private static extern int MsiGetProductInfo(
            string szProduct,
            string szProperty,
            [Out] StringBuilder lpValueBuf,
            [MarshalAs(UnmanagedType.U4)] [In] [Out] ref int pcchValueBuf);

        /*
         * http://msdn.microsoft.com/en-us/library/aa370338(VS.85).aspx
         */

        [DllImport(@"msi.dll", SetLastError = true)]
        private static extern int MsiOpenDatabase(
            string szDatabasePath, IntPtr phPersist, out IntPtr phDatabase);

        /*
         * http://msdn.microsoft.com/en-us/library/aa370082(VS.85).aspx
         */

        [DllImport(@"msi.dll", CharSet = CharSet.Unicode)]
        private static extern int MsiDatabaseOpenView(
            IntPtr hDatabase,
            [MarshalAs(UnmanagedType.LPWStr)] string szQuery,
            out IntPtr phView);

        /*
         * http://msdn.microsoft.com/en-us/library/aa370513(VS.85).aspx
         */

        [DllImport(@"msi.dll", CharSet = CharSet.Unicode)]
        private static extern int MsiViewExecute(IntPtr hView, IntPtr hRecord);

        /*
         * http://msdn.microsoft.com/en-us/library/aa370514(VS.85).aspx
         */

        [DllImport(@"msi.dll", CharSet = CharSet.Unicode)]
        private static extern int MsiViewFetch(IntPtr hView, out IntPtr hRecord);

        /*
         * http://msdn.microsoft.com/en-us/library/aa370368(VS.85).aspx
         */

        [DllImport(@"msi.dll", CharSet = CharSet.Unicode)]
        private static extern int MsiRecordGetString(
            IntPtr hRecord,
            int iField,
            [Out] StringBuilder szValueBuf,
            ref int pcchValueBuf);

        /*
         * http://msdn.microsoft.com/en-us/library/aa370510(VS.85).aspx
         */

        [DllImport(@"msi.dll")]
        private static extern int MsiViewClose(IntPtr viewhandle);

        /*
         * http://msdn.microsoft.com/en-us/library/aa370067(VS.85).aspx
         */

        [DllImport(@"msi.dll", ExactSpelling = true)]
        private static extern int MsiCloseHandle(IntPtr hAny);

        /// <summary>
        /// Gets a property from an MSI package.
        /// </summary>
        /// <param name="msiPackage">An MSI package.</param>
        /// <param name="propName">The name of the property to get.</param>
        /// <returns>The property's value.</returns>
        public static string GetMsiPackageStringProperty(
            FileSystemInfo msiPackage, string propName)
        {
            IntPtr ptrDb;

            var hrOpen = MsiOpenDatabase(
                msiPackage.FullName,
                new IntPtr(0),
                /* ReadOnly */
                out ptrDb);

            if (hrOpen != ErrorSuccess)
            {
                throw new Win32Exception(hrOpen);
            }

            string propValue;

            IntPtr ptrView;
            var query =
                string.Format(
                    @"SELECT `Value` FROM `Property` WHERE `Property` = '{0}'",
                    propName);

            var hrOpenView = MsiDatabaseOpenView(ptrDb, query, out ptrView);

            if (hrOpenView == ErrorSuccess)
            {
                var hrViewExec = MsiViewExecute(ptrView, IntPtr.Zero);

                if (hrViewExec == ErrorSuccess)
                {
                    IntPtr ptrRecord;

                    var hrViewFetch = MsiViewFetch(ptrView, out ptrRecord);

                    if (hrViewFetch == ErrorSuccess)
                    {
                        var upgradeCodeBuff = new StringBuilder();
                        var upgradeCodeBuffLen = GuidLength;

                        var hrRecGetStr = MsiRecordGetString(
                            ptrRecord,
                            1,
                            upgradeCodeBuff,
                            ref upgradeCodeBuffLen);

                        if (hrRecGetStr == ErrorSuccess)
                        {
                            propValue = upgradeCodeBuff.ToString();
                        }
                        else
                        {
                            throw new Win32Exception(hrRecGetStr);
                        }
                    }
                    else
                    {
                        throw new Win32Exception(hrViewFetch);
                    }
                }
                else
                {
                    throw new Win32Exception(hrViewExec);
                }

                var hrCloseView = MsiViewClose(ptrView);

                if (hrCloseView != ErrorSuccess)
                {
                    throw new Win32Exception(hrCloseView);
                }
            }
            else
            {
                throw new Win32Exception(hrOpenView);
            }

            var hrCloseDb = MsiCloseHandle(ptrDb);

            if (hrCloseDb != ErrorSuccess)
            {
                throw new Win32Exception(hrCloseDb);
            }

            return propValue;
        }

        /// <summary>
        /// Gets the ProductCode property from an MSI package.
        /// </summary>
        /// <param name="msiPackage">
        /// The MSI package from which to get the ProductCode property.
        /// </param>
        /// <returns>
        /// The MSI package's ProductCode.
        /// </returns>
        public static string GetProductCode(FileSystemInfo msiPackage)
        {
            return GetMsiPackageStringProperty(msiPackage, @"ProductCode");
        }

        /// <summary>
        /// Gets the UpgradCode property from an MSI package.
        /// </summary>
        /// <param name="msiPackage">
        /// The MSI package from which to get the UpgradeCode property.
        /// </param>
        /// <returns>
        /// The MSI package's UpgradeCode.
        /// </returns>
        public static string GetUpgradeCode(FileSystemInfo msiPackage)
        {
            return GetMsiPackageStringProperty(msiPackage, @"UpgradeCode");
        }

        /// <summary>
        /// Gets an MSI property.
        /// </summary>
        /// <param name="product">The MSI product.</param>
        /// <param name="name">The name of the property to get.</param>
        /// <returns>The property's value.</returns>
        /// <remarks>
        /// For more information on available properties please see:
        /// http://msdn.microsoft.com/en-us/library/aa370130(VS.85).aspx
        /// </remarks>
        private static string GetProperty(Product product, string name)
        {
            var size = 0;
            var hresult = MsiGetProductInfo(
                product.ProductCode, name, null, ref size);

            if (hresult == ErrorSuccess || hresult == ErrorMoreData)
            {
                var buffer = new StringBuilder(++size);
                hresult = MsiGetProductInfo(
                    product.ProductCode, name, buffer, ref size);

                if (hresult == ErrorSuccess)
                {
                    return buffer.ToString();
                }
            }

            throw new Win32Exception(hresult);
        }

        private static int MsiEnumProductCallback(
            int index, ref Product product)
        {
            var buffer = new StringBuilder(GuidLength);
            var hresult = MsiEnumProducts(index, buffer);

            if (hresult != ErrorSuccess)
            {
                return hresult;
            }

            product.ProductCode = buffer.ToString();

            product.InstallDate =
                DateTime.ParseExact(
                    GetProperty(product, @"InstallDate"),
                    @"yyyyMMdd",
                    DateTimeFormatProvider);

            product.LocalPackage =
                new FileInfo(GetProperty(product, @"LocalPackage"));
            product.ProductName = GetProperty(product, @"InstalledProductName");
            product.ProductVersion = GetProperty(product, @"VersionString");

            return ErrorSuccess;
        }

        #region Nested type: MsiEnumFunction

        private delegate int MsiEnumFunction<T>(int index, ref T data)
            where T : struct;

        #endregion

        #region Nested type: Product

        /// <summary>
        /// An MSI product.
        /// </summary>
        public struct Product
        {
            /// <summary>
            /// Gets or sets the product's unique GUID.
            /// </summary>
            public string ProductCode { get; internal set; }

            /// <summary>
            /// Gets the product's name.
            /// </summary>
            public string ProductName { get; internal set; }

            /// <summary>
            /// Gets the path to the product's local package.
            /// </summary>
            public FileInfo LocalPackage { get; internal set; }

            /// <summary>
            /// Gets the product's version.
            /// </summary>
            public string ProductVersion { get; internal set; }

            /// <summary>
            /// Gets the product's install date.
            /// </summary>
            public DateTime InstallDate { get; internal set; }
        }

        #endregion

        #region Nested type: ProductEnumeratorWrapper

        private class ProductEnumeratorWrapper<T> : IEnumerable<T>
            where T : struct
        {
            private readonly MsiEnumFunction<T> func;
            private T data;

            public ProductEnumeratorWrapper(T data, MsiEnumFunction<T> func)
            {
                this.data = data;
                this.func = func;
            }

            #region IEnumerable<T> Members

            public IEnumerator<T> GetEnumerator()
            {
                return new ProductEnumerator<T>(this);
            }

            IEnumerator IEnumerable.GetEnumerator()
            {
                return GetEnumerator();
            }

            #endregion

            #region Nested type: ProductEnumerator

            private class ProductEnumerator<TInner> : IEnumerator<TInner>
                where TInner : struct
            {
                /// <summary>
                /// The enumerator's wrapper.
                /// </summary>
                private readonly ProductEnumeratorWrapper<TInner> wrapper;

                /// <summary>
                /// The index.
                /// </summary>
                private int i;

                public ProductEnumerator(
                    ProductEnumeratorWrapper<TInner> wrapper)
                {
                    this.wrapper = wrapper;
                }

                #region IEnumerator<TInner> Members

                /// <summary>
                /// Gets the element in the collection at the current position 
                /// of the enumerator.
                /// </summary>
                /// <returns>
                /// The element in the collection at the current position 
                /// of the enumerator.
                /// </returns>
                object IEnumerator.Current
                {
                    get { return this.wrapper.data; }
                }

                public bool MoveNext()
                {
                    var hresult = this.wrapper.func(
                        this.i++, ref this.wrapper.data);

                    switch (hresult)
                    {
                        case ErrorSuccess:
                        {
                            return true;
                        }
                        case ErrorNoMoreItems:
                        {
                            return false;
                        }
                        default:
                        {
                            throw new Win32Exception(hresult);
                        }
                    }
                }

                public void Reset()
                {
                    this.i = 0;
                }

                /// <summary>
                /// Gets the element in the collection at the current position 
                /// of the enumerator.
                /// </summary>
                /// <returns>
                /// The element in the collection at the current position 
                /// of the enumerator.
                /// </returns>
                public TInner Current
                {
                    get { return this.wrapper.data; }
                }

                public void Dispose()
                {
                    // Do nothing
                }

                #endregion
            }

            #endregion
        }

        #endregion
    }
}
