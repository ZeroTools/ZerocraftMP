package net.minecraft.src;

import java.util.*;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;

public class ConsoleCommandHandler
{
    private static Logger minecraftLogger = Logger.getLogger("Minecraft");

    /** Stores a reference to the Server */
    private MinecraftServer minecraftServer;

    public ConsoleCommandHandler(MinecraftServer par1MinecraftServer)
    {
        minecraftServer = par1MinecraftServer;
    }

    /**
     * handles the command that was issued by an Op/Console
     */
    public synchronized void handleCommand(ServerCommand par1ServerCommand)
    {
        String s = par1ServerCommand.command;
        String as[] = s.split(" ");
        String s1 = as[0];
        String s2 = s.substring(s1.length()).trim();
        ICommandListener icommandlistener = par1ServerCommand.commandListener;
        String s3 = icommandlistener.getUsername();
        ServerConfigurationManager serverconfigurationmanager = minecraftServer.configManager;

        if (s1.equalsIgnoreCase("help") || s1.equalsIgnoreCase("?"))
        {
            printHelp(icommandlistener);
        }
        else if (s1.equalsIgnoreCase("list"))
        {
            icommandlistener.log((new StringBuilder()).append("Connected players: ").append(serverconfigurationmanager.getPlayerList()).toString());
        }
        else if (s1.equalsIgnoreCase("stop"))
        {
            sendNoticeToOps(s3, "Stopping the server..");
            minecraftServer.initiateShutdown();
        }
        else if (s1.equalsIgnoreCase("save-all"))
        {
            sendNoticeToOps(s3, "Forcing save..");

            if (serverconfigurationmanager != null)
            {
                serverconfigurationmanager.savePlayerStates();
            }

            for (int i = 0; i < minecraftServer.worldMngr.length; i++)
            {
                WorldServer worldserver = minecraftServer.worldMngr[i];
                boolean flag = worldserver.levelSaving;
                worldserver.levelSaving = false;
                worldserver.saveWorld(true, null);
                worldserver.levelSaving = flag;
            }

            sendNoticeToOps(s3, "Save complete.");
        }
        else if (s1.equalsIgnoreCase("save-off"))
        {
            sendNoticeToOps(s3, "Disabling level saving..");

            for (int j = 0; j < minecraftServer.worldMngr.length; j++)
            {
                WorldServer worldserver1 = minecraftServer.worldMngr[j];
                worldserver1.levelSaving = true;
            }
        }
        else if (s1.equalsIgnoreCase("save-on"))
        {
            sendNoticeToOps(s3, "Enabling level saving..");

            for (int k = 0; k < minecraftServer.worldMngr.length; k++)
            {
                WorldServer worldserver2 = minecraftServer.worldMngr[k];
                worldserver2.levelSaving = false;
            }
        }
        else if (s1.equalsIgnoreCase("op"))
        {
            serverconfigurationmanager.addOp(s2);
            sendNoticeToOps(s3, (new StringBuilder()).append("Opping ").append(s2).toString());
            serverconfigurationmanager.sendChatMessageToPlayer(s2, "\247eYou are now op!");
        }
        else if (s1.equalsIgnoreCase("deop"))
        {
            String s4 = s2;
            serverconfigurationmanager.removeOp(s4);
            serverconfigurationmanager.sendChatMessageToPlayer(s4, "\247eYou are no longer op!");
            sendNoticeToOps(s3, (new StringBuilder()).append("De-opping ").append(s4).toString());
        }
        else if (s1.equalsIgnoreCase("ban-ip"))
        {
            String s5 = s2;
            serverconfigurationmanager.banIP(s5);
            sendNoticeToOps(s3, (new StringBuilder()).append("Banning ip ").append(s5).toString());
        }
        else if (s1.equalsIgnoreCase("pardon-ip"))
        {
            String s6 = s2;
            serverconfigurationmanager.pardonIP(s6);
            sendNoticeToOps(s3, (new StringBuilder()).append("Pardoning ip ").append(s6).toString());
        }
        else if (s1.equalsIgnoreCase("ban"))
        {
            String s7 = s2;
            serverconfigurationmanager.banPlayer(s7);
            sendNoticeToOps(s3, (new StringBuilder()).append("Banning ").append(s7).toString());
            EntityPlayerMP entityplayermp1 = serverconfigurationmanager.getPlayerEntity(s7);

            if (entityplayermp1 != null)
            {
                entityplayermp1.playerNetServerHandler.kickPlayer("Banned by admin");
            }
        }
        else if (s1.equalsIgnoreCase("pardon"))
        {
            String s8 = s2;
            serverconfigurationmanager.pardonPlayer(s8);
            sendNoticeToOps(s3, (new StringBuilder()).append("Pardoning ").append(s8).toString());
        }
        else if (s1.equalsIgnoreCase("kick"))
        {
            String s9 = s2;
            EntityPlayerMP entityplayermp2 = null;

            for (int i1 = 0; i1 < serverconfigurationmanager.playerEntities.size(); i1++)
            {
                EntityPlayerMP entityplayermp7 = (EntityPlayerMP)serverconfigurationmanager.playerEntities.get(i1);

                if (entityplayermp7.username.equalsIgnoreCase(s9))
                {
                    entityplayermp2 = entityplayermp7;
                }
            }

            if (entityplayermp2 != null)
            {
                entityplayermp2.playerNetServerHandler.kickPlayer("Kicked by admin");
                sendNoticeToOps(s3, (new StringBuilder()).append("Kicking ").append(entityplayermp2.username).toString());
            }
            else
            {
                icommandlistener.log((new StringBuilder()).append("Can't find user ").append(s9).append(". No kick.").toString());
            }
        }
        else if (s1.equalsIgnoreCase("tp"))
        {
            if (as.length == 3)
            {
                EntityPlayerMP entityplayermp = serverconfigurationmanager.getPlayerEntity(as[1]);
                EntityPlayerMP entityplayermp3 = serverconfigurationmanager.getPlayerEntity(as[2]);

                if (entityplayermp == null)
                {
                    icommandlistener.log((new StringBuilder()).append("Can't find user ").append(as[1]).append(". No tp.").toString());
                }
                else if (entityplayermp3 == null)
                {
                    icommandlistener.log((new StringBuilder()).append("Can't find user ").append(as[2]).append(". No tp.").toString());
                }
                else if (entityplayermp.dimension != entityplayermp3.dimension)
                {
                    icommandlistener.log((new StringBuilder()).append("User ").append(as[1]).append(" and ").append(as[2]).append(" are in different dimensions. No tp.").toString());
                }
                else
                {
                    entityplayermp.playerNetServerHandler.teleportTo(entityplayermp3.posX, entityplayermp3.posY, entityplayermp3.posZ, entityplayermp3.rotationYaw, entityplayermp3.rotationPitch);
                    sendNoticeToOps(s3, (new StringBuilder()).append("Teleporting ").append(as[1]).append(" to ").append(as[2]).append(".").toString());
                }
            }
            else
            {
                icommandlistener.log("Syntax error, please provide a source and a target.");
            }
        }
        else if (s1.equalsIgnoreCase("give"))
        {
            if (as.length != 3 && as.length != 4 && as.length != 5)
            {
                return;
            }

            String s10 = as[1];
            EntityPlayerMP entityplayermp4 = serverconfigurationmanager.getPlayerEntity(s10);

            if (entityplayermp4 != null)
            {
                try
                {
                    int j1 = Integer.parseInt(as[2]);

                    if (Item.itemsList[j1] != null)
                    {
                        sendNoticeToOps(s3, (new StringBuilder()).append("Giving ").append(entityplayermp4.username).append(" some ").append(j1).toString());
                        int k2 = 1;
                        int l2 = 0;

                        if (as.length > 3)
                        {
                            k2 = tryParse(as[3], 1);
                        }

                        if (as.length > 4)
                        {
                            l2 = tryParse(as[4], 1);
                        }

                        if (k2 < 1)
                        {
                            k2 = 1;
                        }

                        if (k2 > 64)
                        {
                            k2 = 64;
                        }

                        entityplayermp4.dropPlayerItem(new ItemStack(j1, k2, l2));
                    }
                    else
                    {
                        icommandlistener.log((new StringBuilder()).append("There's no item with id ").append(j1).toString());
                    }
                }
                catch (NumberFormatException numberformatexception1)
                {
                    icommandlistener.log((new StringBuilder()).append("There's no item with id ").append(as[2]).toString());
                }
            }
            else
            {
                icommandlistener.log((new StringBuilder()).append("Can't find user ").append(s10).toString());
            }
        }
        else if (s1.equalsIgnoreCase("xp"))
        {
            if (as.length != 3)
            {
                return;
            }

            String s11 = as[1];
            EntityPlayerMP entityplayermp5 = serverconfigurationmanager.getPlayerEntity(s11);

            if (entityplayermp5 != null)
            {
                try
                {
                    int k1 = Integer.parseInt(as[2]);
                    k1 = k1 <= 5000 ? k1 : 5000;
                    sendNoticeToOps(s3, (new StringBuilder()).append("Giving ").append(k1).append(" orbs to ").append(entityplayermp5.username).toString());
                    entityplayermp5.addExperience(k1);
                }
                catch (NumberFormatException numberformatexception2)
                {
                    icommandlistener.log((new StringBuilder()).append("Invalid orb count: ").append(as[2]).toString());
                }
            }
            else
            {
                icommandlistener.log((new StringBuilder()).append("Can't find user ").append(s11).toString());
            }
        }
        else if (s1.equalsIgnoreCase("gamemode"))
        {
            if (as.length != 3)
            {
                return;
            }

            String s12 = as[1];
            EntityPlayerMP entityplayermp6 = serverconfigurationmanager.getPlayerEntity(s12);

            if (entityplayermp6 != null)
            {
                try
                {
                    int l1 = Integer.parseInt(as[2]);
                    l1 = WorldSettings.validGameType(l1);

                    if (entityplayermp6.itemInWorldManager.getGameType() != l1)
                    {
                        sendNoticeToOps(s3, (new StringBuilder()).append("Setting ").append(entityplayermp6.username).append(" to game mode ").append(l1).toString());
                        entityplayermp6.itemInWorldManager.toggleGameType(l1);
                        entityplayermp6.playerNetServerHandler.sendPacket(new Packet70Bed(3, l1));
                    }
                    else
                    {
                        sendNoticeToOps(s3, (new StringBuilder()).append(entityplayermp6.username).append(" already has game mode ").append(l1).toString());
                    }
                }
                catch (NumberFormatException numberformatexception3)
                {
                    icommandlistener.log((new StringBuilder()).append("There's no game mode with id ").append(as[2]).toString());
                }
            }
            else
            {
                icommandlistener.log((new StringBuilder()).append("Can't find user ").append(s12).toString());
            }
        }
        else if (s1.equalsIgnoreCase("time"))
        {
            if (as.length != 3)
            {
                return;
            }

            String s13 = as[1];

            try
            {
                int l = Integer.parseInt(as[2]);

                if ("add".equalsIgnoreCase(s13))
                {
                    for (int i2 = 0; i2 < minecraftServer.worldMngr.length; i2++)
                    {
                        WorldServer worldserver3 = minecraftServer.worldMngr[i2];
                        worldserver3.advanceTime(worldserver3.getWorldTime() + (long)l);
                    }

                    sendNoticeToOps(s3, (new StringBuilder()).append("Added ").append(l).append(" to time").toString());
                }
                else if ("set".equalsIgnoreCase(s13))
                {
                    for (int j2 = 0; j2 < minecraftServer.worldMngr.length; j2++)
                    {
                        WorldServer worldserver4 = minecraftServer.worldMngr[j2];
                        worldserver4.advanceTime(l);
                    }

                    sendNoticeToOps(s3, (new StringBuilder()).append("Set time to ").append(l).toString());
                }
                else
                {
                    icommandlistener.log("Unknown method, use either \"add\" or \"set\"");
                }
            }
            catch (NumberFormatException numberformatexception)
            {
                icommandlistener.log((new StringBuilder()).append("Unable to convert time value, ").append(as[2]).toString());
            }
        }
        else if (s1.equalsIgnoreCase("say") && s2.length() > 0)
        {
            minecraftLogger.info((new StringBuilder()).append("[").append(s3).append("] ").append(s2).toString());
            serverconfigurationmanager.sendPacketToAllPlayers(new Packet3Chat((new StringBuilder()).append("\247d[Server] ").append(s2).toString()));
        }
        else if (s1.equalsIgnoreCase("tell"))
        {
            if (as.length >= 3)
            {
                s = s.substring(s.indexOf(" ")).trim();
                s = s.substring(s.indexOf(" ")).trim();
                minecraftLogger.info((new StringBuilder()).append("[").append(s3).append("->").append(as[1]).append("] ").append(s).toString());
                s = (new StringBuilder()).append("\2477").append(s3).append(" whispers ").append(s).toString();
                minecraftLogger.info(s);

                if (!serverconfigurationmanager.sendPacketToPlayer(as[1], new Packet3Chat(s)))
                {
                    icommandlistener.log("There's no player by that name online.");
                }
            }
        }
        else if (s1.equalsIgnoreCase("whitelist"))
        {
            handleWhitelist(s3, s, icommandlistener);
        }
        else if (s1.equalsIgnoreCase("toggledownfall"))
        {
            minecraftServer.worldMngr[0].commandToggleDownfall();
            icommandlistener.log("Toggling rain and snow, hold on...");
        }
        else if (s1.equalsIgnoreCase("banlist"))
        {
            if (as.length == 2)
            {
                if (as[1].equals("ips"))
                {
                    icommandlistener.log((new StringBuilder()).append("IP Ban list:").append(func_40648_a(minecraftServer.getBannedIPsList(), ", ")).toString());
                }
            }
            else
            {
                icommandlistener.log((new StringBuilder()).append("Ban list:").append(func_40648_a(minecraftServer.getBannedPlayersList(), ", ")).toString());
            }
        }
        else
        {
            minecraftLogger.info("Unknown console command. Type \"help\" for help.");
        }
    }

