package net.minecraft.src;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

public class SaveHandler implements IPlayerFileData, ISaveHandler
{
    /** Reference to the logger. */
    private static final Logger logger = Logger.getLogger("Minecraft");

    /** The path to the current world directory */
    private final File worldDirectory;

    /** The directory in which to save world data */
    private final File worldFile;
    private final File mapDataDir;

    /**
     * The time in milliseconds when this field was initialized. Stored in the session lock file.
     */
    private final long initializationTime = System.currentTimeMillis();

    /** The directory name of the world */
    private final String saveDirectoryName;

    public SaveHandler(File par1File, String par2Str, boolean par3)
    {
        worldDirectory = new File(par1File, par2Str);
        worldDirectory.mkdirs();
        worldFile = new File(worldDirectory, "players");
        mapDataDir = new File(worldDirectory, "data");
        mapDataDir.mkdirs();
        saveDirectoryName = par2Str;

        if (par3)
        {
            worldFile.mkdirs();
        }

        setSessionLock();
    }

    /**
     * Creates a session lock file for this process
     */
    private void setSessionLock()
    {
        try
        {
            File file = new File(worldDirectory, "session.lock");
            DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));

            try
            {
                dataoutputstream.writeLong(initializationTime);
            }
            finally
            {
                dataoutputstream.close();
            }
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
            throw new RuntimeException("Failed to check session lock, aborting");
        }
    }

    /**
     * Gets the File object corresponding to the base directory of this world.
     */
    protected File getWorldDirectory()
    {
        return worldDirectory;
    }

    /**
     * Checks the session lock to prevent save collisions
     */
    public void checkSessionLock()
    {
        try
        {
            File file = new File(worldDirectory, "session.lock");
            DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));

            try
            {
                if (datainputstream.readLong() != initializationTime)
                {
                    throw new MinecraftException("The save is being accessed from another location, aborting");
                }
            }
            finally
            {
                datainputstream.close();
            }
        }
        catch (IOException ioexception)
        {
            throw new MinecraftException("Failed to check session lock, aborting");
        }
    }

    /**
     * initializes and returns the chunk loader for the specified world provider
     */
    public IChunkLoader getChunkLoader(WorldProvider par1WorldProvider)
    {
        throw new RuntimeException("Old Chunk Storage is no longer supported.");
    }

    /**
     * Loads and returns the world info
     */
    public WorldInfo loadWorldInfo()
    {
        File file = new File(worldDirectory, "level.dat");

        if (file.exists())
        {
            try
            {
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file));
                NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Data");
                return new WorldInfo(nbttagcompound2);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        file = new File(worldDirectory, "level.dat_old");

        if (file.exists())
        {
            try
            {
                NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
                NBTTagCompound nbttagcompound3 = nbttagcompound1.getCompoundTag("Data");
                return new WorldInfo(nbttagcompound3);
            }
            catch (Exception exception1)
            {
                exception1.printStackTrace();
            }
        }

        return null;
    }

    /**
     * saves level.dat and backs up the existing one to level.dat_old
     */
    public void saveWorldInfoAndPlayer(WorldInfo par1WorldInfo, List par2List)
    {
        NBTTagCompound nbttagcompound = par1WorldInfo.getNBTTagCompoundWithPlayers(par2List);
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setTag("Data", nbttagcompound);

        try
        {
            File file = new File(worldDirectory, "level.dat_new");
            File file1 = new File(worldDirectory, "level.dat_old");
            File file2 = new File(worldDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(nbttagcompound1, new FileOutputStream(file));

            if (file1.exists())
            {
                file1.delete();
            }

            file2.renameTo(file1);

            if (file2.exists())
            {
                file2.delete();
            }

            file.renameTo(file2);

            if (file.exists())
            {
                file.delete();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * used to update level.dat from old format to MCRegion format
     */
    public void saveWorldInfo(WorldInfo par1WorldInfo)
    {
        NBTTagCompound nbttagcompound = par1WorldInfo.getNBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setTag("Data", nbttagcompound);

        try
        {
            File file = new File(worldDirectory, "level.dat_new");
            File file1 = new File(worldDirectory, "level.dat_old");
            File file2 = new File(worldDirectory, "level.dat");
            CompressedStreamTools.writeCompressed(nbttagcompound1, new FileOutputStream(file));

            if (file1.exists())
            {
                file1.delete();
            }

            file2.renameTo(file1);

            if (file2.exists())
            {
                file2.delete();
            }

            file.renameTo(file2);

            if (file.exists())
            {
                file.delete();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Writes the player data to disk from the specified PlayerEntityMP.
     */
    public void writePlayerData(EntityPlayer par1EntityPlayer)
    {
        try
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            par1EntityPlayer.writeToNBT(nbttagcompound);
            File file = new File(worldFile, "_tmp_.dat");
            File file1 = new File(worldFile, (new StringBuilder()).append(par1EntityPlayer.username).append(".dat").toString());
            CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file));

            if (file1.exists())
            {
                file1.delete();
            }

            file.renameTo(file1);
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to save player data for ").append(par1EntityPlayer.username).toString());
        }
    }

    /**
     * Reads the player data from disk into the specified PlayerEntityMP.
     */
    public void readPlayerData(EntityPlayer par1EntityPlayer)
    {
        NBTTagCompound nbttagcompound = getPlayerData(par1EntityPlayer.username);

        if (nbttagcompound != null)
        {
            par1EntityPlayer.readFromNBT(nbttagcompound);
        }
    }

    public NBTTagCompound getPlayerData(String par1Str)
    {
        try
        {
            File file = new File(worldFile, (new StringBuilder()).append(par1Str).append(".dat").toString());

            if (file.exists())
            {
                return CompressedStreamTools.readCompressed(new FileInputStream(file));
            }
        }
        catch (Exception exception)
        {
            logger.warning((new StringBuilder()).append("Failed to load player data for ").append(par1Str).toString());
        }

        return null;
    }

    public IPlayerFileData getPlayerNBTManager()
    {
        return this;
    }

    public String[] func_52007_g()
    {
        return worldFile.list();
    }

    public void func_22093_e()
    {
    }

    /**
     * Gets the file location of the given map
     */
    public File getMapFileFromName(String par1Str)
    {
        return new File(mapDataDir, (new StringBuilder()).append(par1Str).append(".dat").toString());
    }
}
