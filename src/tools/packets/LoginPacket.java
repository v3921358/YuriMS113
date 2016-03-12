/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packets;

import client.MapleCharacter;
import client.MapleClient;
import java.net.InetAddress;
import java.util.List;
import net.SendOpcode;
import net.server.Server;
import net.server.channel.Channel;
import tools.data.output.MaplePacketLittleEndianWriter;

public class LoginPacket {

    /**
     * 角色列表的封包
     *
     * @param c 要讀取的 MapleClient
     * @param serverId 所在的伺服器ID.
     * @return 角色列表封包.
     */
    public static final byte[] getCharList(MapleClient c, int serverId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHARLIST.getValue());
        mplew.write(0);
        mplew.writeInt(c.getAccID());
        List<MapleCharacter> chars = c.loadCharacters(serverId);
        mplew.write((byte) chars.size());
        for (MapleCharacter chr : chars) {
            MaplePacketCreator.addCharEntry(mplew, chr, false);
        }
        mplew.writeShort(3);
        mplew.writeInt(c.getCharacterSlots());

        return mplew.getPacket();
    }

    public static final byte[] getGenderChanged(final MapleClient client) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.GENDER_SET.getValue());
        mplew.writeMapleAsciiString(client.getAccountName());

        return mplew.getPacket();
    }

    public static final byte[] getGenderNeeded(final MapleClient client) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.CHOOSE_GENDER.getValue());
        mplew.writeMapleAsciiString(client.getAccountName());

        return mplew.getPacket();
    }

    /**
     * Gets a login failed packet.
     *
     * Possible values for <code>reason</code>:<br> 3: ID deleted or blocked<br>
     * 4: Incorrect password<br> 5: Not a registered id<br> 6: System error<br>
     * 7: Already logged in<br> 8: System error<br> 9: System error<br> 10:
     * Cannot process so many connections<br> 11: Only users older than 20 can
     * use this channel<br> 13: Unable to log on as master at this ip<br> 14:
     * Wrong gateway or personal info and weird korean button<br> 15: Processing
     * request with that korean button!<br> 16: Please verify your account
     * through email...<br> 17: Wrong gateway or personal info<br> 21: Please
     * verify your account through email...<br> 23: License agreement<br> 25:
     * Maple Europe notice =[ FUCK YOU NEXON<br> 27: Some weird full client
     * notice, probably for trial versions<br>
     *
     * @param reason The reason logging in failed.
     * @return The login failed packet.
     */
    public static final byte[] getLoginFailed(int reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.write(reason);
        mplew.write(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static final byte[] getPermBan(byte reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.write(2); // Account is banned
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeLong(PacketUtil.getTime(-1));

        return mplew.getPacket();
    }

    public static byte[] getTempBan(long timestampTill, byte reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.write(2);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(reason);
        mplew.writeLong(PacketUtil.getTime(timestampTill)); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.
        return mplew.getPacket();
    }

    /**
     * 取得登入成功的封包.
     *
     *
     * @param c 該帳號的 MapleClient
     * @param account 帳號名稱.
     * @return 登入成功的封包.
     */
    public static byte[] getAuthSuccess(MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.write(0);
        mplew.writeInt(c.getAccID());
        mplew.write(c.getGender());
        mplew.writeShort(c.gmLevel() > 0 ? 1 : 0); //admin byte
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(c.getAccountName());
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(0);

        return mplew.getPacket();
    }

    /**
     * 取得伺服器列表的封包.
     *
     * @param serverId
     * @param serverName The name of the server.
     * @param channelLoad Load of the channel - 1200 seems to be max.
     * @return The server info packet.
     */
    public static byte[] getServerList(int serverId, String serverName, int flag, String eventmsg, List<Channel> channelLoad) {
        
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERLIST.getValue());

        mplew.write(serverId);
        mplew.writeMapleAsciiString(serverName);
        mplew.write(flag);
        mplew.writeMapleAsciiString(eventmsg);
        mplew.write(100); // rate modifier, don't ask O.O!
        mplew.write(0); // event xp * 2.6 O.O!
        mplew.write(100); // rate modifier, don't ask O.O!
        mplew.write(0); // drop rate * 2.6
        mplew.write(channelLoad.size());
        for (Channel ch : channelLoad) {
            mplew.writeMapleAsciiString(serverName + "-" + ch.getId());
            mplew.writeInt((ch.getConnectedClients() * 1200) / Server.getChannelMaxPlayer());
            mplew.write(1);
            mplew.writeShort(ch.getId() - 1);
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet saying that the server list is over.
     *
     * @return The end of server list packet.
     */
    public static byte[] getEndOfServerList() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.SERVERLIST.getValue());
        mplew.write(0xFF);
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a server status message.
     *
     * Possible values for <code>status</code>:<br> 0 - Normal<br> 1 - Highly
     * populated<br> 2 - Full
     *
     * @param status The server status.
     * @return The server status packet.
     */
    public static byte[] getServerStatus(int status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.SERVERSTATUS.getValue());
        mplew.writeShort(status);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the channel server.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @param clientId The ID of the client.
     * @return The server IP packet.
     */
    public static byte[] getServerIP(InetAddress inetAddr, int port, int clientId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.write(new byte[]{0, 0, 0, 0, 0});
        return mplew.getPacket();
    }
}
