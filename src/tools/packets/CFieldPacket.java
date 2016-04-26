/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packets;

import client.MapleCharacter;
import java.net.InetAddress;
import net.SendOpcode;
import server.maps.MapleMap;
import tools.Randomizer;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author yuri
 */
public class CFieldPacket {
    
    public static byte[] getCharInfo(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(Randomizer.nextInt());
        }
        PacketHelper.addCharacterInfo(mplew, chr);
        mplew.writeLong(PacketUtil.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }
    
    public static byte[] addNewCharEntry(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(0);
        PacketHelper.addCharEntry(mplew, chr, false);
        return mplew.getPacket();
    }
    
    public static byte[] updateQuestInfo(short quest, int npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(8); //0x0A in v95
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] updateQuestFinish(short quest, int npc, short nextquest) { //Check
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue()); //0xF2 in v95
        mplew.write(8);//0x0A in v95
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeShort(nextquest);
        return mplew.getPacket();
    }
    
    public static byte[] skillCooldown(int sid, int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(time);//Int in v97
        return mplew.getPacket();
    }
    
    public static byte[] getChannelChange(InetAddress inetAddr, int port) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        return mplew.getPacket();
    }
    
    public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.writeInt(2);//updated
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getHp());
        mplew.write(0);
        mplew.writeLong(PacketUtil.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }
}
