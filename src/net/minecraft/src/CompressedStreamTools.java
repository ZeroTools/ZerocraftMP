package net.minecraft.src;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedStreamTools
{
    public CompressedStreamTools()
    {
    }

    /**
     * Load the gzipped compound from the inputstream.
     */
    public static NBTTagCompound readCompressed(InputStream par0InputStream) throws IOException
    {
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(par0InputStream)));

        try
        {
            NBTTagCompound nbttagcompound = read(datainputstream);
            return nbttagcompound;
        }
        finally
        {
            datainputstream.close();
        }
    }

    /**
     * Write the compound, gzipped, to the outputstream.
     */
    public static void writeCompressed(NBTTagCompound par0NBTTagCompound, OutputStream par1OutputStream) throws IOException
    {
        DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(par1OutputStream));

        try
        {
            write(par0NBTTagCompound, dataoutputstream);
        }
        finally
        {
            dataoutputstream.close();
        }
    }

    public static NBTTagCompound decompress(byte par0ArrayOfByte[]) throws IOException
    {
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(par0ArrayOfByte))));

        try
        {
            NBTTagCompound nbttagcompound = read(datainputstream);
            return nbttagcompound;
        }
        finally
        {
            datainputstream.close();
        }
    }

    public static byte[] compress(NBTTagCompound par0NBTTagCompound) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));

        try
        {
            write(par0NBTTagCompound, dataoutputstream);
        }
        finally
        {
            dataoutputstream.close();
        }

        return bytearrayoutputstream.toByteArray();
    }

    /**
     * Reads from a CompressedStream.
     */
    public static NBTTagCompound read(DataInput par0DataInput) throws IOException
    {
        NBTBase nbtbase = NBTBase.readNamedTag(par0DataInput);

        if (nbtbase instanceof NBTTagCompound)
        {
            return (NBTTagCompound)nbtbase;
        }
        else
        {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(NBTTagCompound par0NBTTagCompound, DataOutput par1DataOutput) throws IOException
    {
        NBTBase.writeNamedTag(par0NBTTagCompound, par1DataOutput);
    }
}
