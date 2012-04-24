package net.minecraft.src;

import java.util.*;
import net.minecraft.server.MinecraftServer;

public class EntityPlayerMP extends EntityPlayer implements ICrafting
{
    /** The NetServerHandler for this particular player. */
    public NetServerHandler playerNetServerHandler;

    /** Reference to the MinecraftServer object. */
    public MinecraftServer mcServer;

    /** The ItemInWorldManager belonging to this player */
    public ItemInWorldManager itemInWorldManager;

    /** player X position as seen by PlayerManager */
    public double managedPosX;

    /** player Z position as seen by PlayerManager */
    public double managedPosZ;

    /** LinkedList that holds loaded chunks */
    public List loadedChunks;

    /**
     * set of ChunkCoordIntPair, represents blocks that the player will receive block updates from
     */
    public Set listeningChunks;

    /** amount of health the client was last set to */
    private int lastHealth;
    private int field_35221_cc;
    private boolean field_35222_cd;

    /** Amount of experience the client was last set to */
    private int lastExperience;

    /** how many ticks of invulnerability(spawn protection) this player has */
    private int ticksOfInvuln;
    private ItemStack playerInventory[] =
    {
        null, null, null, null, null
    };

    /** the currently in use window id */
    private int currentWindowId;

    /**
     * set to true when player is moving quantity of items from one inventory to another(crafting) but item in either
     * slot is not changed
     */
    public boolean isChangingQuantityOnly;
    public int ping;

    /**
     * Set when a player beats the ender dragon, used to determine whether a Packet9Respawn is valid.
     */
    public boolean gameOver;

    public EntityPlayerMP(MinecraftServer par1MinecraftServer, World par2World, String par3Str, ItemInWorldManager par4ItemInWorldManager)
    {
        super(par2World);
        loadedChunks = new LinkedList();
        listeningChunks = new HashSet();
        lastHealth = 0xfa0a1f01;
        field_35221_cc = 0xfa0a1f01;
        field_35222_cd = true;
        lastExperience = 0xfa0a1f01;
        ticksOfInvuln = 60;
        currentWindowId = 0;
        gameOver = false;
        par4ItemInWorldManager.thisPlayer = this;
        itemInWorldManager = par4ItemInWorldManager;
        ChunkCoordinates chunkcoordinates = par2World.getSpawnPoint();
        int i = chunkcoordinates.posX;
        int j = chunkcoordinates.posZ;
        int k = chunkcoordinates.posY;

        if (!par2World.worldProvider.hasNoSky)
        {
            i += rand.nextInt(20) - 10;
            k = par2World.getTopSolidOrLiquidBlock(i, j);
            j += rand.nextInt(20) - 10;
        }

        setLocationAndAngles((double)i + 0.5D, k, (double)j + 0.5D, 0.0F, 0.0F);
        mcServer = par1MinecraftServer;
        stepHeight = 0.0F;
        username = par3Str;
        yOffset = 0.0F;
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);

        if (par1NBTTagCompound.hasKey("playerGameType"))
        {
            itemInWorldManager.toggleGameType(par1NBTTagCompound.getInteger("playerGameType"));
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("playerGameType", itemInWorldManager.getGameType());
    }

    /**
     * Sets the reference to the World object.
     */
    public void setWorld(World par1World)
    {
        super.setWorld(par1World);
    }

    /**
     * Removes the specified number of experience levels.
     */
    public void removeExperience(int par1)
    {
        super.removeExperience(par1);
        lastExperience = -1;
    }

    public void func_20057_k()
    {
        craftingInventory.onCraftGuiOpened(this);
    }

    /**
     * returns the inventory of this entity (only used in EntityPlayerMP it seems)
     */
    public ItemStack[] getInventory()
    {
        return playerInventory;
    }

    /**
     * sets the players height back to normal after doing things like sleeping and dieing
     */
    protected void resetHeight()
    {
        yOffset = 0.0F;
    }

    public float getEyeHeight()
    {
        return 1.62F;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        itemInWorldManager.updateBlockRemoving();
        ticksOfInvuln--;
        craftingInventory.updateCraftingResults();

        for (int i = 0; i < 5; i++)
        {
            ItemStack itemstack = getEquipmentInSlot(i);

            if (itemstack != playerInventory[i])
            {
                mcServer.getEntityTracker(dimension).sendPacketToTrackedPlayers(this, new Packet5PlayerInventory(entityId, i, itemstack));
                playerInventory[i] = itemstack;
            }
        }
    }

