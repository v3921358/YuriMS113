package tools.packets;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;
import static tools.packets.MaplePacketCreator.updatePlayerStats;

/**
 *
 * @author Frz
 */
public class CWvsContext {    public static byte[] finishedSort(int inv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.SORT_ITEM_RESULT.getValue());
        mplew.write(0);
        mplew.write(inv);
        return mplew.getPacket();
    }

    public static byte[] finishedGather(int inv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.GATHER_ITEM_RESULT.getValue());
        mplew.write(0);
        mplew.write(inv);
        return mplew.getPacket();
    }
    
    
    public static byte[] enableActions() {
        return updatePlayerStats(PacketUtil.EMPTY_STATUPDATE, true);
    }
    
    public static byte[] broadcastMsg(String message) {
        return broadcastMessage(4, 0, message, false);
    }

    public static byte[] broadcastMsg(int type, String message) {
        return broadcastMessage(type, 0, message, false);
    }

    public static byte[] broadcastMsg(int type, int channel, String message) {
        return broadcastMessage(type, channel, message, false);
    }

    public static byte[] broadcastMsg(int type, int channel, String message, boolean smegaEar) {
        return broadcastMessage(type, channel, message, smegaEar);
    }

    /**
     * Gets a server message packet.
     *
     * Possible values for <code>type</code>:<br> 0: [Notice]<br> 1: Popup<br>
     * 2: Megaphone<br> 3: Super Megaphone<br> 4: Scrolling message at top<br>
     * 5: Pink Text<br> 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @param servermessage Is this a scrolling ticker?
     * @return The server notice packet.
     */
    private static byte[] broadcastMessage(int type, int channel, String message, boolean megaEar) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (type == 4) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);
        switch (type) {
            case 3:
            case 11:
            case 12:
                mplew.write(channel - 1); // channel
                mplew.writeBool(megaEar);
                break;
            case 9:
                mplew.write(channel - 1);
                break;
            case 6:
            case 7:
                mplew.writeInt(0);

        }
        return mplew.getPacket();
    }
}
