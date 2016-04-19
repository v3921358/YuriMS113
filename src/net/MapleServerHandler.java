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
package net;

import client.MapleClient;
import constants.ServerConstants;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import net.server.Server;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import tools.FilePrinter;
import tools.HexTool;
import tools.MapleAESOFB;
import tools.packets.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;

public class MapleServerHandler extends IoHandlerAdapter {

    private PacketProcessor processor;
    private int world = -1, channel = -1;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public MapleServerHandler() {
        this.processor = PacketProcessor.getProcessor(-1, -1);
    }

    public MapleServerHandler(int world, int channel) {
        this.processor = PacketProcessor.getProcessor(world, channel);
        this.world = world;
        this.channel = channel;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (cause instanceof IOException || cause instanceof ClassCastException) {
            return;
        }
        MapleClient mc = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (mc != null && mc.getPlayer() != null) {
            FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, cause, "Exception caught by: " + mc.getPlayer());
        }
    }

    @Override
    public void sessionOpened(IoSession session) {
        if (!Server.getInstance().isOnline()) {
            session.close(true);
            return;
        }
        if (channel > -1 && world > -1) {
            if (Server.getInstance().getChannel(world, channel) == null) {
                session.close(true);
                return;
            }
        } else {
            System.out.println("IoSession with " + session.getRemoteAddress() + " opened on " + sdf.format(Calendar.getInstance().getTime()));
            FilePrinter.print(FilePrinter.SESSION, "IoSession with " + session.getRemoteAddress() + " opened on " + sdf.format(Calendar.getInstance().getTime()), false);
        }

        byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};
        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};
        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend, (short) (0xFFFF - ServerConstants.VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv, (short) ServerConstants.VERSION);
        MapleClient client = new MapleClient(sendCypher, recvCypher, session);
        client.setWorld(world);
        client.setChannel(channel);
        session.write(MaplePacketCreator.getHello(ServerConstants.VERSION, ivSend, ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            try {
                boolean inCashShop = false;
                if (client.getPlayer() != null) {
                    inCashShop = client.getPlayer().getCashShop().isOpened();
                }
                if (!client.isDisconnection()) {
                    client.disconnect(false, inCashShop);
                }
            } catch (Throwable t) {
                FilePrinter.printError(FilePrinter.ACCOUNT_STUCK, t);
            } finally {
                session.close(true);
                session.removeAttribute(MapleClient.CLIENT_KEY);

            }
        }
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        byte[] content = (byte[]) message;
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
        short packetId = slea.readShort();
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

        final MaplePacketHandler packetHandler = processor.getHandler(packetId);
        boolean check = false;
        if (packetHandler != null && packetHandler.validateState(client)) {
            try {
                for (RecvOpcode header : RecvOpcode.values()) {
                    if (header.getValue() == packetId) {

                        if (header.getDebugMode()) {
                            PrintReceived(header, content, client);
                        }
                        check = true;
                        break;
                    }
                }
                packetHandler.handlePacket(slea, client);
            } catch (final Throwable t) {
                FilePrinter.printError(FilePrinter.PACKET_HANDLER + packetHandler.getClass().getName() + ".txt", t, "Error for " + (client.getPlayer() == null ? "" : "player ; " + client.getPlayer() + " on map ; " + client.getPlayer().getMapId() + " - ") + "account ; " + client.getAccountName() + "\r\n" + slea.toString());
                //client.announce(MaplePacketCreator.enableActions());//bugs sometimes
            }
        }
        if (!check && packetId != 23) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.putShort(packetId);
            FilePrinter.printError("unknowPacket.txt", "未知封包 :" + HexTool.toString(buffer.array()) + "\n 收到的封包:\n" + HexTool.toString(content));
            PrintUnknowPacket(packetId, content);
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) {
        byte[] content = (byte[]) message;
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
        short packetId = slea.readShort();
        try {
            for (SendOpcode header : SendOpcode.values()) {
                if (header.getValue() == packetId) {
                    if (header.getDebugMode()) {
                        PrintSent(header, content, client);
                    }
                    break;
                }
            }
        } catch (final Throwable t) {
            //FilePrinter.printError(FilePrinter.PACKET_HANDLER + packetHandler.getClass().getName() + ".txt", t, "Error for " + (client.getPlayer() == null ? "" : "player ; " + client.getPlayer() + " on map ; " + client.getPlayer().getMapId() + " - ") + "account ; " + client.getAccountName() + "\r\n" + slea.toString());
            //client.announce(MaplePacketCreator.enableActions());//bugs sometimes
        }
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            client.sendPing();
        }
        super.sessionIdle(session, status);
    }

    public void PrintUnknowPacket(int packetid, byte[] content) {

        System.err.println(String.format("未知封包頭:%d", packetid));
        System.err.println("未知封:");
        System.err.println(HexTool.toString(content));
    }

    public void PrintSent(SendOpcode header, byte[] content, MapleClient client) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_BLUE = "\u001B[34m";
        System.out.println(""
                + ANSI_BLUE + String.format("玩家：%s \n", client.getAccountName())
                + ANSI_BLUE + String.format("封包頭 :%s(%d) \n", header.toString(), header.getValue())
                + ANSI_BLUE + "送出的封包:\n"
                + ANSI_BLUE + HexTool.toString(content)
                + ANSI_RESET + "\n"
        );
    }

    public void PrintReceived(RecvOpcode header, byte[] content, MapleClient client) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_GREEN = "\u001B[32m";
        System.out.println(""
                + ANSI_GREEN + String.format("玩家：%s \n", client.getAccountName())
                + ANSI_GREEN + String.format("封包頭 :%s(%d) \n", header.toString(), header.getValue())
                + ANSI_GREEN + "收到的封包:\n"
                + ANSI_GREEN + HexTool.toString(content)
                + ANSI_RESET + "\n"
        );
    }

}
