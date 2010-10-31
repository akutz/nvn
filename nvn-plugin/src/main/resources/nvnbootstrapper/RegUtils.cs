namespace NvnBootstrapper
{
    using System;
    using System.Reflection;
    using System.Runtime.InteropServices;
    using Microsoft.Win32;

    /// <summary>
    /// A utilities class for managing the Registry.
    /// </summary>
    internal static class RegUtils
    {
        #region ProcessArchitecture enum

        /// <summary>
        /// The different values for a process's architecture.
        /// </summary>
        public enum ProcessArchitecture
        {
            /// <summary>
            /// A 32-bit process.
            /// </summary>
            X86,

            /// <summary>
            /// A 64-bit process.
            /// </summary>
            X64,
        }

        #endregion

        private const string SafeRegHandleTypeName =
            @"Microsoft.Win32.SafeHandles.SafeRegistryHandle";

        private const int KeyRead = 0x20019;
        private const int KeyWrite = 0x20006;
        private const int KeyWow6432Key = 0x0200;
        private const int KeyWow6464Key = 0x0100;

        private const BindingFlags PrivateInstanceFlags =
            BindingFlags.Instance | BindingFlags.NonPublic;

        private static readonly UIntPtr HKCR = (UIntPtr)0x80000000;
        private static readonly UIntPtr HKCU = (UIntPtr)0x80000001;
        private static readonly UIntPtr HKLM = (UIntPtr)0x80000002;
        private static readonly UIntPtr HKU = (UIntPtr)0x80000003;
        private static readonly UIntPtr HKPD = (UIntPtr)0x80000004;
        private static readonly UIntPtr HKCC = (UIntPtr)0x80000005;

        private static readonly Type SafeHandleType;
        private static readonly Assembly SafeHandleAssembly;
        private static readonly Type RegistryKeyType;

        /// <summary>
        /// The static constructor.
        /// </summary>
        static RegUtils()
        {
            SafeHandleType = typeof(SafeHandle);
            SafeHandleAssembly = SafeHandleType.Assembly;
            RegistryKeyType = typeof(RegistryKey);
        }

        /// <summary>
        /// Gets the current process architecture.
        /// </summary>
        public static ProcessArchitecture CurrentProcessArchitecture
        {
            get
            {
                switch (IntPtr.Size)
                {
                    case 8:
                        {
                            return ProcessArchitecture.X64;
                        }
                    default:
                        {
                            return ProcessArchitecture.X86;
                        }
                }
            }
        }

        /// <summary>Opens the specified registry key.</summary>
        /// <param name="hKey">A pointer one of the registry roots.</param>
        /// <param name="subKey">The path to the sub-key.</param>
        /// <param name="options">Always 0</param>
        /// <param name="sam">The access rights to the key.</param>
        /// <param name="phkResult">A handle to the opened key.</param>
        /// <returns>A zero-value if successful; a non-zero value if failed.</returns>
        /// <remarks>http://msdn.microsoft.com/en-us/library/aa965886(v=VS.85).aspx</remarks>
        [DllImport(@"advapi32.dll")]
        private static extern int RegOpenKeyEx(
            UIntPtr hKey,
            string subKey,
            uint options,
            int sam,
            out IntPtr phkResult);

        /// <summary>
        /// Opens a registry key as read-only in the hive of 
        /// the current process architecture.
        /// </summary>
        /// <param name="path">
        /// The path to the registry key. The root of the
        /// path should begin with one of the registry root
        /// acronyms, such as HKLM, HKCU, HKCC, etc.
        /// </param>
        /// <returns>
        /// A registry key for the given path or null if
        /// the path does not exist.
        /// </returns>
        public static RegistryKey OpenKey(string path)
        {
            return OpenKey(path, false);
        }

        /// <summary>
        /// Opens a registry key in the hive of the current
        /// process architecture.
        /// </summary>
        /// <param name="path">
        /// The path to the registry key. The root of the
        /// path should begin with one of the registry root
        /// acronyms, such as HKLM, HKCU, HKCC, etc.
        /// </param>
        /// <param name="writeable">
        /// Set to true to open the key as writeable.
        /// </param>
        /// <returns>
        /// A registry key for the given path or null if
        /// the path does not exist.
        /// </returns>
        public static RegistryKey OpenKey(string path, bool writeable)
        {
            return OpenKey(path, writeable, CurrentProcessArchitecture);
        }

        /// <summary>
        /// Opens a registry key.
        /// </summary>
        /// <param name="path">
        /// The path to the registry key. The root of the
        /// path should begin with one of the registry root
        /// acronyms, such as HKLM, HKCU, HKCC, etc.
        /// </param>
        /// <param name="writeable">
        /// Set to true to open the key as writeable.
        /// </param>
        /// <param name="hive">
        /// The process architecture of the hive to use.
        /// </param>
        /// <returns>
        /// A registry key for the given path or null if
        /// the path does not exist.
        /// </returns>
        public static RegistryKey OpenKey(
            string path, bool writeable, ProcessArchitecture hive)
        {
            UIntPtr regRootKey;
            path = ProcessRegPath(path, out regRootKey);

            if (regRootKey == UIntPtr.Zero)
            {
                return null;
            }

            var flags = KeyRead;

            if (writeable)
            {
                flags |= KeyWrite;
            }

            if (hive == ProcessArchitecture.X64)
            {
                flags |= KeyWow6464Key;
            }
            else
            {
                flags |= KeyWow6432Key;
            }

            IntPtr handle;
            var hresult = RegOpenKeyEx(regRootKey, path, 0, flags, out handle);

            if (hresult != 0)
            {
                return null;
            }

            var sh = CreateSafeHandle(handle);
            var key =
                (RegistryKey)
                    Activator.CreateInstance(
                        RegistryKeyType,
                        PrivateInstanceFlags,
                        null,
                        new object[] { sh, true },
                        null);
            return key;
        }

        /// <summary>
        /// Creates a safe handle from a dangerous one.
        /// </summary>
        /// <param name="handle">A dangerous handle</param>
        /// <returns>A safe handle.</returns>
        private static SafeHandle CreateSafeHandle(IntPtr handle)
        {
            var t = SafeHandleAssembly.GetType(SafeRegHandleTypeName);
            var sh =
                (SafeHandle)
                    Activator.CreateInstance(
                        t,
                        PrivateInstanceFlags,
                        null,
                        new object[] { handle, true },
                        null);
            return sh;
        }

        /// <summary>
        /// Processes a registry path and removes the root component and
        /// sets the rootRegKey ponter to a pointer to the root registry
        /// key that was specified in the given path.
        /// </summary>
        /// <param name="path">
        /// A registry path including the root.
        /// </param>
        /// <param name="rootRegKey">
        /// A pointer to the root registry key that will be parsed from the given path.
        /// </param>
        /// <returns>
        /// The given path without the root component.
        /// </returns>
        private static string ProcessRegPath(string path, out UIntPtr rootRegKey)
        {
            if (path.StartsWith(@"HKCR\"))
            {
                path = path.Replace(@"HKCR\", string.Empty);
                rootRegKey = HKCR;
            }
            else if (path.StartsWith(@"HKCU\"))
            {
                path = path.Replace(@"HKCU\", string.Empty);
                rootRegKey = HKCU;
            }
            else if (path.StartsWith(@"HKLM\"))
            {
                path = path.Replace(@"HKLM\", string.Empty);
                rootRegKey = HKLM;
            }
            else if (path.StartsWith(@"HKU\"))
            {
                path = path.Replace(@"HKU\", string.Empty);
                rootRegKey = HKU;
            }
            else if (path.StartsWith(@"HKCC\"))
            {
                path = path.Replace(@"HKCC\", string.Empty);
                rootRegKey = HKCC;
            }
            else if (path.StartsWith(@"HKPD\"))
            {
                path = path.Replace(@"HKPD\", string.Empty);
                rootRegKey = HKPD;
            }
            else
            {
                rootRegKey = UIntPtr.Zero;
            }

            return path;
        }
    }
}
