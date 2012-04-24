package net.minecraft.src;

public class ChunkCache implements IBlockAccess
{
    private int chunkX;
    private int chunkZ;
    private Chunk chunkArray[][];
    private boolean field_48098_d;

    /** Reference to the World object. */
    private World worldObj;

    public ChunkCache(World par1World, int par2, int par3, int par4, int par5, int par6, int par7)
    {
        worldObj = par1World;
        chunkX = par2 >> 4;
        chunkZ = par4 >> 4;
        int i = par5 >> 4;
        int j = par7 >> 4;
        chunkArray = new Chunk[(i - chunkX) + 1][(j - chunkZ) + 1];
        field_48098_d = true;

        for (int k = chunkX; k <= i; k++)
        {
            for (int l = chunkZ; l <= j; l++)
            {
                Chunk chunk = par1World.getChunkFromChunkCoords(k, l);

                if (chunk == null)
                {
                    continue;
                }

                chunkArray[k - chunkX][l - chunkZ] = chunk;

                if (!chunk.getAreLevelsEmpty(par3, par6))
                {
                    field_48098_d = false;
                }
            }
        }
    }

    /**
     * Returns the block ID at coords x,y,z
     */
    public int getBlockId(int par1, int par2, int par3)
    {
        if (par2 < 0)
        {
            return 0;
        }

        if (par2 >= 256)
        {
            return 0;
        }

        int i = (par1 >> 4) - chunkX;
        int j = (par3 >> 4) - chunkZ;

        if (i < 0 || i >= chunkArray.length || j < 0 || j >= chunkArray[i].length)
        {
            return 0;
        }

        Chunk chunk = chunkArray[i][j];

        if (chunk == null)
        {
            return 0;
        }
        else
        {
            return chunk.getBlockID(par1 & 0xf, par2, par3 & 0xf);
        }
    }

    /**
     * Returns the TileEntity associated with a given block in X,Y,Z coordinates, or null if no TileEntity exists
     */
    public TileEntity getBlockTileEntity(int par1, int par2, int par3)
    {
        int i = (par1 >> 4) - chunkX;
        int j = (par3 >> 4) - chunkZ;
        return chunkArray[i][j].getChunkBlockTileEntity(par1 & 0xf, par2, par3 & 0xf);
    }

    /**
     * Returns the block metadata at coords x,y,z
     */
    public int getBlockMetadata(int par1, int par2, int par3)
    {
        if (par2 < 0)
        {
            return 0;
        }

        if (par2 >= 256)
        {
            return 0;
        }
        else
        {
            int i = (par1 >> 4) - chunkX;
            int j = (par3 >> 4) - chunkZ;
            return chunkArray[i][j].getBlockMetadata(par1 & 0xf, par2, par3 & 0xf);
        }
    }

    /**
     * Returns the block's material.
     */
    public Material getBlockMaterial(int par1, int par2, int par3)
    {
        int i = getBlockId(par1, par2, par3);

        if (i == 0)
        {
            return Material.air;
        }
        else
        {
            return Block.blocksList[i].blockMaterial;
        }
    }

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    public boolean isBlockNormalCube(int par1, int par2, int par3)
    {
        Block block = Block.blocksList[getBlockId(par1, par2, par3)];

        if (block == null)
        {
            return false;
        }
        else
        {
            return block.blockMaterial.blocksMovement() && block.renderAsNormalBlock();
        }
    }
}
