package tools.packets;

import client.BuddylistEntry;
import client.MapleCharacter;
import client.MapleClient;
import java.util.Collection;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class BuddyPacket {

    public static byte[] updateBuddylist(MapleClient c, Collection<BuddylistEntry> buddylist) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());

        mplew.write(0x7);

        mplew.write(buddylist.size());

        for (BuddylistEntry buddy : buddylist) {
            if (buddy.isVisible()) {
                mplew.writeInt(buddy.getCharacterId());
                mplew.writeAsciiString(buddy.getName(), 15);
                mplew.write(0);
                mplew.writeInt(buddy.getChannel() == -1 ? -1 : buddy.getChannel() - 1);
                mplew.writeAsciiString(buddy.getGroup(), 17);
            }
        }

        for (BuddylistEntry buddy : buddylist) {
            MapleCharacter chr = c.getWorldServer().getPlayerStorage().getCharacterById(buddy.getCharacterId());
            mplew.writeInt(chr == null ? 0 : chr.getMapId());
        }

        return mplew.getPacket();
    }

    public static byte[] buddylistMessage(byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    public static byte[] requestBuddylistAdd(int cidFrom, int cid, String nameFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());

        mplew.write(0x9);

        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(nameFrom, 15);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeAsciiString("其他", 16);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] updateBuddyChannel(int characterid, int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());

        mplew.write(0x14);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);

        return mplew.getPacket();
    }

    public static byte[] updateBuddyCapacity(int capacity) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(0x15);
        mplew.write(capacity);

        return mplew.getPacket();
    }
}
