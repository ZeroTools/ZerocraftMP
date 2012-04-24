package net.minecraft.src;

public interface ISaveFormat
{
    /**
     * gets if the map is old chunk saving (true) or McRegion (false)
     */
    public abstract boolean isOldMapFormat(String s);

    /**
     * converts the map to mcRegion
     */
    public abstract boolean convertMapFormat(String s, IProgressUpdate iprogressupdate);
}
