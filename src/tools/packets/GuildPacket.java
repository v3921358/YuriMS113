/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packets;

import client.MapleCharacter;
import java.util.Collection;
import java.util.List;
import net.SendOpcode;
import net.server.guild.MapleBBSThread;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author user
 */
public class GuildPacket {

    public static byte[] showGuildInfo(MapleCharacter c, String prefix) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());

        mplew.write(0x1A); //signature for showing guild info

        if (c == null || c.getMGC() == null) {
            mplew.write(0);
            return mplew.getPacket();
        }

        MapleGuild g = c.getClient().getWorldServer().getGuild(c.getMGC());

        if (g == null) {
            mplew.write(0);
            return mplew.getPacket();
        }

        c.setGuildRank(c.getGuildRank());

        mplew.write(1);
        addGuildInfo(mplew, g);
 
        return mplew.getPacket();
    }

    private static void addGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
        addGuildInfo(mplew, guild, "");
    }

    private static void addGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild, String prefix) {
        
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(prefix.equals("") ? guild.getRankTitle(i) : "[" + prefix + "]" + guild.getRankTitle(i));
        }
        Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.write(members.size()); //then it is the size of all the members
        for (MapleGuildCharacter mgc : members) {//and each of their character ids o_O
            mplew.writeInt(mgc.getId());
        }
        for (MapleGuildCharacter mgc : members) {
            mplew.writeAsciiString(mgc.getName(), 15);
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(guild.getSignature());
            mplew.writeInt(mgc.getAllianceRank());
        }
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
    }

    public static byte[] showGuildInfo(MapleCharacter c) {
        return showGuildInfo(c, "");
    }

    public static byte[] guildMemberOnline(int gid, int cid, boolean bOnline) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3D);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.write(bOnline ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] guildInvite(int gid, String charName) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x05);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(charName);
        return mplew.getPacket();
    }

    /**
     * 'Char' has denied your guild invitation.
     *
     * @param charname
     * @return
     */
    public static byte[] denyGuildInvitation(String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x37);
        mplew.writeMapleAsciiString(charname);
        return mplew.getPacket();
    }

    public static byte[] genericGuildMessage(byte code) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(code);
        return mplew.getPacket();
    }

    public static byte[] newGuildMember(MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(mgc.getName(), 15);
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
        mplew.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
        mplew.writeInt(1); //? could be guild signature, but doesn't seem to matter
        mplew.writeInt(mgc.getAllianceRank());
        return mplew.getPacket();
    }

    //someone leaving, mode == 0x2c for leaving, 0x2f for expelled
    public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(bExpelled ? 0x2F : 0x2C);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());
        return mplew.getPacket();
    }

    //rank change
    public static byte[] changeRank(MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x40);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());
        return mplew.getPacket();
    }

    public static byte[] guildNotice(int gid, String notice) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x44);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(notice);
        return mplew.getPacket();
    }

    public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3C);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());
        return mplew.getPacket();
    }

    public static byte[] rankTitleChange(int gid, String[] ranks) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3E);
        mplew.writeInt(gid);
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString(ranks[i]);
        }
        return mplew.getPacket();
    }

    public static byte[] guildDisband(int gid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x32);
        mplew.writeInt(gid);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x42);
        mplew.writeInt(gid);
        mplew.writeShort(bg);
        mplew.write(bgcolor);
        mplew.writeShort(logo);
        mplew.write(logocolor);
        return mplew.getPacket();
    }

    public static byte[] guildCapacityChange(int gid, int capacity) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3A);
        mplew.writeInt(gid);
        mplew.write(capacity);
        return mplew.getPacket();
    }

    public static byte[] showThread(MapleBBSThread thread) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
        mplew.write(7);

        mplew.writeInt(thread.localthreadID);
        mplew.writeInt(thread.ownerID);
        mplew.writeLong(PacketUtil.getTime(thread.timestamp));
        mplew.writeMapleAsciiString(thread.name);
        mplew.writeMapleAsciiString(thread.text);
        mplew.writeInt(thread.icon);
        mplew.writeInt(thread.getReplyCount());
        for (MapleBBSThread.MapleBBSReply reply : thread.replies.values()) {
            mplew.writeInt(reply.replyid);
            mplew.writeInt(reply.ownerID);
            mplew.writeLong(PacketUtil.getTime(reply.timestamp));
            mplew.writeMapleAsciiString(reply.content);
        }
        return mplew.getPacket();
    }

    public static byte[] BBSThreadList(final List<MapleBBSThread> bbs, int start) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
        mplew.write(6);

        if (bbs == null) {
            mplew.write(0);
            mplew.writeLong(0);
            return mplew.getPacket();
        }

        int threadCount = bbs.size();

        MapleBBSThread notice = null;
        for (MapleBBSThread b : bbs) {
            if (b.isNotice()) { //notice
                notice = b;
                break;
            }
        }
        final int ret = (notice == null ? 0 : 1);
        mplew.write(ret);
        if (notice != null) { //has a notice
            addThread(mplew, notice);
            threadCount--; //one thread didn't count (because it's a notice)
        }
        if (threadCount < start) { //seek to the thread before where we start
            //uh, we're trying to start at a place past possible
            start = 0;
        }
        //each page has 10 threads, start = page # in packet but not here
        mplew.writeInt(threadCount);
        final int pages = Math.min(10, threadCount - start);
        mplew.writeInt(pages);

        for (int i = 0; i < pages; i++) {
            addThread(mplew, bbs.get(start + i + ret)); //because 0 = notice
        }
        return mplew.getPacket();
    }

    private static void addThread(MaplePacketLittleEndianWriter mplew, MapleBBSThread rs) {
        mplew.writeInt(rs.localthreadID);
        mplew.writeInt(rs.ownerID);
        mplew.writeMapleAsciiString(rs.name);
        mplew.writeLong(PacketUtil.getTime(rs.timestamp));
        mplew.writeInt(rs.icon);
        mplew.writeInt(rs.getReplyCount());
    }

}
