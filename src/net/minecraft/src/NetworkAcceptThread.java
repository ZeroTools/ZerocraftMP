package net.minecraft.src;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import net.minecraft.server.MinecraftServer;

class NetworkAcceptThread extends Thread
{
    /** Reference to the MinecraftServer object. */
    final MinecraftServer mcServer;

    /** The network listener object. */
    final NetworkListenThread netWorkListener;

    NetworkAcceptThread(NetworkListenThread par1NetworkListenThread, String par2Str, MinecraftServer par3MinecraftServer)
    {
        super(par2Str);
        netWorkListener = par1NetworkListenThread;
        mcServer = par3MinecraftServer;
    }

    public void run()
    {
        do
        {
            if (!netWorkListener.isListening)
            {
                break;
            }

            try
            {
                Socket socket = NetworkListenThread.getServerSocket(netWorkListener).accept();

                if (socket == null)
                {
                    continue;
                }

                synchronized (NetworkListenThread.func_35504_b(netWorkListener))
                {
                    InetAddress inetaddress = socket.getInetAddress();

                    if (NetworkListenThread.func_35504_b(netWorkListener).containsKey(inetaddress) && !"127.0.0.1".equals(inetaddress.getHostAddress()) && System.currentTimeMillis() - ((Long)NetworkListenThread.func_35504_b(netWorkListener).get(inetaddress)).longValue() < 4000L)
                    {
                        NetworkListenThread.func_35504_b(netWorkListener).put(inetaddress, Long.valueOf(System.currentTimeMillis()));
                        socket.close();
                        continue;
                    }

                    NetworkListenThread.func_35504_b(netWorkListener).put(inetaddress, Long.valueOf(System.currentTimeMillis()));
                }

                NetLoginHandler netloginhandler = new NetLoginHandler(mcServer, socket, (new StringBuilder()).append("Connection #").append(NetworkListenThread.func_712_b(netWorkListener)).toString());
                NetworkListenThread.func_716_a(netWorkListener, netloginhandler);
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        }
        while (true);
    }
}
