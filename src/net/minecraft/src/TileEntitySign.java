package net.minecraft.src;

public class TileEntitySign extends TileEntity
{
    public String signText[] =
    {
        "", "", "", ""
    };

    /**
     * The index of the line currently being edited. Only used on client side, but defined on both. Note this is only
     * really used when the > < are going to be visible.
     */
    public int lineBeingEdited;
    private boolean isEditable;

    public TileEntitySign()
    {
        lineBeingEdited = -1;
        isEditable = true;
    }

    /**
     * Writes a tile entity to NBT.
     */
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setString("Text1", signText[0]);
        par1NBTTagCompound.setString("Text2", signText[1]);
        par1NBTTagCompound.setString("Text3", signText[2]);
        par1NBTTagCompound.setString("Text4", signText[3]);
    }

    /**
     * Reads a tile entity from NBT.
     */
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        isEditable = false;
        super.readFromNBT(par1NBTTagCompound);

        for (int i = 0; i < 4; i++)
        {
            signText[i] = par1NBTTagCompound.getString((new StringBuilder()).append("Text").append(i + 1).toString());

            if (signText[i].length() > 15)
            {
                signText[i] = signText[i].substring(0, 15);
            }
        }
    }

    /**
     * Overriden in a sign to provide the text
     */
    public Packet getDescriptionPacket()
    {
        String as[] = new String[4];

        for (int i = 0; i < 4; i++)
        {
            as[i] = signText[i];
        }

        return new Packet130UpdateSign(xCoord, yCoord, zCoord, as);
    }

    public boolean isEditable()
    {
        return isEditable;
    }
}
