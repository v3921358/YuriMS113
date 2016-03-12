/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.server.handlers;

import client.MapleClient;
import net.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Flower
 */
public class ClientErrorHandler implements MaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        System.out.println(slea.toString());
    }

    @Override
    public boolean validateState(MapleClient c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
