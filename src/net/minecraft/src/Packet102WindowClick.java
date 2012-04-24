package net.minecraft.src;

import java.io.*;

public class Packet102WindowClick extends Packet
{
    /** The id of the window which was clicked. 0 for player inventory. */
    public int window_Id;

    /** The clicked slot (-999 is outside of inventory) */
    public int inventorySlot;

    /** 1 when right-clicking and otherwise 0 */
    public int mouseClick;

    /** A unique number for the action, used for transaction handling */
    public short action;

    /** Item stack for inventory */
    public ItemStack itemStack;
    public boolean holdingShift;

    public Packet102WindowClick()
    {
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(NetHandler par1NetHandler)
    {
        par1NetHandler.handleWindowClick(this);
    }

    /**
     * Abstract. Reads the raw packet data from the data stream.
     */
    public void readPacketData(DataInputStream par1DataInputStream) throws IOException
    {
        window_Id = par1DataInputStream.readByte();
        inventorySlot = par1DataInputStream.readShort();
        mouseClick = par1DataInputStream.readByte();
        action = par1DataInputStream.readShort();
        holdingShift = par1DataInputStream.readBoolean();
        itemStack = readItemStack(par1DataInputStream);
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    public void writePacketData(DataOutputStream par1DataOutputStream) throws IOException
    {
        par1DataOutputStream.writeByte(window_Id);
        par1DataOutputStream.writeShort(inventorySlot);
        par1DataOutputStream.writeByte(mouseClick);
        par1DataOutputStream.writeShort(action);
        par1DataOutputStream.writeBoolean(holdingShift);
        writeItemStack(itemStack, par1DataOutputStream);
    }

    /**
     * Abstract. Return the size of the packet (not counting the header).
     */
    public int getPacketSize()
    {
        return 11;
    }
}
