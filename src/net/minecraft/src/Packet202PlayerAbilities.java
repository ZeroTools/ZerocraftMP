package net.minecraft.src;

import java.io.*;

public class Packet202PlayerAbilities extends Packet
{
    public boolean field_50007_a;
    public boolean field_50005_b;
    public boolean field_50006_c;
    public boolean field_50004_d;

    public Packet202PlayerAbilities()
    {
        field_50007_a = false;
        field_50005_b = false;
        field_50006_c = false;
        field_50004_d = false;
    }

    public Packet202PlayerAbilities(PlayerCapabilities par1PlayerCapabilities)
    {
        field_50007_a = false;
        field_50005_b = false;
        field_50006_c = false;
        field_50004_d = false;
        field_50007_a = par1PlayerCapabilities.disableDamage;
        field_50005_b = par1PlayerCapabilities.isFlying;
        field_50006_c = par1PlayerCapabilities.allowFlying;
        field_50004_d = par1PlayerCapabilities.isCreativeMode;
    }

    /**
     * Abstract. Reads the raw packet data from the data stream.
     */
    public void readPacketData(DataInputStream par1DataInputStream) throws IOException
    {
        field_50007_a = par1DataInputStream.readBoolean();
        field_50005_b = par1DataInputStream.readBoolean();
        field_50006_c = par1DataInputStream.readBoolean();
        field_50004_d = par1DataInputStream.readBoolean();
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    public void writePacketData(DataOutputStream par1DataOutputStream) throws IOException
    {
        par1DataOutputStream.writeBoolean(field_50007_a);
        par1DataOutputStream.writeBoolean(field_50005_b);
        par1DataOutputStream.writeBoolean(field_50006_c);
        par1DataOutputStream.writeBoolean(field_50004_d);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(NetHandler par1NetHandler)
    {
        par1NetHandler.func_50003_a(this);
    }

    /**
     * Abstract. Return the size of the packet (not counting the header).
     */
    public int getPacketSize()
    {
        return 1;
    }
}
