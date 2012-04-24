package net.minecraft.src;

import java.io.*;

public class Packet52MultiBlockChange extends Packet
{
    /** Chunk X position. */
    public int xPosition;

    /** Chunk Z position. */
    public int zPosition;
    public byte metadataArray[];

    /** The size of the arrays. */
    public int size;
    private static byte field_48123_e[] = new byte[0];

    public Packet52MultiBlockChange()
    {
        isChunkDataPacket = true;
    }

    public Packet52MultiBlockChange(int par1, int par2, short par3ArrayOfShort[], int par4, World par5World)
    {
        isChunkDataPacket = true;
        xPosition = par1;
        zPosition = par2;
        size = par4;
        int i = 4 * par4;
        Chunk chunk = par5World.getChunkFromChunkCoords(par1, par2);

        try
        {
            if (par4 >= 64)
            {
                System.out.println((new StringBuilder()).append("ChunkTilesUpdatePacket compress ").append(par4).toString());

                if (field_48123_e.length < i)
                {
                    field_48123_e = new byte[i];
                }
            }
            else
            {
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(i);
                DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);

                for (int j = 0; j < par4; j++)
                {
                    int k = par3ArrayOfShort[j] >> 12 & 0xf;
                    int l = par3ArrayOfShort[j] >> 8 & 0xf;
                    int i1 = par3ArrayOfShort[j] & 0xff;
                    dataoutputstream.writeShort(par3ArrayOfShort[j]);
                    dataoutputstream.writeShort((short)((chunk.getBlockID(k, i1, l) & 0xfff) << 4 | chunk.getBlockMetadata(k, i1, l) & 0xf));
                }

                metadataArray = bytearrayoutputstream.toByteArray();

                if (metadataArray.length != i)
                {
                    throw new RuntimeException((new StringBuilder()).append("Expected length ").append(i).append(" doesn't match received length ").append(metadataArray.length).toString());
                }
            }
        }
        catch (IOException ioexception)
        {
            System.err.println(ioexception.getMessage());
            metadataArray = null;
        }
    }

    /**
     * Abstract. Reads the raw packet data from the data stream.
     */
    public void readPacketData(DataInputStream par1DataInputStream) throws IOException
    {
        xPosition = par1DataInputStream.readInt();
        zPosition = par1DataInputStream.readInt();
        size = par1DataInputStream.readShort() & 0xffff;
        int i = par1DataInputStream.readInt();

        if (i > 0)
        {
            metadataArray = new byte[i];
            par1DataInputStream.readFully(metadataArray);
        }
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    public void writePacketData(DataOutputStream par1DataOutputStream) throws IOException
    {
        par1DataOutputStream.writeInt(xPosition);
        par1DataOutputStream.writeInt(zPosition);
        par1DataOutputStream.writeShort((short)size);

        if (metadataArray != null)
        {
            par1DataOutputStream.writeInt(metadataArray.length);
            par1DataOutputStream.write(metadataArray);
        }
        else
        {
            par1DataOutputStream.writeInt(0);
        }
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(NetHandler par1NetHandler)
    {
        par1NetHandler.handleMultiBlockChange(this);
    }

    /**
     * Abstract. Return the size of the packet (not counting the header).
     */
    public int getPacketSize()
    {
        return 10 + size * 4;
    }
}