    /**
     * Handles the whitelist command
     */
    private void handleWhitelist(String par1Str, String par2Str, ICommandListener par3ICommandListener)
    {
        String as[] = par2Str.split(" ");

        if (as.length < 2)
        {
            return;
        }

        String s = as[1].toLowerCase();

        if ("on".equals(s))
        {
            sendNoticeToOps(par1Str, "Turned on white-listing");
            minecraftServer.propertyManagerObj.setProperty("white-list", true);
        }
        else if ("off".equals(s))
        {
            sendNoticeToOps(par1Str, "Turned off white-listing");
            minecraftServer.propertyManagerObj.setProperty("white-list", false);
        }
        else if ("list".equals(s))
        {
            Set set = minecraftServer.configManager.getWhiteListedIPs();
            String s3 = "";

            for (Iterator iterator = set.iterator(); iterator.hasNext();)
            {
                String s4 = (String)iterator.next();
                s3 = (new StringBuilder()).append(s3).append(s4).append(" ").toString();
            }

            par3ICommandListener.log((new StringBuilder()).append("White-listed players: ").append(s3).toString());
        }
        else if ("add".equals(s) && as.length == 3)
        {
            String s1 = as[2].toLowerCase();
            minecraftServer.configManager.addToWhiteList(s1);
            sendNoticeToOps(par1Str, (new StringBuilder()).append("Added ").append(s1).append(" to white-list").toString());
        }
        else if ("remove".equals(s) && as.length == 3)
        {
            String s2 = as[2].toLowerCase();
            minecraftServer.configManager.removeFromWhiteList(s2);
            sendNoticeToOps(par1Str, (new StringBuilder()).append("Removed ").append(s2).append(" from white-list").toString());
        }
        else if ("reload".equals(s))
        {
            minecraftServer.configManager.reloadWhiteList();
            sendNoticeToOps(par1Str, "Reloaded white-list from file");
        }
    }

