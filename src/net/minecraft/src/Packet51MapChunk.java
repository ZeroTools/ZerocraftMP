package net.minecraft.src;

import java.io.*;
import java.util.zip.*;

public class Packet51MapChunk extends Packet
{
    /** The x-position of the transmitted chunk, in chunk coordinates. */
    public int xCh;

    /** The z-position of the transmitted chunk, in chunk coordinates. */
    public int zCh;

    /**
     * The y-position of the lowest chunk Section in the transmitted chunk, in chunk coordinates.
     */
    public int yChMin;

    /**
     * The y-position of the highest chunk Section in the transmitted chunk, in chunk coordinates.
     */
    public int yChMax;
    public byte chunkData[];

    /**
     * Whether to initialize the Chunk before applying the effect of the Packet51MapChunk.
     */
    public boolean includeInitialize;

    /** The length of the compressed chunk data byte array. */
    private int tempLength;
    private int field_48110_h;
    private static byte temp[] = new byte[0];

    public Packet51MapChunk()
    {
        isChunkDataPacket = true;
    }

    public Packet51MapChunk(Chunk par1Chunk, boolean par2, int par3)
    {
        isChunkDataPacket = true;
        xCh = par1Chunk.xPosition;
        zCh = par1Chunk.zPosition;
        includeInitialize = par2;

        if (par2)
        {
            par3 = 65535;
            par1Chunk.field_50025_o = true;
        }

        ExtendedBlockStorage aextendedblockstorage[] = par1Chunk.getBlockStorageArray();
        int i = 0;
        int j = 0;

        for (int k = 0; k < aextendedblockstorage.length; k++)
        {
            if (aextendedblockstorage[k] == null || par2 && aextendedblockstorage[k].getIsEmpty() || (par3 & 1 << k) == 0)
            {
                continue;
            }

            yChMin |= 1 << k;
            i++;

            if (aextendedblockstorage[k].getBlockMSBArray() != null)
            {
                yChMax |= 1 << k;
                j++;
            }
        }

        int l = 2048 * (5 * i + j);

        if (par2)
        {
            l += 256;
        }

        if (temp.length < l)
        {
            temp = new byte[l];
        }

        byte abyte0[] = temp;
        int i1 = 0;

        for (int j1 = 0; j1 < aextendedblockstorage.length; j1++)
        {
            if (aextendedblockstorage[j1] != null && (!par2 || !aextendedblockstorage[j1].getIsEmpty()) && (par3 & 1 << j1) != 0)
            {
                byte abyte2[] = aextendedblockstorage[j1].func_48590_g();
                System.arraycopy(abyte2, 0, abyte0, i1, abyte2.length);
                i1 += abyte2.length;
            }
        }

        for (int k1 = 0; k1 < aextendedblockstorage.length; k1++)
        {
            if (aextendedblockstorage[k1] != null && (!par2 || !aextendedblockstorage[k1].getIsEmpty()) && (par3 & 1 << k1) != 0)
            {
                NibbleArray nibblearray = aextendedblockstorage[k1].func_48594_i();
                System.arraycopy(nibblearray.data, 0, abyte0, i1, nibblearray.data.length);
                i1 += nibblearray.data.length;
            }
        }

        for (int l1 = 0; l1 < aextendedblockstorage.length; l1++)
        {
            if (aextendedblockstorage[l1] != null && (!par2 || !aextendedblockstorage[l1].getIsEmpty()) && (par3 & 1 << l1) != 0)
            {
                NibbleArray nibblearray1 = aextendedblockstorage[l1].getBlocklightArray();
                System.arraycopy(nibblearray1.data, 0, abyte0, i1, nibblearray1.data.length);
                i1 += nibblearray1.data.length;
            }
        }

        for (int i2 = 0; i2 < aextendedblockstorage.length; i2++)
        {
            if (aextendedblockstorage[i2] != null && (!par2 || !aextendedblockstorage[i2].getIsEmpty()) && (par3 & 1 << i2) != 0)
            {
                NibbleArray nibblearray2 = aextendedblockstorage[i2].getSkylightArray();
                System.arraycopy(nibblearray2.data, 0, abyte0, i1, nibblearray2.data.length);
                i1 += nibblearray2.data.length;
            }
        }

        if (j > 0)
        {
            for (int j2 = 0; j2 < aextendedblockstorage.length; j2++)
            {
                if (aextendedblockstorage[j2] != null && (!par2 || !aextendedblockstorage[j2].getIsEmpty()) && aextendedblockstorage[j2].getBlockMSBArray() != null && (par3 & 1 << j2) != 0)
                {
                    NibbleArray nibblearray3 = aextendedblockstorage[j2].getBlockMSBArray();
                    System.arraycopy(nibblearray3.data, 0, abyte0, i1, nibblearray3.data.length);
                    i1 += nibblearray3.data.length;
                }
            }
        }

        if (par2)
        {
            byte abyte1[] = par1Chunk.getBiomeArray();
            System.arraycopy(abyte1, 0, abyte0, i1, abyte1.length);
            i1 += abyte1.length;
        }

        Deflater deflater = new Deflater(-1);

        try
        {
            deflater.setInput(abyte0, 0, i1);
            deflater.finish();
            chunkData = new byte[i1];
            tempLength = deflater.deflate(chunkData);
        }
        finally
        {
            deflater.end();
        }
    }

    /**
     * Abstract. Reads the raw packet data from the data stream.
     */
    public void readPacketData(DataInputStream par1DataInputStream) throws IOException
    {
        xCh = par1DataInputStream.readInt();
        zCh = par1DataInputStream.readInt();
        includeInitialize = par1DataInputStream.readBoolean();
        yChMin = par1DataInputStream.readShort();
        yChMax = par1DataInputStream.readShort();
        tempLength = par1DataInputStream.readInt();
        field_48110_h = par1DataInputStream.readInt();

        if (temp.length < tempLength)
        {
            temp = new byte[tempLength];
        }

        par1DataInputStream.readFully(temp, 0, tempLength);
        int i = 0;

        for (int j = 0; j < 16; j++)
        {
            i += yChMin >> j & 1;
        }

        int k = 12288 * i;

        if (includeInitialize)
        {
            k += 256;
        }

        chunkData = new byte[k];
        Inflater inflater = new Inflater();
        inflater.setInput(temp, 0, tempLength);

        try
        {
            inflater.inflate(chunkData);
        }
        catch (DataFormatException dataformatexception)
        {
            throw new IOException("Bad compressed data format");
        }
        finally
        {
            inflater.end();
        }
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    public void writePacketData(DataOutputStream par1DataOutputStream) throws IOException
    {
        par1DataOutputStream.writeInt(xCh);
        par1DataOutputStream.writeInt(zCh);
        par1DataOutputStream.writeBoolean(includeInitialize);
        par1DataOutputStream.writeShort((short)(yChMin & 0xffff));
        par1DataOutputStream.writeShort((short)(yChMax & 0xffff));
        par1DataOutputStream.writeInt(tempLength);
        par1DataOutputStream.writeInt(field_48110_h);
        par1DataOutputStream.write(chunkData, 0, tempLength);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(NetHandler par1NetHandler)
    {
        par1NetHandler.func_48070_a(this);
    }

    /**
     * Abstract. Return the size of the packet (not counting the header).
     */
    public int getPacketSize()
    {
        return 17 + tempLength;
    }
}
