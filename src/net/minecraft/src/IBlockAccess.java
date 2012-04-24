package net.minecraft.src;

public interface IBlockAccess
{
    /**
     * Returns the block ID at coords x,y,z
     */
    public abstract int getBlockId(int i, int j, int k);

    /**
     * Returns the TileEntity associated with a given block in X,Y,Z coordinates, or null if no TileEntity exists
     */
    public abstract TileEntity getBlockTileEntity(int i, int j, int k);

    /**
     * Returns the block metadata at coords x,y,z
     */
    public abstract int getBlockMetadata(int i, int j, int k);

    /**
     * Returns the block's material.
     */
    public abstract Material getBlockMaterial(int i, int j, int k);

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    public abstract boolean isBlockNormalCube(int i, int j, int k);
}
