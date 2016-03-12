/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packets;

import client.MapleCharacter;
import client.MapleFamilyEntry;
import java.util.Map;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author flyx
 */
public class FamilyPacket {

    public static byte[] loadFamily(MapleCharacter player) {

        String[] title = {
            "立刻移動至家族成員",
            "立刻召喚家族成員",
            "我的掉寶率1.5倍(15 分鐘)",
            "我的經驗值1.5倍(15分鐘)",
            "家族成員的團結(30分鐘)",
            "我的掉寶率 2倍(15分鐘)",
            "我的經驗值2倍(15分鐘)",
            "我的掉寶率2倍(30分鐘)",
            "我的經驗值2倍(30分鐘)",
            "我的隊伍掉寶率2倍",
            "我的隊伍經驗值2倍"
        };
        String[] description = {
            "[對象] 自己\n[效果] 移動到想要的上線家族成員所在地圖。",
            "[對象] 1個家族對象\n[效果] 召喚指定的上線家族成員到自己所在的地圖。",
            "[對象] 自己\n[時間] 15 分鐘.\n[效果] 獵捕怪物的掉寶率提升 #c1.5倍#\n* 如果有使用其他加倍這效果將無效.",
            "[對象] 自己\n[時間] 15 分鐘.\n[效果] 獵捕怪物的經驗值提升 #c1.5倍#\n* 如果有使用其他加倍這效果將無效.",
            "[對象] 至少有6個成員以上在線上\n[時間] 30 分鐘　\n[效果] 獵捕怪物的掉寶率和經驗值提升 #c2倍#\n* 如果有使用其他加倍這效果將無效.",
            "[對象] 自己\n[時間] 15 分鐘.\n[效果] 獵捕怪物的掉寶率提升 #c2倍#\n* 如果有使用其他加倍這效果將無效.",
            "[對象] 自己\n[時間] 15 分鐘.\n[效果] 獵捕怪物的經驗值提升 #c2倍#\n* 如果有使用其他加倍這效果將無效.",
            "[對象] 自己\n[時間] 30 分鐘.\n[效果] 獵捕怪物的掉寶率提升 #c2倍#\n* 如果有使用其他加倍這效果將無效.",
            "[對象] 自己\n[時間] 30 分鐘.\n[效果] 獵捕怪物的經驗值提升 #c2倍#\n* 如果有使用其他加倍這效果將無效.",
            "[對象] 我的隊伍\n[時間] 30 分鐘.\n[效果] 同一張地圖內的所屬隊伍成員掉寶率提升 #c2倍#\n* 如果有使用其他加倍這效果將無效.",
            "[對象] 我的隊伍\n[時間] 30 分鐘.\n[效果] 同一張地圖內的所屬隊伍成員經驗值提升 #c2倍#\n* 如果有使用其他加倍這效果將無效."
        };
        int[] repCost = {
            3, 5, 7, 8, 10, 12,
            15, 20, 25, 40, 50
        };
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_PRIVILEGE_LIST.getValue());
        mplew.writeInt(11);
        for (int i = 0; i < 11; i++) {
            mplew.write(i > 4 ? (i % 2) + 1 : i);
            mplew.writeInt(repCost[i] * 100);
            mplew.writeInt(1);
            mplew.writeMapleAsciiString(title[i]);
            mplew.writeMapleAsciiString(description[i]);
        }
        return mplew.getPacket();
    }

    /**
     * Family Result Message
     *
     * Possible values for <code>type</code>:<br>
     * 67: You do not belong to the same family.<br>
     * 69: The character you wish to add as\r\na Junior must be in the same
     * map.<br>
     * 70: This character is already a Junior of another character.<br>
     * 71: The Junior you wish to add\r\nmust be at a lower rank.<br>
     * 72: The gap between you and your\r\njunior must be within 20 levels.<br>
     * 73: Another character has requested to add this character.\r\nPlease try
     * again later.<br>
     * 74: Another character has requested a summon.\r\nPlease try again
     * later.<br>
     * 75: The summons has failed. Your current location or state does not allow
     * a summons.<br>
     * 76: The family cannot extend more than 1000 generations from above and
     * below.<br>
     * 77: The Junior you wish to add\r\nmust be over Level 10.<br>
     * 78: You cannot add a Junior \r\nthat has requested to change worlds.<br>
     * 79: You cannot add a Junior \r\nsince you've requested to change
     * worlds.<br>
     * 80: Separation is not possible due to insufficient Mesos.\r\nYou will
     * need %d Mesos to\r\nseparate with a Senior.<br>
     * 81: Separation is not possible due to insufficient Mesos.\r\nYou will
     * need %d Mesos to\r\nseparate with a Junior.<br>
     * 82: The Entitlement does not apply because your level does not match the
     * corresponding area.<br>
     *
     * @param type The type
     * @return Family Result packet
     */
    public static byte[] sendFamilyMessage(int type, int mesos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.FAMILY_RESULT.getValue());
        mplew.writeInt(type);
        mplew.writeInt(mesos);
        return mplew.getPacket();
    }

    public static byte[] getFamilyInfo(MapleFamilyEntry f) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_INFO_RESULT.getValue());
        mplew.writeInt(f.getReputation()); // cur rep left
        mplew.writeInt(f.getTotalReputation()); // tot rep left
        mplew.writeInt(f.getTodaysRep()); // todays rep
        mplew.writeShort(f.getJuniors()); // juniors added
        mplew.writeShort(f.getTotalJuniors()); // juniors allowed
        mplew.writeShort(0); //Unknown
        mplew.writeInt(f.getId()); // id?
        mplew.writeMapleAsciiString(f.getFamilyName());
        mplew.writeInt(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] showPedigree(int chrid, Map<Integer, MapleFamilyEntry> members) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_CHART_RESULT.getValue());
        //Hmmm xD
        return mplew.getPacket();
    }

    public static byte[] sendFamilyInvite(int playerId, String inviter) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_JOIN_REQUEST.getValue());
        mplew.writeInt(playerId);
        mplew.writeMapleAsciiString(inviter);
        return mplew.getPacket();
    }

    public static byte[] getSeniorMessage(String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_JOIN_ACCEPTED.getValue());
        mplew.writeMapleAsciiString(name);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] sendFamilyJoinResponse(boolean accepted, String added) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_JOIN_REQUEST_RESULT.getValue());
        mplew.write(accepted ? 1 : 0);
        mplew.writeMapleAsciiString(added);
        return mplew.getPacket();
    }

    public static byte[] sendGainRep(int gain, int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAMILY_FAMOUS_POINT_INC_RESULT.getValue());
        mplew.writeInt(gain);
        mplew.writeShort(0);
        return mplew.getPacket();
    }
}
