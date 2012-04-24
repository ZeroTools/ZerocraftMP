package net.minecraft.src;

import java.io.IOException;
import java.util.*;

public class ChunkProviderServer implements IChunkProvider
{
    private Set droppedChunksSet;

    /** a dummy chunk, returned in place of an actual chunk. */
    private Chunk dummyChunk;

    /**
     * chunk generator object. Calls to load nonexistent chunks are forwarded to this object.
     */
    private IChunkProvider serverChunkGenerator;
    private IChunkLoader chunkLoader;

    /**
     * if set, this flag forces a request to load a chunk to load the chunk rather than defaulting to the dummy if
     * possible
     */
    public boolean chunkLoadOverride;

    /** map of chunk Id's to Chunk instances */
    private LongHashMap id2ChunkMap;
    private List field_727_f;
    private WorldServer world;

    public ChunkProviderServer(WorldServer par1WorldServer, IChunkLoader par2IChunkLoader, IChunkProvider par3IChunkProvider)
    {
        droppedChunksSet = new HashSet();
        chunkLoadOverride = false;
        id2ChunkMap = new LongHashMap();
        field_727_f = new ArrayList();
        dummyChunk = new EmptyChunk(par1WorldServer, 0, 0);
        world = par1WorldServer;
        chunkLoader = par2IChunkLoader;
        serverChunkGenerator = par3IChunkProvider;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    public boolean chunkExists(int par1, int par2)
    {
        return id2ChunkMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(par1, par2));
    }

    public void dropChunk(int par1, int par2)
    {
        if (world.worldProvider.canRespawnHere())
        {
            ChunkCoordinates chunkcoordinates = world.getSpawnPoint();
            int i = (par1 * 16 + 8) - chunkcoordinates.posX;
            int j = (par2 * 16 + 8) - chunkcoordinates.posZ;
            char c = '\200';

            if (i < -c || i > c || j < -c || j > c)
            {
                droppedChunksSet.add(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(par1, par2)));
            }
        }
        else
        {
            droppedChunksSet.add(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(par1, par2)));
        }
    }

    public void unloadAllChunks()
    {
        Chunk chunk;

        for (Iterator iterator = field_727_f.iterator(); iterator.hasNext(); dropChunk(chunk.xPosition, chunk.zPosition))
        {
            chunk = (Chunk)iterator.next();
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public Chunk loadChunk(int par1, int par2)
    {
        long l = ChunkCoordIntPair.chunkXZ2Int(par1, par2);
        droppedChunksSet.remove(Long.valueOf(l));
        Chunk chunk = (Chunk)id2ChunkMap.getValueByKey(l);

        if (chunk == null)
        {
            chunk = loadChunkFromFile(par1, par2);

            if (chunk == null)
            {
                if (serverChunkGenerator == null)
                {
                    chunk = dummyChunk;
                }
                else
                {
                    chunk = serverChunkGenerator.provideChunk(par1, par2);
                }
            }

            id2ChunkMap.add(l, chunk);
            field_727_f.add(chunk);

            if (chunk != null)
            {
                chunk.func_4053_c();
                chunk.onChunkLoad();
            }

            chunk.populateChunk(this, this, par1, par2);
        }

        return chunk;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int par1, int par2)
    {
        Chunk chunk = (Chunk)id2ChunkMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(par1, par2));

        if (chunk == null)
        {
            if (world.findingSpawnPoint || chunkLoadOverride)
            {
                return loadChunk(par1, par2);
            }
            else
            {
                return dummyChunk;
            }
        }
        else
        {
            return chunk;
        }
    }

    private Chunk loadChunkFromFile(int par1, int par2)
    {
        if (chunkLoader == null)
        {
            return null;
        }

        try
        {
            Chunk chunk = chunkLoader.loadChunk(world, par1, par2);

            if (chunk != null)
            {
                chunk.lastSaveTime = world.getWorldTime();
            }

            return chunk;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    private void saveChunkExtraData(Chunk par1Chunk)
    {
        if (chunkLoader == null)
        {
            return;
        }

        try
        {
            chunkLoader.saveExtraChunkData(world, par1Chunk);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void saveChunkData(Chunk par1Chunk)
    {
        if (chunkLoader == null)
        {
            return;
        }

        try
        {
            par1Chunk.lastSaveTime = world.getWorldTime();
            chunkLoader.saveChunk(world, par1Chunk);
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3)
    {
        Chunk chunk = provideChunk(par2, par3);

        if (!chunk.isTerrainPopulated)
        {
            chunk.isTerrainPopulated = true;

            if (serverChunkGenerator != null)
            {
                serverChunkGenerator.populate(par1IChunkProvider, par2, par3);
                chunk.setChunkModified();
            }
        }
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        int i = 0;

        for (int j = 0; j < field_727_f.size(); j++)
        {
            Chunk chunk = (Chunk)field_727_f.get(j);

            if (par1)
            {
                saveChunkExtraData(chunk);
            }

            if (!chunk.needsSaving(par1))
            {
                continue;
            }

            saveChunkData(chunk);
            chunk.isModified = false;

            if (++i == 24 && !par1)
            {
                return false;
            }
        }

        if (par1)
        {
            if (chunkLoader == null)
            {
                return true;
            }

            chunkLoader.saveExtraData();
        }

        return true;
    }

    /**
     * Unloads the 100 oldest chunks from memory, due to a bug with chunkSet.add() never being called it thinks the list
     * is always empty and will not remove any chunks.
     */
    public boolean unload100OldestChunks()
    {
        if (!world.levelSaving)
        {
            for (int i = 0; i < 100; i++)
            {
                if (!droppedChunksSet.isEmpty())
                {
                    Long long1 = (Long)droppedChunksSet.iterator().next();
                    Chunk chunk = (Chunk)id2ChunkMap.getValueByKey(long1.longValue());
                    chunk.onChunkUnload();
                    saveChunkData(chunk);
                    saveChunkExtraData(chunk);
                    droppedChunksSet.remove(long1);
                    id2ChunkMap.remove(long1.longValue());
                    field_727_f.remove(chunk);
                }
            }

            if (chunkLoader != null)
            {
                chunkLoader.chunkTick();
            }
        }

        return serverChunkGenerator.unload100OldestChunks();
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return !world.levelSaving;
    }

    public String func_46040_d()
    {
        return (new StringBuilder()).append("ServerChunkCache: ").append(id2ChunkMap.getNumHashElements()).append(" Drop: ").append(droppedChunksSet.size()).toString();
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        return serverChunkGenerator.getPossibleCreatures(par1EnumCreatureType, par2, par3, par4);
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     */
    public ChunkPosition findClosestStructure(World par1World, String par2Str, int par3, int par4, int par5)
    {
        return serverChunkGenerator.findClosestStructure(par1World, par2Str, par3, par4, par5);
    }
}
