/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Flower
 */
public class UpdateCharProfileHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int type = slea.readByte();
        if (type == 0) { // 角色訊息
            String charmessage = slea.readMapleAsciiString();
            c.getPlayer().setProfileMessage(charmessage);
            //System.err.println("SetCharMessage");
        } else if (type == 1) { // 表情
            byte emotion = slea.readByte();
            c.getPlayer().setProfileEmotion(emotion);
            //System.err.println("Expression"+ expression);
        } else if (type == 2) { // 生日及星座
            //97 00 02 02 0C 05 0B
            byte blood = slea.readByte();
            byte month = slea.readByte();
            byte day = slea.readByte();
            byte constellation = slea.readByte();
            c.getPlayer().setProfileBloodType(blood);
            c.getPlayer().setProfileBirthMonth(month);
            c.getPlayer().setProfileBirthDay(day);
            c.getPlayer().setProfileConstellation(constellation);
            c.getPlayer().saveToDB();
        }
    }
    
}
