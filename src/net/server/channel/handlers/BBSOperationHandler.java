package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.server.guild.MapleBBSThread;
import net.server.guild.MapleGuild;
import tools.DatabaseConnection;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.GuildPacket;

public final class BBSOperationHandler extends AbstractMaplePacketHandler {

    private String correctLength(String in, int maxSize) {
        return in.length() > maxSize ? in.substring(0, maxSize) : in;
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getGuildId() < 1) {
            return;
        }
        byte mode = slea.readByte();
        int localthreadid = 0;
        switch (mode) {
            case 0:
                boolean bEdit = slea.readByte() == 1;
                if (bEdit) {
                    localthreadid = slea.readInt();
                }
                boolean bNotice = slea.readByte() == 1;
                String title = correctLength(slea.readMapleAsciiString(), 25);
                String text = correctLength(slea.readMapleAsciiString(), 600);
                int icon = slea.readInt();
                if (icon >= 0x64 && icon <= 0x6a) {
                    if (c.getPlayer().getItemQuantity(5290000 + icon - 0x64, false) > 0) {
                        return;
                    }
                } else if (icon < 0 || icon > 3) {
                    return;
                }
                if (!bEdit) {
                    newBBSThread(c, title, text, icon, bNotice);
                } else {
                    editBBSThread(c, title, text, icon, localthreadid);
                }
                break;
            case 1:
                localthreadid = slea.readInt();
                deleteBBSThread(c, localthreadid);
                break;
            case 2:
                int start = slea.readInt();
                listBBSThreads(c, start * 10);
                break;
            case 3: // list thread + reply, followed by id (int)
                localthreadid = slea.readInt();
                displayThread(c, localthreadid);
                break;
            case 4: // reply
                localthreadid = slea.readInt();
                text = correctLength(slea.readMapleAsciiString(), 25);
                newBBSReply(c, localthreadid, text);
                break;
            case 5: // delete reply
                localthreadid = slea.readInt(); // we don't use this
                int replyid = slea.readInt();
                deleteBBSReply(c, localthreadid, replyid);
                break;
            default:
            //System.out.println("Unhandled BBS mode: " + slea.toString());
        }
    }

    private static void listBBSThreads(MapleClient c, int start) {
        MapleCharacter mc = c.getPlayer();
        MapleGuild guild = mc.getGuild();
        if (mc.getGuildId() <= 0 || guild == null) {
            return;
        }
        c.announce(GuildPacket.BBSThreadList(guild.getBBS(), start));
    }

    private static void newBBSReply(MapleClient c, int localthreadid, String text) {
        MapleCharacter mc = c.getPlayer();
        MapleGuild guild = mc.getGuild();
        if (mc.getGuildId() <= 0 || guild == null) {
            return;
        }
        guild.addBBSReply(localthreadid, text, mc.getId());
    }

    private static void editBBSThread(MapleClient c, String title, String text, int icon, int localthreadid) {
        MapleCharacter mc = c.getPlayer();
        MapleGuild guild = mc.getGuild();
        if (mc.getGuildId() <= 0 || guild == null) {
            return;
        }
        guild.editBBSThread(localthreadid, title, text, icon, mc.getId(), mc.getGuildRank());
        displayThread(c, localthreadid);
    }

    private static void newBBSThread(MapleClient c, String title, String text, int icon, boolean bNotice) {
        MapleCharacter mc = c.getPlayer();
        MapleGuild guild = mc.getGuild();
        if (mc.getGuildId() <= 0 || guild == null) {
            return;
        }
        int id = guild.addBBSThread(title, text, icon, bNotice, mc.getId());
        displayThread(c, id);
    }

    public static void deleteBBSThread(MapleClient client, int localthreadid) {
        MapleCharacter mc = client.getPlayer();
        MapleGuild guild = mc.getGuild();
        if (mc.getGuildId() <= 0 || guild == null) {
            return;
        }
        guild.deleteBBSThread(localthreadid, mc.getId(), mc.getGuildRank());
        displayThread(client, localthreadid);
    }

    public static void deleteBBSReply(MapleClient c, int localthreadid, int replyid) {
        MapleCharacter mc = c.getPlayer();
        MapleGuild guild = mc.getGuild();
        if (mc.getGuildId() <= 0 || guild == null) {
            return;
        }

        guild.deleteBBSReply(localthreadid, replyid, mc.getId(), mc.getGuildRank());
        displayThread(c, localthreadid);
    }

    public static void displayThread(MapleClient c, int threadid) {
        displayThread(c, threadid, true);
    }

    public static void displayThread(MapleClient c, int threadid, boolean bIsThreadIdLocal) {
        MapleCharacter mc = c.getPlayer();
        MapleGuild guild = mc.getGuild();
        if (mc.getGuildId() <= 0 || guild == null) {
            return;
        }

        final List<MapleBBSThread> bbsList = guild.getBBS();
        if (bbsList != null) {
            for (MapleBBSThread t : bbsList) {
                if (t != null && t.localthreadID == threadid) {
                    c.getSession().write(GuildPacket.showThread(t));
                }
            }
        }

    }
}
