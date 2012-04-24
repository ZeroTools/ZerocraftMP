package net.minecraft.src;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class RConThreadClient extends RConThreadBase
{
    /**
     * True if the client has succefssfully logged into the RCon, otherwise false
     */
    private boolean loggedIn;

    /** The client's Socket connection */
    private Socket clientSocket;
    private byte buffer[];

    /** The RCon password */
    private String rconPassword;

    RConThreadClient(IServer par1IServer, Socket par2Socket)
    {
        super(par1IServer);
        loggedIn = false;
        buffer = new byte[1460];
        clientSocket = par2Socket;
        rconPassword = par1IServer.getStringProperty("rcon.password", "");
        log((new StringBuilder()).append("Rcon connection from: ").append(par2Socket.getInetAddress()).toString());
    }

    public void run()
    {
        try
        {
            while (true)
            {
                if (!running)
                {
                    break;
                }

                try
                {
                    BufferedInputStream bufferedinputstream = new BufferedInputStream(clientSocket.getInputStream());
                    int i = bufferedinputstream.read(buffer, 0, 1460);

                    if (10 > i)
                    {
                        return;
                    }

                    int j = 0;
                    int k = RConUtils.getBytesAsLEInt(buffer, 0, i);

                    if (k != i - 4)
                    {
                        return;
                    }

                    j += 4;
                    int l = RConUtils.getBytesAsLEInt(buffer, j, i);
                    j += 4;
                    int i1 = RConUtils.getRemainingBytesAsLEInt(buffer, j);
                    j += 4;

                    switch (i1)
                    {
                        case 3:
                            String s = RConUtils.getBytesAsString(buffer, j, i);
                            j += s.length();

                            if (0 != s.length() && s.equals(rconPassword))
                            {
                                loggedIn = true;
                                sendResponse(l, 2, "");
                            }
                            else
                            {
                                loggedIn = false;
                                sendLoginFailedResponse();
                            }

                            break;

                        case 2:
                            if (loggedIn)
                            {
                                String s1 = RConUtils.getBytesAsString(buffer, j, i);

                                try
                                {
                                    sendMultipacketResponse(l, server.handleRConCommand(s1));
                                }
                                catch (Exception exception1)
                                {
                                    sendMultipacketResponse(l, (new StringBuilder()).append("Error executing: ").append(s1).append(" (").append(exception1.getMessage()).append(")").toString());
                                }
                            }
                            else
                            {
                                sendLoginFailedResponse();
                            }

                            break;

                        default:
                            sendMultipacketResponse(l, String.format("Unknown request %s", new Object[]
                                    {
                                        Integer.toHexString(i1)
                                }));
                            break;
                    }
                }
                catch (SocketTimeoutException sockettimeoutexception) { }
                catch (IOException ioexception)
                {
                    if (running)
                    {
                        log((new StringBuilder()).append("IO: ").append(ioexception.getMessage()).toString());
                    }
                }
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }
        finally
        {
            closeSocket();
        }
    }

    /**
     * Sends the given response message to the client
     */
    private void sendResponse(int par1, int par2, String par3Str) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(1248);
        DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
        dataoutputstream.writeInt(Integer.reverseBytes(par3Str.length() + 10));
        dataoutputstream.writeInt(Integer.reverseBytes(par1));
        dataoutputstream.writeInt(Integer.reverseBytes(par2));
        dataoutputstream.writeBytes(par3Str);
        dataoutputstream.write(0);
        dataoutputstream.write(0);
        clientSocket.getOutputStream().write(bytearrayoutputstream.toByteArray());
    }

    /**
     * Sends the standard RCon 'authorization failed' response packet
     */
    private void sendLoginFailedResponse() throws IOException
    {
        sendResponse(-1, 2, "");
    }

    /**
     * Splits the response message into individual packets and sends each one
     */
    private void sendMultipacketResponse(int par1, String par2Str) throws IOException
    {
        int i = par2Str.length();

        do
        {
            int j = 4096 > i ? i : 4096;
            sendResponse(par1, 0, par2Str.substring(0, j));
            par2Str = par2Str.substring(j);
            i = par2Str.length();
        }
        while (0 != i);
    }

    /**
     * Closes the client socket
     */
    private void closeSocket()
    {
        if (null == clientSocket)
        {
            return;
        }

        try
        {
            clientSocket.close();
        }
        catch (IOException ioexception)
        {
            logWarning((new StringBuilder()).append("IO: ").append(ioexception.getMessage()).toString());
        }

        clientSocket = null;
    }
}
