package net.minecraft.src;

import java.io.File;
import java.util.List;

public interface ISaveHandler
{
    /**
     * Loads and returns the world info
     */
    public abstract WorldInfo loadWorldInfo();

    /**
     * Checks the session lock to prevent save collisions
     */
    public abstract void checkSessionLock();

    /**
     * initializes and returns the chunk loader for the specified world provider
     */
    public abstract IChunkLoader getChunkLoader(WorldProvider worldprovider);

    /**
     * saves level.dat and backs up the existing one to level.dat_old
     */
    public abstract void saveWorldInfoAndPlayer(WorldInfo worldinfo, List list);

    /**
     * used to update level.dat from old format to MCRegion format
     */
    public abstract void saveWorldInfo(WorldInfo worldinfo);

    public abstract IPlayerFileData getPlayerNBTManager();

    public abstract void func_22093_e();

    /**
     * Gets the file location of the given map
     */
    public abstract File getMapFileFromName(String s);
}
