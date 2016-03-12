/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.server.login.handlers;

import client.MapleClient;
import constants.ServerConstants;
import java.io.FileInputStream;
import java.util.Properties;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.channel.Channel;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.LoginPacket;

public final class ServerStatusRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        byte world = (byte) slea.readShort();
        int status;
        int num = 0;
        for (Channel ch : Server.getInstance().getWorld(world).getChannels()) {
            num += ch.getConnectedClients();
        }
        if (num >= Server.getChannelMaxPlayer()) {
            status = 2;
        } else if (num >= Server.getChannelMaxPlayer() * .8) {
            status = 1;
        } else {
            status = 0;
        }
        c.announce(LoginPacket.getServerStatus(status));
    }
}
