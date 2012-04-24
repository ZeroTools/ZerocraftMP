package net.minecraft.src;

import java.util.*;
import net.minecraft.server.MinecraftServer;

public class EntityTracker
{
    /**
     * List of tracked entities, used for iteration operations on tracked entities.
     */
    private Set trackedEntitySet;

    /** Used for identity lookup of tracked entities. */
    private IntHashMap trackedEntityHashTable;

    /** Reference to the MinecraftServer object. */
    private MinecraftServer mcServer;
    private int maxTrackingDistanceThreshold;
    private int field_28113_e;

    public EntityTracker(MinecraftServer par1MinecraftServer, int par2)
    {
        trackedEntitySet = new HashSet();
        trackedEntityHashTable = new IntHashMap();
        mcServer = par1MinecraftServer;
        field_28113_e = par2;
        maxTrackingDistanceThreshold = par1MinecraftServer.configManager.getMaxTrackingDistance();
    }

    public void trackEntity(Entity par1Entity)
    {
        if (par1Entity instanceof EntityPlayerMP)
        {
            trackEntity(par1Entity, 512, 2);
            EntityPlayerMP entityplayermp = (EntityPlayerMP)par1Entity;
            Iterator iterator = trackedEntitySet.iterator();

            do
            {
                if (!iterator.hasNext())
                {
                    break;
                }

                EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)iterator.next();

                if (entitytrackerentry.trackedEntity != entityplayermp)
                {
                    entitytrackerentry.updatePlayerEntity(entityplayermp);
                }
            }
            while (true);
        }
        else if (par1Entity instanceof EntityFishHook)
        {
            trackEntity(par1Entity, 64, 5, true);
        }
        else if (par1Entity instanceof EntityArrow)
        {
            trackEntity(par1Entity, 64, 20, false);
        }
        else if (par1Entity instanceof EntitySmallFireball)
        {
            trackEntity(par1Entity, 64, 10, false);
        }
        else if (par1Entity instanceof EntityFireball)
        {
            trackEntity(par1Entity, 64, 10, false);
        }
        else if (par1Entity instanceof EntitySnowball)
        {
            trackEntity(par1Entity, 64, 10, true);
        }
        else if (par1Entity instanceof EntityEnderPearl)
        {
            trackEntity(par1Entity, 64, 10, true);
        }
        else if (par1Entity instanceof EntityEnderEye)
        {
            trackEntity(par1Entity, 64, 10, true);
        }
        else if (par1Entity instanceof EntityEgg)
        {
            trackEntity(par1Entity, 64, 10, true);
        }
        else if (par1Entity instanceof EntityPotion)
        {
            trackEntity(par1Entity, 64, 10, true);
        }
        else if (par1Entity instanceof EntityExpBottle)
        {
            trackEntity(par1Entity, 64, 10, true);
        }
        else if (par1Entity instanceof EntityItem)
        {
            trackEntity(par1Entity, 64, 20, true);
        }
        else if (par1Entity instanceof EntityMinecart)
        {
            trackEntity(par1Entity, 80, 3, true);
        }
        else if (par1Entity instanceof EntityBoat)
        {
            trackEntity(par1Entity, 80, 3, true);
        }
        else if (par1Entity instanceof EntitySquid)
        {
            trackEntity(par1Entity, 64, 3, true);
        }
        else if (par1Entity instanceof IAnimals)
        {
            trackEntity(par1Entity, 80, 3, true);
        }
        else if (par1Entity instanceof EntityDragon)
        {
            trackEntity(par1Entity, 160, 3, true);
        }
        else if (par1Entity instanceof EntityTNTPrimed)
        {
            trackEntity(par1Entity, 160, 10, true);
        }
        else if (par1Entity instanceof EntityFallingSand)
        {
            trackEntity(par1Entity, 160, 20, true);
        }
        else if (par1Entity instanceof EntityPainting)
        {
            trackEntity(par1Entity, 160, 0x7fffffff, false);
        }
        else if (par1Entity instanceof EntityXPOrb)
        {
            trackEntity(par1Entity, 160, 20, true);
        }
        else if (par1Entity instanceof EntityEnderCrystal)
        {
            trackEntity(par1Entity, 256, 0x7fffffff, false);
        }
    }

    public void trackEntity(Entity par1Entity, int par2, int par3)
    {
        trackEntity(par1Entity, par2, par3, false);
    }

    public void trackEntity(Entity par1Entity, int par2, int par3, boolean par4)
    {
        if (par2 > maxTrackingDistanceThreshold)
        {
            par2 = maxTrackingDistanceThreshold;
        }

        if (trackedEntityHashTable.containsItem(par1Entity.entityId))
        {
            throw new IllegalStateException("Entity is already tracked!");
        }
        else
        {
            EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(par1Entity, par2, par3, par4);
            trackedEntitySet.add(entitytrackerentry);
            trackedEntityHashTable.addKey(par1Entity.entityId, entitytrackerentry);
            entitytrackerentry.updatePlayerEntities(mcServer.getWorldManager(field_28113_e).playerEntities);
            return;
        }
    }

    public void untrackEntity(Entity par1Entity)
    {
        if (par1Entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)par1Entity;
            EntityTrackerEntry entitytrackerentry1;

            for (Iterator iterator = trackedEntitySet.iterator(); iterator.hasNext(); entitytrackerentry1.removeFromTrackedPlayers(entityplayermp))
            {
                entitytrackerentry1 = (EntityTrackerEntry)iterator.next();
            }
        }

        EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)trackedEntityHashTable.removeObject(par1Entity.entityId);

        if (entitytrackerentry != null)
        {
            trackedEntitySet.remove(entitytrackerentry);
            entitytrackerentry.sendDestroyEntityPacketToTrackedPlayers();
        }
    }

    public void updateTrackedEntities()
    {
        ArrayList arraylist = new ArrayList();
        Iterator iterator = trackedEntitySet.iterator();

        do
        {
            if (!iterator.hasNext())
            {
                break;
            }

            EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)iterator.next();
            entitytrackerentry.updatePlayerList(mcServer.getWorldManager(field_28113_e).playerEntities);

            if (entitytrackerentry.playerEntitiesUpdated && (entitytrackerentry.trackedEntity instanceof EntityPlayerMP))
            {
                arraylist.add((EntityPlayerMP)entitytrackerentry.trackedEntity);
            }
        }
        while (true);

        label0:

        for (int i = 0; i < arraylist.size(); i++)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)arraylist.get(i);
            Iterator iterator1 = trackedEntitySet.iterator();

            do
            {
                if (!iterator1.hasNext())
                {
                    continue label0;
                }

                EntityTrackerEntry entitytrackerentry1 = (EntityTrackerEntry)iterator1.next();

                if (entitytrackerentry1.trackedEntity != entityplayermp)
                {
                    entitytrackerentry1.updatePlayerEntity(entityplayermp);
                }
            }
            while (true);
        }
    }

    public void sendPacketToTrackedPlayers(Entity par1Entity, Packet par2Packet)
    {
        EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)trackedEntityHashTable.lookup(par1Entity.entityId);

        if (entitytrackerentry != null)
        {
            entitytrackerentry.sendPacketToTrackedPlayers(par2Packet);
        }
    }

    public void sendPacketToTrackedPlayersAndTrackedEntity(Entity par1Entity, Packet par2Packet)
    {
        EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)trackedEntityHashTable.lookup(par1Entity.entityId);

        if (entitytrackerentry != null)
        {
            entitytrackerentry.sendPacketToTrackedPlayersAndTrackedEntity(par2Packet);
        }
    }

    public void removeTrackedPlayerSymmetric(EntityPlayerMP par1EntityPlayerMP)
    {
        EntityTrackerEntry entitytrackerentry;

        for (Iterator iterator = trackedEntitySet.iterator(); iterator.hasNext(); entitytrackerentry.removeTrackedPlayerSymmetric(par1EntityPlayerMP))
        {
            entitytrackerentry = (EntityTrackerEntry)iterator.next();
        }
    }
}
