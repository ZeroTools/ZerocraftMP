package net.minecraft.src;

public class ItemCloth extends ItemBlock
{
    public ItemCloth(int par1)
    {
        super(par1);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    /**
     * Returns the metadata of the block which this Item (ItemBlock) can place
     */
    public int getMetadata(int par1)
    {
        return par1;
    }

    public String getItemNameIS(ItemStack par1ItemStack)
    {
        return (new StringBuilder()).append(super.getItemName()).append(".").append(ItemDye.dyeColorNames[BlockCloth.getBlockFromDye(par1ItemStack.getItemDamage())]).toString();
    }
}