    /**
     * 0: Tool in Hand; 1-4: Armor
     */
    public ItemStack getEquipmentInSlot(int par1)
    {
        if (par1 == 0)
        {
            return inventory.getCurrentItem();
        }
        else
        {
            return inventory.armorInventory[par1 - 1];
        }
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource par1DamageSource)
    {
        mcServer.configManager.sendPacketToAllPlayers(new Packet3Chat(par1DamageSource.getDeathMessage(this)));
        inventory.dropAllItems();
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
    {
        if (ticksOfInvuln > 0)
        {
            return false;
        }

        if (!mcServer.pvpOn && (par1DamageSource instanceof EntityDamageSource))
        {
            Entity entity = par1DamageSource.getEntity();

            if (entity instanceof EntityPlayer)
            {
                return false;
            }

            if (entity instanceof EntityArrow)
            {
                EntityArrow entityarrow = (EntityArrow)entity;

                if (entityarrow.shootingEntity instanceof EntityPlayer)
                {
                    return false;
                }
            }
        }

        return super.attackEntityFrom(par1DamageSource, par2);
    }

    /**
     * returns if pvp is enabled or not
     */
    protected boolean isPVPEnabled()
    {
        return mcServer.pvpOn;
    }

    /**
     * Heal living entity (param: amount of half-hearts)
     */
    public void heal(int par1)
    {
        super.heal(par1);
    }

    /**
     * updates the player, also has a flag to tell the server if it should update chunks or not(wanted onUpdate(because
     * this calls super.onUpdate) but already one in this class)
     */
    public void onUpdateEntity(boolean par1)
    {
        super.onUpdate();

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (itemstack == null || !Item.itemsList[itemstack.itemID].func_28019_b() || playerNetServerHandler.getNumChunkDataPackets() > 2)
            {
                continue;
            }

            Packet packet = ((ItemMapBase)Item.itemsList[itemstack.itemID]).getUpdatePacket(itemstack, worldObj, this);

            if (packet != null)
            {
                playerNetServerHandler.sendPacket(packet);
            }
        }

        if (par1 && !loadedChunks.isEmpty())
        {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)loadedChunks.get(0);
            double d = chunkcoordintpair.func_48477_a(this);

            for (int j = 0; j < loadedChunks.size(); j++)
            {
                ChunkCoordIntPair chunkcoordintpair1 = (ChunkCoordIntPair)loadedChunks.get(j);
                double d1 = chunkcoordintpair1.func_48477_a(this);

                if (d1 < d)
                {
                    chunkcoordintpair = chunkcoordintpair1;
                    d = d1;
                }
            }

            if (chunkcoordintpair != null)
            {
                boolean flag = false;

                if (playerNetServerHandler.getNumChunkDataPackets() < 4)
                {
                    flag = true;
                }

                if (flag)
                {
                    WorldServer worldserver = mcServer.getWorldManager(dimension);

                    if (worldserver.blockExists(chunkcoordintpair.chunkXPos << 4, 0, chunkcoordintpair.chunkZPos << 4))
                    {
                        Chunk chunk = worldserver.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);

                        if (chunk.isTerrainPopulated)
                        {
                            loadedChunks.remove(chunkcoordintpair);
                            playerNetServerHandler.sendPacket(new Packet51MapChunk(worldserver.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos), true, 0));
                            List list = worldserver.getTileEntityList(chunkcoordintpair.chunkXPos * 16, 0, chunkcoordintpair.chunkZPos * 16, chunkcoordintpair.chunkXPos * 16 + 16, 256, chunkcoordintpair.chunkZPos * 16 + 16);

                            for (int k = 0; k < list.size(); k++)
                            {
                                getTileEntityInfo((TileEntity)list.get(k));
                            }
                        }
                    }
                }
            }
        }

        if (inPortal)
        {
            if (mcServer.propertyManagerObj.getBooleanProperty("allow-nether", true))
            {
                if (craftingInventory != inventorySlots)
                {
                    closeScreen();
                }

                if (ridingEntity != null)
                {
                    mountEntity(ridingEntity);
                }
                else
                {
                    timeInPortal += 0.0125F;

                    if (timeInPortal >= 1.0F)
                    {
                        timeInPortal = 1.0F;
                        timeUntilPortal = 10;
                        byte byte0 = 0;

                        if (dimension == -1)
                        {
                            byte0 = 0;
                        }
                        else
                        {
                            byte0 = -1;
                        }

                        mcServer.configManager.sendPlayerToOtherDimension(this, byte0);
                        lastExperience = -1;
                        lastHealth = -1;
                        field_35221_cc = -1;
                        triggerAchievement(AchievementList.portal);
                    }
                }

                inPortal = false;
            }
        }
        else
        {
            if (timeInPortal > 0.0F)
            {
                timeInPortal -= 0.05F;
            }

            if (timeInPortal < 0.0F)
            {
                timeInPortal = 0.0F;
            }
        }

        if (timeUntilPortal > 0)
        {
            timeUntilPortal--;
        }

        if (getHealth() != lastHealth || field_35221_cc != foodStats.getFoodLevel() || (foodStats.getSaturationLevel() == 0.0F) != field_35222_cd)
        {
            playerNetServerHandler.sendPacket(new Packet8UpdateHealth(getHealth(), foodStats.getFoodLevel(), foodStats.getSaturationLevel()));
            lastHealth = getHealth();
            field_35221_cc = foodStats.getFoodLevel();
            field_35222_cd = foodStats.getSaturationLevel() == 0.0F;
        }

        if (experienceTotal != lastExperience)
        {
            lastExperience = experienceTotal;
            playerNetServerHandler.sendPacket(new Packet43Experience(experience, experienceTotal, experienceLevel));
        }
    }

    public void travelToTheEnd(int par1)
    {
        if (dimension == 1 && par1 == 1)
        {
            triggerAchievement(AchievementList.theEnd2);
            worldObj.setEntityDead(this);
            gameOver = true;
            playerNetServerHandler.sendPacket(new Packet70Bed(4, 0));
        }
        else
        {
            triggerAchievement(AchievementList.theEnd);
            ChunkCoordinates chunkcoordinates = mcServer.getWorldManager(par1).getEntrancePortalLocation();

            if (chunkcoordinates != null)
            {
                playerNetServerHandler.teleportTo(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, 0.0F, 0.0F);
            }

            mcServer.configManager.sendPlayerToOtherDimension(this, 1);
            lastExperience = -1;
            lastHealth = -1;
            field_35221_cc = -1;
        }
    }

    /**
     * gets description packets from all TileEntity's that override func_20070
     */
    private void getTileEntityInfo(TileEntity par1TileEntity)
    {
        if (par1TileEntity != null)
        {
            Packet packet = par1TileEntity.getDescriptionPacket();

            if (packet != null)
            {
                playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    /**
     * Called whenever an item is picked up from walking over it. Args: pickedUpEntity, stackSize
     */
    public void onItemPickup(Entity par1Entity, int par2)
    {
        if (!par1Entity.isDead)
        {
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);

            if (par1Entity instanceof EntityItem)
            {
                entitytracker.sendPacketToTrackedPlayers(par1Entity, new Packet22Collect(par1Entity.entityId, entityId));
            }

            if (par1Entity instanceof EntityArrow)
            {
                entitytracker.sendPacketToTrackedPlayers(par1Entity, new Packet22Collect(par1Entity.entityId, entityId));
            }

            if (par1Entity instanceof EntityXPOrb)
            {
                entitytracker.sendPacketToTrackedPlayers(par1Entity, new Packet22Collect(par1Entity.entityId, entityId));
            }
        }

        super.onItemPickup(par1Entity, par2);
        craftingInventory.updateCraftingResults();
    }

    /**
     * Swings the item the player is holding.
     */
    public void swingItem()
    {
        if (!isSwinging)
        {
            swingProgressInt = -1;
            isSwinging = true;
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            entitytracker.sendPacketToTrackedPlayers(this, new Packet18Animation(this, 1));
        }
    }

    public void func_22068_s()
    {
    }

    /**
     * puts player to sleep on specified bed if possible
     */
    public EnumStatus sleepInBedAt(int par1, int par2, int par3)
    {
        EnumStatus enumstatus = super.sleepInBedAt(par1, par2, par3);

        if (enumstatus == EnumStatus.OK)
        {
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            Packet17Sleep packet17sleep = new Packet17Sleep(this, 0, par1, par2, par3);
            entitytracker.sendPacketToTrackedPlayers(this, packet17sleep);
            playerNetServerHandler.teleportTo(posX, posY, posZ, rotationYaw, rotationPitch);
            playerNetServerHandler.sendPacket(packet17sleep);
        }

        return enumstatus;
    }

    /**
     * Wake up the player if they're sleeping.
     */
    public void wakeUpPlayer(boolean par1, boolean par2, boolean par3)
    {
        if (isPlayerSleeping())
        {
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            entitytracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(this, 3));
        }

        super.wakeUpPlayer(par1, par2, par3);

        if (playerNetServerHandler != null)
        {
            playerNetServerHandler.teleportTo(posX, posY, posZ, rotationYaw, rotationPitch);
        }
    }

    /**
     * Called when a player mounts an entity. e.g. mounts a pig, mounts a boat.
     */
    public void mountEntity(Entity par1Entity)
    {
        super.mountEntity(par1Entity);
        playerNetServerHandler.sendPacket(new Packet39AttachEntity(this, ridingEntity));
        playerNetServerHandler.teleportTo(posX, posY, posZ, rotationYaw, rotationPitch);
    }

    /**
     * Takes in the distance the entity has fallen this tick and whether its on the ground to update the fall distance
     * and deal fall damage if landing on the ground.  Args: distanceFallenThisTick, onGround
     */
    protected void updateFallState(double d, boolean flag)
    {
    }

    /**
     * process player falling based on movement packet
     */
    public void handleFalling(double par1, boolean par3)
    {
        super.updateFallState(par1, par3);
    }

    /**
     * get the next window id to use
     */
    private void getNextWidowId()
    {
        currentWindowId = currentWindowId % 100 + 1;
    }

    /**
     * Displays the crafting GUI for a workbench.
     */
    public void displayWorkbenchGUI(int par1, int par2, int par3)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 1, "Crafting", 9));
        craftingInventory = new ContainerWorkbench(inventory, worldObj, par1, par2, par3);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    public void displayGUIEnchantment(int par1, int par2, int par3)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 4, "Enchanting", 9));
        craftingInventory = new ContainerEnchantment(inventory, worldObj, par1, par2, par3);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    /**
     * Displays the GUI for interacting with a chest inventory. Args: chestInventory
     */
    public void displayGUIChest(IInventory par1IInventory)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 0, par1IInventory.getInvName(), par1IInventory.getSizeInventory()));
        craftingInventory = new ContainerChest(inventory, par1IInventory);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    /**
     * Displays the furnace GUI for the passed in furnace entity. Args: tileEntityFurnace
     */
    public void displayGUIFurnace(TileEntityFurnace par1TileEntityFurnace)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 2, par1TileEntityFurnace.getInvName(), par1TileEntityFurnace.getSizeInventory()));
        craftingInventory = new ContainerFurnace(inventory, par1TileEntityFurnace);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    /**
     * Displays the dipsenser GUI for the passed in dispenser entity. Args: TileEntityDispenser
     */
    public void displayGUIDispenser(TileEntityDispenser par1TileEntityDispenser)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 3, par1TileEntityDispenser.getInvName(), par1TileEntityDispenser.getSizeInventory()));
        craftingInventory = new ContainerDispenser(inventory, par1TileEntityDispenser);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    /**
     * Displays the GUI for interacting with a brewing stand.
     */
    public void displayGUIBrewingStand(TileEntityBrewingStand par1TileEntityBrewingStand)
    {
        getNextWidowId();
        playerNetServerHandler.sendPacket(new Packet100OpenWindow(currentWindowId, 5, par1TileEntityBrewingStand.getInvName(), par1TileEntityBrewingStand.getSizeInventory()));
        craftingInventory = new ContainerBrewingStand(inventory, par1TileEntityBrewingStand);
        craftingInventory.windowId = currentWindowId;
        craftingInventory.onCraftGuiOpened(this);
    }

    /**
     * inform the player of a change in a single slot
     */
    public void updateCraftingInventorySlot(Container par1Container, int par2, ItemStack par3ItemStack)
    {
        if (par1Container.getSlot(par2) instanceof SlotCrafting)
        {
            return;
        }

        if (isChangingQuantityOnly)
        {
            return;
        }
        else
        {
            playerNetServerHandler.sendPacket(new Packet103SetSlot(par1Container.windowId, par2, par3ItemStack));
            return;
        }
    }

    public void func_28017_a(Container par1Container)
    {
        updateCraftingInventory(par1Container, par1Container.func_28127_b());
    }

    /**
     * update the crafting window inventory with the items in the list
     */
    public void updateCraftingInventory(Container par1Container, List par2List)
    {
        playerNetServerHandler.sendPacket(new Packet104WindowItems(par1Container.windowId, par2List));
        playerNetServerHandler.sendPacket(new Packet103SetSlot(-1, -1, inventory.getItemStack()));
    }

    /**
     * send information about the crafting inventory to the client(currently only for furnace times)
     */
    public void updateCraftingInventoryInfo(Container par1Container, int par2, int par3)
    {
        playerNetServerHandler.sendPacket(new Packet105UpdateProgressbar(par1Container.windowId, par2, par3));
    }

    public void onItemStackChanged(ItemStack itemstack)
    {
    }

    /**
     * set current crafting inventory back to the 2x2 square
     */
    public void closeScreen()
    {
        playerNetServerHandler.sendPacket(new Packet101CloseWindow(craftingInventory.windowId));
        closeCraftingGui();
    }

    /**
     * updates item held by mouse, This method always returns before doing anything...
     */
    public void updateHeldItem()
    {
        if (isChangingQuantityOnly)
        {
            return;
        }
        else
        {
            playerNetServerHandler.sendPacket(new Packet103SetSlot(-1, -1, inventory.getItemStack()));
            return;
        }
    }

    /**
     * close the current crafting gui
     */
    public void closeCraftingGui()
    {
        craftingInventory.onCraftGuiClosed(this);
        craftingInventory = inventorySlots;
    }

    /**
     * Adds a value to a statistic field.
     */
    public void addStat(StatBase par1StatBase, int par2)
    {
        if (par1StatBase == null)
        {
            return;
        }

        if (!par1StatBase.isIndependent)
        {
            for (; par2 > 100; par2 -= 100)
            {
                playerNetServerHandler.sendPacket(new Packet200Statistic(par1StatBase.statId, 100));
            }

            playerNetServerHandler.sendPacket(new Packet200Statistic(par1StatBase.statId, par2));
        }
    }

    public void func_30002_A()
    {
        if (ridingEntity != null)
        {
            mountEntity(ridingEntity);
        }

        if (riddenByEntity != null)
        {
            riddenByEntity.mountEntity(this);
        }

        if (sleeping)
        {
            wakeUpPlayer(true, false, false);
        }
    }

    public void func_30001_B()
    {
        lastHealth = 0xfa0a1f01;
    }

    /**
     * Add a chat message to the player
     */
    public void addChatMessage(String par1Str)
    {
        StringTranslate stringtranslate = StringTranslate.getInstance();
        String s = stringtranslate.translateKey(par1Str);
        playerNetServerHandler.sendPacket(new Packet3Chat(s));
    }

    /**
     * Used for when item use count runs out, ie: eating completed
     */
    protected void onItemUseFinish()
    {
        playerNetServerHandler.sendPacket(new Packet38EntityStatus(entityId, (byte)9));
        super.onItemUseFinish();
    }

    /**
     * sets the itemInUse when the use item button is clicked. Args: itemstack, int maxItemUseDuration
     */
    public void setItemInUse(ItemStack par1ItemStack, int par2)
    {
        super.setItemInUse(par1ItemStack, par2);

        if (par1ItemStack != null && par1ItemStack.getItem() != null && par1ItemStack.getItem().getItemUseAction(par1ItemStack) == EnumAction.eat)
        {
            EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
            entitytracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(this, 5));
        }
    }

    protected void onNewPotionEffect(PotionEffect par1PotionEffect)
    {
        super.onNewPotionEffect(par1PotionEffect);
        playerNetServerHandler.sendPacket(new Packet41EntityEffect(entityId, par1PotionEffect));
    }

    protected void onChangedPotionEffect(PotionEffect par1PotionEffect)
    {
        super.onChangedPotionEffect(par1PotionEffect);
        playerNetServerHandler.sendPacket(new Packet41EntityEffect(entityId, par1PotionEffect));
    }

    protected void onFinishedPotionEffect(PotionEffect par1PotionEffect)
    {
        super.onFinishedPotionEffect(par1PotionEffect);
        playerNetServerHandler.sendPacket(new Packet42RemoveEntityEffect(entityId, par1PotionEffect));
    }

    /**
     * Sets the position of the entity and updates the 'last' variables
     */
    public void setPositionAndUpdate(double par1, double par3, double par5)
    {
        playerNetServerHandler.teleportTo(par1, par3, par5, rotationYaw, rotationPitch);
    }

    /**
     * Called when the player performs a critical hit on the Entity. Args: entity that was hit critically
     */
    public void onCriticalHit(Entity par1Entity)
    {
        EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
        entitytracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(par1Entity, 6));
    }

    public void onEnchantmentCritical(Entity par1Entity)
    {
        EntityTracker entitytracker = mcServer.getEntityTracker(dimension);
        entitytracker.sendPacketToTrackedPlayersAndTrackedEntity(this, new Packet18Animation(par1Entity, 7));
    }

    public void func_50022_L()
    {
        if (playerNetServerHandler == null)
        {
            return;
        }
        else
        {
            playerNetServerHandler.sendPacket(new Packet202PlayerAbilities(capabilities));
            return;
        }
    }
}
