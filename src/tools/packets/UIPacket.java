/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packets;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Flower
 */
public class UIPacket {

    /**
     * Sends a UI utility. 0x01 - Equipment Inventory. 0x02 - Stat Window. 0x03
     * - Skill Window. 0x05 - Keyboard Settings. 0x06 - Quest window. 0x09 -
     * Monsterbook Window. 0x0A - Char Info 0x0B - Guild BBS 0x12 - Monster
     * Carnival Window 0x16 - Party Search. 0x17 - Item Creation Window. 0x1A -
     * My Ranking O.O 0x1B - Family Window 0x1C - Family Pedigree 0x1D - GM
     * Story Board /funny shet 0x1E - Envelop saying you got mail from an admin.
     * lmfao 0x1F - Medal Window 0x20 - Maple Event (???) 0x21 - Invalid Pointer
     * Crash
     *
     * @param ui
     * @return
     */
    public static byte[] openUI(byte ui) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.OPEN_UI.getValue());
        mplew.write(ui);
        return mplew.getPacket();
    }

    public static byte[] lockUI(boolean enable) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.LOCK_UI.getValue());
        mplew.write(enable ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] disableUI(boolean enable) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DISABLE_UI.getValue());
        mplew.write(enable ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] sendPolice() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAKE_GM_NOTICE.getValue());
        mplew.write(0);//doesn't even matter what value
        return mplew.getPacket();
    }

    public static byte[] sendPolice(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DATA_CRC_CHECK_FAILED.getValue());
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

}
