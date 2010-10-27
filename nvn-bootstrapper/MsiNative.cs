namespace NvnBootstrapper
{
    using System;
    using System.Collections;
    using System.Collections.Generic;
    using System.ComponentModel;
    using System.Runtime.InteropServices;
    using System.Text;

    internal class Msi
    {
        public const int ErrorSuccess = 0;
        public const int ErrorMoreData = 234;
        public const int ErrorNoMoreItems = 259;

        public const int GuidLength = 39;

        private const string MsiDll = "msi.dll";

        [DllImport(MsiDll, CharSet = CharSet.Auto)]
        [return: MarshalAs(UnmanagedType.U4)]
        public static extern int MsiEnumProducts(
            [MarshalAs(UnmanagedType.U4)] int iProductIndex,
            [Out] StringBuilder lpProductBuf);

        [DllImport(MsiDll, CharSet = CharSet.Auto)]
        [return: MarshalAs(UnmanagedType.U4)]
        public static extern int MsiGetProductInfo(
            string szProduct,
            string szProperty,
            [Out] StringBuilder lpValueBuf,
            [MarshalAs(UnmanagedType.U4)] [In] [Out] ref int pcchValueBuf);
    }

    internal delegate int MsiEnumFunction<T>(int index, ref T data)
        where T : struct;

    // Provides easy use from iterators like "foreach".
    internal class MsiEnumWrapper<T> : IEnumerable<T> where T : struct
    {
        private readonly MsiEnumFunction<T> func;
        private T data;

        public MsiEnumWrapper(T data, MsiEnumFunction<T> func)
        {
            if (func == null) throw new ArgumentNullException("func");
            this.data = data;
            this.func = func;
        }

        #region IEnumerable<T> Members

        public IEnumerator<T> GetEnumerator()
        {
            return new MsiEnumWrapperEnumerator<T>(this);
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }

        #endregion

        // Inner generic enumerator class that works with various Windows Installer APIs,
        // such as those begining with "MsiEnum*" that all work the same way but require
        // different data.

        #region Nested type: MsiEnumWrapperEnumerator

        private class MsiEnumWrapperEnumerator<TInner> : IEnumerator<TInner>
            where TInner : struct
        {
            private readonly MsiEnumWrapper<TInner> wrapper;
            private int i;

            public MsiEnumWrapperEnumerator(MsiEnumWrapper<TInner> wrapper)
            {
                this.wrapper = wrapper;
            }

            #region IEnumerator<TInner> Members

            public TInner Current
            {
                get { return this.wrapper.data; }
            }

            object IEnumerator.Current
            {
                get { return this.wrapper.data; }
            }

            public bool MoveNext()
            {
                var ret = this.wrapper.func(this.i++, ref this.wrapper.data);

                switch (ret)
                {
                    case Msi.ErrorSuccess:
                        return true;
                    case Msi.ErrorNoMoreItems:
                        return false;
                    default:
                        throw new Win32Exception(ret);
                }
            }

            public void Reset()
            {
                this.i = 0;
            }

            public void Dispose()
            {
            }

            #endregion
        }

        #endregion
    }

    internal struct Product
    {
        private string localPackage;
        private string productCode;
        private string productName;

        public string ProductCode
        {
            get { return this.productCode; }
            set
            {
                if (this.productCode == value)
                {
                    return;
                }

                this.productCode = value;
                this.productName = null;
            }
        }

        public string ProductName
        {
            get
            {
                return this.productName ??
                    (this.productName = GetProperty("InstalledProductName"));
            }
        }

        public string LocalPackage
        {
            get
            {
                return this.localPackage ??
                    (this.localPackage = GetProperty("LocalPackage"));
            }
        }

        private string GetProperty(string name)
        {
            var size = 0;
            var ret = Msi.MsiGetProductInfo(
                this.productCode, name, null, ref size);

            if (ret == Msi.ErrorSuccess || ret == Msi.ErrorMoreData)
            {
                var buffer = new StringBuilder(++size);
                ret = Msi.MsiGetProductInfo(
                    this.productCode, name, buffer, ref size);

                if (ret == Msi.ErrorSuccess)
                {
                    return buffer.ToString();
                }
            }

            throw new Win32Exception(ret);
        }
    }
}
