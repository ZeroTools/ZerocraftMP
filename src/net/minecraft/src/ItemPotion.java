package net.minecraft.src;

import java.util.*;

public class ItemPotion extends Item
{
    /**
     * Contains a map from integers to the list of potion effects that potions with that damage value confer (to prevent
     * recalculating it).
     */
    private HashMap effectCache;

    public ItemPotion(int par1)
    {
        super(par1);
        effectCache = new HashMap();
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    /**
     * Returns a list of potion effects for the specified itemstack.
     */
    public List getEffects(ItemStack par1ItemStack)
    {
        return getEffects(par1ItemStack.getItemDamage());
    }

    /**
     * Returns a list of effects for the specified potion damage value.
     */
    public List getEffects(int par1)
    {
        List list = (List)effectCache.get(Integer.valueOf(par1));

        if (list == null)
        {
            list = PotionHelper.getPotionEffects(par1, false);
            effectCache.put(Integer.valueOf(par1), list);
        }

        return list;
    }

    public ItemStack onFoodEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        par1ItemStack.stackSize--;

        if (!par2World.isRemote)
        {
            List list = getEffects(par1ItemStack);

            if (list != null)
            {
                PotionEffect potioneffect;

                for (Iterator iterator = list.iterator(); iterator.hasNext(); par3EntityPlayer.addPotionEffect(new PotionEffect(potioneffect)))
                {
                    potioneffect = (PotionEffect)iterator.next();
                }
            }
        }

        if (par1ItemStack.stackSize <= 0)
        {
            return new ItemStack(Item.glassBottle);
        }
        else
        {
            par3EntityPlayer.inventory.addItemStackToInventory(new ItemStack(Item.glassBottle));
            return par1ItemStack;
        }
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 32;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.drink;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        if (isSplash(par1ItemStack.getItemDamage()))
        {
            par1ItemStack.stackSize--;
            par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if (!par2World.isRemote)
            {
                par2World.spawnEntityInWorld(new EntityPotion(par2World, par3EntityPlayer, par1ItemStack.getItemDamage()));
            }

            return par1ItemStack;
        }
        else
        {
            par3EntityPlayer.setItemInUse(par1ItemStack, getMaxItemUseDuration(par1ItemStack));
            return par1ItemStack;
        }
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS !
     */
    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int i, int j, int k, int l)
    {
        return false;
    }

    /**
     * returns wether or not a potion is a throwable splash potion based on damage value
     */
    public static boolean isSplash(int par0)
    {
        return (par0 & 0x4000) != 0;
    }
}
