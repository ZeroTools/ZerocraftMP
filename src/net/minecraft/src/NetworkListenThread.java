package net.minecraft.src;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;

public class NetworkListenThread
{
    /** Reference to the logger. */
    public static Logger logger = Logger.getLogger("Minecraft");
    private ServerSocket serverSocket;
    private Thread networkAcceptThread;

    /** Whether the network listener object is listening. */
    public volatile boolean isListening;
    private int field_977_f;

    /** list of all people currently trying to connect to the server */
    private ArrayList pendingConnections;

    /** list of all currently connected players */
    private ArrayList playerList;

    /** Reference to the MinecraftServer object. */
    public MinecraftServer mcServer;
    private HashMap field_35506_i;

    public NetworkListenThread(MinecraftServer par1MinecraftServer, InetAddress par2InetAddress, int par3) throws IOException
    {
        isListening = false;
        field_977_f = 0;
        pendingConnections = new ArrayList();
        playerList = new ArrayList();
        field_35506_i = new HashMap();
        mcServer = par1MinecraftServer;
        serverSocket = new ServerSocket(par3, 0, par2InetAddress);
        serverSocket.setPerformancePreferences(0, 2, 1);
        isListening = true;
        networkAcceptThread = new NetworkAcceptThread(this, "Listen thread", par1MinecraftServer);
        networkAcceptThread.start();
    }

    public void func_35505_a(Socket par1Socket)
    {
        InetAddress inetaddress = par1Socket.getInetAddress();

        synchronized (field_35506_i)
        {
            field_35506_i.remove(inetaddress);
        }
    }

    /**
     * adds this connection to the list of currently connected players
     */
    public void addPlayer(NetServerHandler par1NetServerHandler)
    {
        playerList.add(par1NetServerHandler);
    }

    /**
     * adds a new pending connection to the waiting list
     */
    private void addPendingConnection(NetLoginHandler par1NetLoginHandler)
    {
        if (par1NetLoginHandler == null)
        {
            throw new IllegalArgumentException("Got null pendingconnection!");
        }
        else
        {
            pendingConnections.add(par1NetLoginHandler);
            return;
        }
    }

    /**
     * Handles all incoming connections and packets
     */
    public void handleNetworkListenThread()
    {
        for (int i = 0; i < pendingConnections.size(); i++)
        {
            NetLoginHandler netloginhandler = (NetLoginHandler)pendingConnections.get(i);

            try
            {
                netloginhandler.tryLogin();
            }
            catch (Exception exception)
            {
                netloginhandler.kickUser("Internal server error");
                logger.log(Level.WARNING, (new StringBuilder()).append("Failed to handle packet: ").append(exception).toString(), exception);
            }

            if (netloginhandler.finishedProcessing)
            {
                pendingConnections.remove(i--);
            }

            netloginhandler.netManager.wakeThreads();
        }

        for (int j = 0; j < playerList.size(); j++)
        {
            NetServerHandler netserverhandler = (NetServerHandler)playerList.get(j);

            try
            {
                netserverhandler.handlePackets();
            }
            catch (Exception exception1)
            {
                logger.log(Level.WARNING, (new StringBuilder()).append("Failed to handle packet: ").append(exception1).toString(), exception1);
                netserverhandler.kickPlayer("Internal server error");
            }

            if (netserverhandler.connectionClosed)
            {
                playerList.remove(j--);
            }

            netserverhandler.netManager.wakeThreads();
        }
    }

    /**
     * Gets the server socket.
     */
    static ServerSocket getServerSocket(NetworkListenThread par0NetworkListenThread)
    {
        return par0NetworkListenThread.serverSocket;
    }

    static HashMap func_35504_b(NetworkListenThread par0NetworkListenThread)
    {
        return par0NetworkListenThread.field_35506_i;
    }

    static int func_712_b(NetworkListenThread par0NetworkListenThread)
    {
        return par0NetworkListenThread.field_977_f++;
    }

    static void func_716_a(NetworkListenThread par0NetworkListenThread, NetLoginHandler par1NetLoginHandler)
    {
        par0NetworkListenThread.addPendingConnection(par1NetLoginHandler);
    }
}
