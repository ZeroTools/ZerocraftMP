package net.minecraft.src;

public class RConUtils
{
    public static char hexDigits[] =
    {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f'
    };

    public RConUtils()
    {
    }

    /**
     * Read a null-terminated string from the given byte array
     */
    public static String getBytesAsString(byte par0ArrayOfByte[], int par1, int par2)
    {
        int i = par2 - 1;
        int j;

        for (j = par1 <= i ? par1 : i; 0 != par0ArrayOfByte[j] && j < i; j++) { }

        return new String(par0ArrayOfByte, par1, j - par1);
    }

    /**
     * Read 4 bytes from the
     */
    public static int getRemainingBytesAsLEInt(byte par0ArrayOfByte[], int par1)
    {
        return getBytesAsLEInt(par0ArrayOfByte, par1, par0ArrayOfByte.length);
    }

    /**
     * Read 4 bytes from the given array in little-endian format and return them as an int
     */
    public static int getBytesAsLEInt(byte par0ArrayOfByte[], int par1, int par2)
    {
        if (0 > par2 - par1 - 4)
        {
            return 0;
        }
        else
        {
            return par0ArrayOfByte[par1 + 3] << 24 | (par0ArrayOfByte[par1 + 2] & 0xff) << 16 | (par0ArrayOfByte[par1 + 1] & 0xff) << 8 | par0ArrayOfByte[par1] & 0xff;
        }
    }

    /**
     * Read 4 bytes from the given array in big-endian format and return them as an int
     */
    public static int getBytesAsBEint(byte par0ArrayOfByte[], int par1, int par2)
    {
        if (0 > par2 - par1 - 4)
        {
            return 0;
        }
        else
        {
            return par0ArrayOfByte[par1] << 24 | (par0ArrayOfByte[par1 + 1] & 0xff) << 16 | (par0ArrayOfByte[par1 + 2] & 0xff) << 8 | par0ArrayOfByte[par1 + 3] & 0xff;
        }
    }

    /**
     * Returns a String representation of the byte in hexadecimal format
     */
    public static String getByteAsHexString(byte par0)
    {
        return (new StringBuilder()).append("").append(hexDigits[(par0 & 0xf0) >>> 4]).append(hexDigits[par0 & 0xf]).toString();
    }
}