    /**
     * Print help on server commands
     */
    private void printHelp(ICommandListener par1ICommandListener)
    {
        par1ICommandListener.log("To run the server without a gui, start it like this:");
        par1ICommandListener.log("   java -Xmx1024M -Xms1024M -jar minecraft_server.jar nogui");
        par1ICommandListener.log("Console commands:");
        par1ICommandListener.log("   help  or  ?               shows this message");
        par1ICommandListener.log("   kick <player>             removes a player from the server");
        par1ICommandListener.log("   ban <player>              bans a player from the server");
        par1ICommandListener.log("   pardon <player>           pardons a banned player so that they can connect again");
        par1ICommandListener.log("   ban-ip <ip>               bans an IP address from the server");
        par1ICommandListener.log("   pardon-ip <ip>            pardons a banned IP address so that they can connect again");
        par1ICommandListener.log("   op <player>               turns a player into an op");
        par1ICommandListener.log("   deop <player>             removes op status from a player");
        par1ICommandListener.log("   tp <player1> <player2>    moves one player to the same location as another player");
        par1ICommandListener.log("   give <player> <id> [num]  gives a player a resource");
        par1ICommandListener.log("   tell <player> <message>   sends a private message to a player");
        par1ICommandListener.log("   stop                      gracefully stops the server");
        par1ICommandListener.log("   save-all                  forces a server-wide level save");
        par1ICommandListener.log("   save-off                  disables terrain saving (useful for backup scripts)");
        par1ICommandListener.log("   save-on                   re-enables terrain saving");
        par1ICommandListener.log("   list                      lists all currently connected players");
        par1ICommandListener.log("   say <message>             broadcasts a message to all players");
        par1ICommandListener.log("   time <add|set> <amount>   adds to or sets the world time (0-24000)");
        par1ICommandListener.log("   gamemode <player> <mode>  sets player's game mode (0 or 1)");
        par1ICommandListener.log("   toggledownfall            toggles rain on or off");
        par1ICommandListener.log("   xp <player> <amount>      gives the player the amount of xp (0-5000)");
    }

    /**
     * sends a notice to all online ops.
     */
    private void sendNoticeToOps(String par1Str, String par2Str)
    {
        String s = (new StringBuilder()).append(par1Str).append(": ").append(par2Str).toString();
        minecraftServer.configManager.sendChatMessageToAllOps((new StringBuilder()).append("\2477(").append(s).append(")").toString());
        minecraftLogger.info(s);
    }

    /**
     * Parses First argument if possible; if not returns second argument
     */
    private int tryParse(String par1Str, int par2)
    {
        try
        {
            return Integer.parseInt(par1Str);
        }
        catch (NumberFormatException numberformatexception)
        {
            return par2;
        }
    }

    private String func_40648_a(String par1ArrayOfStr[], String par2Str)
    {
        int i = par1ArrayOfStr.length;

        if (0 == i)
        {
            return "";
        }

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(par1ArrayOfStr[0]);

        for (int j = 1; j < i; j++)
        {
            stringbuilder.append(par2Str).append(par1ArrayOfStr[j]);
        }

        return stringbuilder.toString();
    }
}
