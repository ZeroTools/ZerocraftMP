package net.minecraft.src;

public class ItemInWorldManager
{
    /** The world object that this object is connected to. */
    public World thisWorld;

    /** The player that this object references. */
    public EntityPlayer thisPlayer;

    /** The game mode, 1 for creative, 0 for survival. */
    private int gameType;
    private float field_672_d;
    private int initialDamage;
    private int curBlockX;
    private int curBlockY;
    private int curBlockZ;
    private int curblockDamage;
    private boolean field_22050_k;
    private int field_22049_l;
    private int field_22048_m;
    private int field_22047_n;
    private int field_22046_o;

    public ItemInWorldManager(World par1World)
    {
        gameType = -1;
        field_672_d = 0.0F;
        thisWorld = par1World;
    }

    public void toggleGameType(int par1)
    {
        gameType = par1;

        if (par1 == 0)
        {
            thisPlayer.capabilities.allowFlying = false;
            thisPlayer.capabilities.isFlying = false;
            thisPlayer.capabilities.isCreativeMode = false;
            thisPlayer.capabilities.disableDamage = false;
        }
        else
        {
            thisPlayer.capabilities.allowFlying = true;
            thisPlayer.capabilities.isCreativeMode = true;
            thisPlayer.capabilities.disableDamage = true;
        }

        thisPlayer.func_50022_L();
    }

    public int getGameType()
    {
        return gameType;
    }

    /**
     * Get if we are in creative game mode.
     */
    public boolean isCreative()
    {
        return gameType == 1;
    }

    public void func_35695_b(int par1)
    {
        if (gameType == -1)
        {
            gameType = par1;
        }

        toggleGameType(gameType);
    }

    public void updateBlockRemoving()
    {
        curblockDamage++;

        if (field_22050_k)
        {
            int i = curblockDamage - field_22046_o;
            int j = thisWorld.getBlockId(field_22049_l, field_22048_m, field_22047_n);

            if (j != 0)
            {
                Block block = Block.blocksList[j];
                float f = block.blockStrength(thisPlayer) * (float)(i + 1);

                if (f >= 1.0F)
                {
                    field_22050_k = false;
                    blockHarvessted(field_22049_l, field_22048_m, field_22047_n);
                }
            }
            else
            {
                field_22050_k = false;
            }
        }
    }

    public void blockClicked(int par1, int par2, int par3, int par4)
    {
        if (isCreative())
        {
            if (!thisWorld.func_48093_a(null, par1, par2, par3, par4))
            {
                blockHarvessted(par1, par2, par3);
            }

            return;
        }

        thisWorld.func_48093_a(null, par1, par2, par3, par4);
        initialDamage = curblockDamage;
        int i = thisWorld.getBlockId(par1, par2, par3);

        if (i > 0)
        {
            Block.blocksList[i].onBlockClicked(thisWorld, par1, par2, par3, thisPlayer);
        }

        if (i > 0 && Block.blocksList[i].blockStrength(thisPlayer) >= 1.0F)
        {
            blockHarvessted(par1, par2, par3);
        }
        else
        {
            curBlockX = par1;
            curBlockY = par2;
            curBlockZ = par3;
        }
    }

    public void blockRemoving(int par1, int par2, int par3)
    {
        if (par1 == curBlockX && par2 == curBlockY && par3 == curBlockZ)
        {
            int i = curblockDamage - initialDamage;
            int j = thisWorld.getBlockId(par1, par2, par3);

            if (j != 0)
            {
                Block block = Block.blocksList[j];
                float f = block.blockStrength(thisPlayer) * (float)(i + 1);

                if (f >= 0.7F)
                {
                    blockHarvessted(par1, par2, par3);
                }
                else if (!field_22050_k)
                {
                    field_22050_k = true;
                    field_22049_l = par1;
                    field_22048_m = par2;
                    field_22047_n = par3;
                    field_22046_o = initialDamage;
                }
            }
        }

        field_672_d = 0.0F;
    }

    /**
     * Removes a block and triggers the appropriate events
     */
    public boolean removeBlock(int par1, int par2, int par3)
    {
        Block block = Block.blocksList[thisWorld.getBlockId(par1, par2, par3)];
        int i = thisWorld.getBlockMetadata(par1, par2, par3);
        boolean flag = thisWorld.setBlockWithNotify(par1, par2, par3, 0);

        if (block != null && flag)
        {
            block.onBlockDestroyedByPlayer(thisWorld, par1, par2, par3, i);
        }

        return flag;
    }

    public boolean blockHarvessted(int par1, int par2, int par3)
    {
        int i = thisWorld.getBlockId(par1, par2, par3);
        int j = thisWorld.getBlockMetadata(par1, par2, par3);
        thisWorld.playAuxSFXAtEntity(thisPlayer, 2001, par1, par2, par3, i + (thisWorld.getBlockMetadata(par1, par2, par3) << 12));
        boolean flag = removeBlock(par1, par2, par3);

        if (isCreative())
        {
            ((EntityPlayerMP)thisPlayer).playerNetServerHandler.sendPacket(new Packet53BlockChange(par1, par2, par3, thisWorld));
        }
        else
        {
            ItemStack itemstack = thisPlayer.getCurrentEquippedItem();
            boolean flag1 = thisPlayer.canHarvestBlock(Block.blocksList[i]);

            if (itemstack != null)
            {
                itemstack.onDestroyBlock(i, par1, par2, par3, thisPlayer);

                if (itemstack.stackSize == 0)
                {
                    itemstack.onItemDestroyedByUse(thisPlayer);
                    thisPlayer.destroyCurrentEquippedItem();
                }
            }

            if (flag && flag1)
            {
                Block.blocksList[i].harvestBlock(thisWorld, thisPlayer, par1, par2, par3, j);
            }
        }

        return flag;
    }

    public boolean itemUsed(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack)
    {
        int i = par3ItemStack.stackSize;
        int j = par3ItemStack.getItemDamage();
        ItemStack itemstack = par3ItemStack.useItemRightClick(par2World, par1EntityPlayer);

        if (itemstack != par3ItemStack || itemstack != null && itemstack.stackSize != i || itemstack != null && itemstack.getMaxItemUseDuration() > 0)
        {
            par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = itemstack;

            if (isCreative())
            {
                itemstack.stackSize = i;
                itemstack.setItemDamage(j);
            }

            if (itemstack.stackSize == 0)
            {
                par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = null;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Will either active a block (if there is one at the given location), otherwise will try to use the item being hold
     */
    public boolean activeBlockOrUseItem(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack, int par4, int par5, int par6, int par7)
    {
        int i = par2World.getBlockId(par4, par5, par6);

        if (i > 0 && Block.blocksList[i].blockActivated(par2World, par4, par5, par6, par1EntityPlayer))
        {
            return true;
        }

        if (par3ItemStack == null)
        {
            return false;
        }

        if (isCreative())
        {
            int j = par3ItemStack.getItemDamage();
            int k = par3ItemStack.stackSize;
            boolean flag = par3ItemStack.useItem(par1EntityPlayer, par2World, par4, par5, par6, par7);
            par3ItemStack.setItemDamage(j);
            par3ItemStack.stackSize = k;
            return flag;
        }
        else
        {
            return par3ItemStack.useItem(par1EntityPlayer, par2World, par4, par5, par6, par7);
        }
    }

    /**
     * Sets the world instance.
     */
    public void setWorld(WorldServer par1WorldServer)
    {
        thisWorld = par1WorldServer;
    }
}
