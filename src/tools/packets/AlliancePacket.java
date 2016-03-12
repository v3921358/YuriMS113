package tools.packets;

import client.MapleCharacter;
import client.MapleClient;
import java.util.Collection;
import net.SendOpcode;
import net.server.Server;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import tools.data.output.MaplePacketLittleEndianWriter;

public class AlliancePacket {

    public static byte[] getAllianceInfo(MapleAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0C);
        mplew.write(alliance == null ? 0 : 1);
        if (alliance != null) {
            addAllianceInfo(mplew, alliance);
        }
        return mplew.getPacket();
    }

    public static byte[] makeNewAlliance(MapleAlliance alliance, MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0F);
        addAllianceInfo(mplew, alliance);
        for (Integer guildd : alliance.getGuilds()) {
            addGuildInfo(mplew, Server.getInstance().getGuild(guildd, c.getPlayer().getMGC()));
        }
        return mplew.getPacket();
    }

    public static byte[] getGuildAlliances(MapleAlliance alliance, MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0D);
        mplew.writeInt(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            addGuildInfo(mplew, Server.getInstance().getGuild(guild, null));
        }
        return mplew.getPacket();
    }

    public static byte[] addGuildToAlliance(MapleAlliance alliance, int newGuild, MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x12);
        addAllianceInfo(mplew, alliance);
        mplew.writeInt(newGuild);
        addGuildInfo(mplew, Server.getInstance().getGuild(newGuild, null));
        return mplew.getPacket();
    }

    public static byte[] allianceMemberOnline(MapleCharacter mc, boolean online) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0E);
        mplew.writeInt(mc.getGuild().getAllianceId());
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        mplew.write(online ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] allianceNotice(int id, String notice) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1C);
        mplew.writeInt(id);
        mplew.writeMapleAsciiString(notice);
        return mplew.getPacket();
    }

    public static byte[] changeAllianceRankTitle(int alliance, String[] ranks) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1A);
        mplew.writeInt(alliance);
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString(ranks[i]);
        }
        return mplew.getPacket();
    }

    public static byte[] updateAllianceJobLevel(MapleCharacter mc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x18);
        mplew.writeInt(mc.getGuild().getAllianceId());
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        mplew.writeInt(mc.getLevel());
        mplew.writeInt(mc.getJob().getId());
        return mplew.getPacket();
    }

    public static byte[] removeGuildFromAlliance(MapleAlliance alliance, int expelledGuild, MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x10);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(2);
        mplew.writeMapleAsciiString(alliance.getNotice());
        mplew.writeInt(expelledGuild);
        addGuildInfo(mplew, Server.getInstance().getGuild(expelledGuild, null));
        mplew.write(0x01);
        return mplew.getPacket();
    }

    public static byte[] disbandAlliance(int alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1D);
        mplew.writeInt(alliance);
        return mplew.getPacket();
    }

    private static void addGuildInfo(final MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.write(members.size());
        for (MapleGuildCharacter mgc : members) {
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
        mplew.writeInt(guild.getAllianceId());
    }

    private static void addAllianceInfo(MaplePacketLittleEndianWriter mplew, MapleAlliance alliance) {
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(alliance.getCapacity()); // ????
        mplew.writeMapleAsciiString(alliance.getNotice());
    }

}
