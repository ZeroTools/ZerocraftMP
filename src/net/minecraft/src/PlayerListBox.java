package net.minecraft.src;

import java.util.List;
import java.util.Vector;
import javax.swing.JList;
import net.minecraft.server.MinecraftServer;

public class PlayerListBox extends JList implements IUpdatePlayerListBox
{
    /** Reference to the MinecraftServer object. */
    private MinecraftServer mcServer;

    /** Counts the number of updates. */
    private int updateCounter;

    public PlayerListBox(MinecraftServer par1MinecraftServer)
    {
        updateCounter = 0;
        mcServer = par1MinecraftServer;
        par1MinecraftServer.addToOnlinePlayerList(this);
    }

    /**
     * Updates the Jlist with a new model.
     */
    public void update()
    {
        if (updateCounter++ % 20 == 0)
        {
            Vector vector = new Vector();

            for (int i = 0; i < mcServer.configManager.playerEntities.size(); i++)
            {
                vector.add(((EntityPlayerMP)mcServer.configManager.playerEntities.get(i)).username);
            }

            setListData(vector);
        }
    }
}
