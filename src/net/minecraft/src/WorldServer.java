package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;

public class WorldServer extends World
{
    public ChunkProviderServer chunkProviderServer;

    /** Set to true when an op is building or this dimension != 0 */
    public boolean disableSpawnProtection;

    /** Whether or not level saving is enabled */
    public boolean levelSaving;
    private MinecraftServer mcServer;

    /** Maps ids to entity instances */
    private IntHashMap entityInstanceIdMap;

    public WorldServer(MinecraftServer par1MinecraftServer, ISaveHandler par2ISaveHandler, String par3Str, int par4, WorldSettings par5WorldSettings)
    {
        super(par2ISaveHandler, par3Str, par5WorldSettings, WorldProvider.getProviderForDimension(par4));
        disableSpawnProtection = false;
        mcServer = par1MinecraftServer;

        if (entityInstanceIdMap == null)
        {
            entityInstanceIdMap = new IntHashMap();
        }
    }

    /**
     * Will update the entity in the world if the chunk the entity is in is currently loaded or its forced to update.
     * Args: entity, forceUpdate
     */
    public void updateEntityWithOptionalForce(Entity par1Entity, boolean par2)
    {
        if (!mcServer.spawnPeacefulMobs && ((par1Entity instanceof EntityAnimal) || (par1Entity instanceof EntityWaterMob)))
        {
            par1Entity.setDead();
        }

        if (!mcServer.field_44002_p && (par1Entity instanceof INpc))
        {
            par1Entity.setDead();
        }

        if (par1Entity.riddenByEntity == null || !(par1Entity.riddenByEntity instanceof EntityPlayer))
        {
            super.updateEntityWithOptionalForce(par1Entity, par2);
        }
    }

    public void func_12017_b(Entity par1Entity, boolean par2)
    {
        super.updateEntityWithOptionalForce(par1Entity, par2);
    }

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    protected IChunkProvider createChunkProvider()
    {
        IChunkLoader ichunkloader = saveHandler.getChunkLoader(worldProvider);
        chunkProviderServer = new ChunkProviderServer(this, ichunkloader, worldProvider.getChunkProvider());
        return chunkProviderServer;
    }

    /**
     * get a list of tileEntity's
     */
    public List getTileEntityList(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        ArrayList arraylist = new ArrayList();

        for (int i = 0; i < loadedTileEntityList.size(); i++)
        {
            TileEntity tileentity = (TileEntity)loadedTileEntityList.get(i);

            if (tileentity.xCoord >= par1 && tileentity.yCoord >= par2 && tileentity.zCoord >= par3 && tileentity.xCoord < par4 && tileentity.yCoord < par5 && tileentity.zCoord < par6)
            {
                arraylist.add(tileentity);
            }
        }

        return arraylist;
    }

    /**
     * Called when checking if a certain block can be mined or not. The 'spawn safe zone' check is located here.
     */
    public boolean canMineBlock(EntityPlayer par1EntityPlayer, int par2, int par3, int par4)
    {
        int i = MathHelper.abs(par2 - worldInfo.getSpawnX());
        int j = MathHelper.abs(par4 - worldInfo.getSpawnZ());

        if (i > j)
        {
            j = i;
        }

        return j > 16 || mcServer.configManager.isOp(par1EntityPlayer.username);
    }

    /**
     * generates a spawn point for this world
     */
    protected void generateSpawnPoint()
    {
        if (entityInstanceIdMap == null)
        {
            entityInstanceIdMap = new IntHashMap();
        }

        super.generateSpawnPoint();
    }

    /**
     * Start the skin for this entity downloading, if necessary, and increment its reference counter
     */
    protected void obtainEntitySkin(Entity par1Entity)
    {
        super.obtainEntitySkin(par1Entity);
        entityInstanceIdMap.addKey(par1Entity.entityId, par1Entity);
        Entity aentity[] = par1Entity.getParts();

        if (aentity != null)
        {
            for (int i = 0; i < aentity.length; i++)
            {
                entityInstanceIdMap.addKey(aentity[i].entityId, aentity[i]);
            }
        }
    }

    /**
     * Decrement the reference counter for this entity's skin image data
     */
    protected void releaseEntitySkin(Entity par1Entity)
    {
        super.releaseEntitySkin(par1Entity);
        entityInstanceIdMap.removeObject(par1Entity.entityId);
        Entity aentity[] = par1Entity.getParts();

        if (aentity != null)
        {
            for (int i = 0; i < aentity.length; i++)
            {
                entityInstanceIdMap.removeObject(aentity[i].entityId);
            }
        }
    }

    public Entity func_6158_a(int par1)
    {
        return (Entity)entityInstanceIdMap.lookup(par1);
    }

    /**
     * adds a lightning bolt to the list of lightning bolts in this world.
     */
    public boolean addWeatherEffect(Entity par1Entity)
    {
        if (super.addWeatherEffect(par1Entity))
        {
            mcServer.configManager.sendPacketToPlayersAroundPoint(par1Entity.posX, par1Entity.posY, par1Entity.posZ, 512D, worldProvider.worldType, new Packet71Weather(par1Entity));
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * sends a Packet 38 (Entity Status) to all tracked players of that entity
     */
    public void setEntityState(Entity par1Entity, byte par2)
    {
        Packet38EntityStatus packet38entitystatus = new Packet38EntityStatus(par1Entity.entityId, par2);
        mcServer.getEntityTracker(worldProvider.worldType).sendPacketToTrackedPlayersAndTrackedEntity(par1Entity, packet38entitystatus);
    }

    /**
     * returns a new explosion. Does initiation (at time of writing Explosion is not finished)
     */
    public Explosion newExplosion(Entity par1Entity, double par2, double par4, double par6, float par8, boolean par9)
    {
        Explosion explosion = new Explosion(this, par1Entity, par2, par4, par6, par8);
        explosion.isFlaming = par9;
        explosion.doExplosionA();
        explosion.doExplosionB(false);
        mcServer.configManager.sendPacketToPlayersAroundPoint(par2, par4, par6, 64D, worldProvider.worldType, new Packet60Explosion(par2, par4, par6, par8, explosion.destroyedBlockPositions));
        return explosion;
    }

    /**
     * plays a given note at x, y, z. args: x, y, z, instrument, note
     */
    public void playNoteAt(int par1, int par2, int par3, int par4, int par5)
    {
        super.playNoteAt(par1, par2, par3, par4, par5);
        mcServer.configManager.sendPacketToPlayersAroundPoint(par1, par2, par3, 64D, worldProvider.worldType, new Packet54PlayNoteBlock(par1, par2, par3, par4, par5));
    }

    public void func_30006_w()
    {
        saveHandler.func_22093_e();
    }

    /**
     * update's all weather states.
     */
    protected void updateWeather()
    {
        boolean flag = isRaining();
        super.updateWeather();

        if (flag != isRaining())
        {
            if (flag)
            {
                mcServer.configManager.sendPacketToAllPlayers(new Packet70Bed(2, 0));
            }
            else
            {
                mcServer.configManager.sendPacketToAllPlayers(new Packet70Bed(1, 0));
            }
        }
    }
}
