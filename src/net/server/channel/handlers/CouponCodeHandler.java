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
package net.server.channel.handlers;

import java.sql.SQLException;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import net.AbstractMaplePacketHandler;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CashShopPacket;

/**
 *
 * @author Penguins (Acrylic)
 */
public final class CouponCodeHandler extends AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(2);
        String code = slea.readMapleAsciiString();
        boolean validcode = false;
        int type = -1;
        int item = -1;
        int amount = 0;
        boolean pass = false;
        validcode = getNXCodeValid(code.toUpperCase(), validcode);
        if (validcode) {
            type = getNXCode(code, "type");
            item = getNXCode(code, "item");
            amount = getNXCode(code, "amount");
            switch (type) {
                case 0: //nxCredit
                    c.getPlayer().getCashShop().gainCash(type, item);
                    c.getPlayer().dropMessage(1, String.format("你兌換到了 %d 點數。", item));
                    pass = true;
                    break;
                case 1: //maplePoint
                    c.getPlayer().getCashShop().gainCash(type, item);
                    c.getPlayer().dropMessage(1, String.format("你兌換到了 %d 楓葉點數。", item));
                    pass = true;
                    break;
                case 2:
                    if (MapleInventoryManipulator.addById(c, item, (short) amount, null, -1, -1)) {
                        //c.announce(CashShopPacket.showCouponRedeemedItem(null, null, type, item, item));
                        c.getPlayer().dropMessage(1, String.format("己經兌換到 道具 請去確認。", item));
                        pass = true;
                    }else{
                        c.getPlayer().dropMessage(1, String.format("請你確認物品欄是否己滿。", item));
                    }
                    break;
                case 3:
                    c.getPlayer().gainMeso(item, false);
                    c.getPlayer().dropMessage(1, String.format("你兌換到了 %d 楓幣。", item));
                    pass = true;
                    break;
            }
            if (pass) {
                updateTODB(c, code);
            }

            c.announce(CashShopPacket.showCash(c.getPlayer()));
        } else {
            c.getPlayer().dropMessage(1, "這個序號無法使用，請重新確認期限、英文大小寫。");
            c.announce(CashShopPacket.showCash(c.getPlayer()));
            //c.announce(CashShopPacket.wrongCouponCode());
        }
        c.announce(CashShopPacket.enableCSUse());
    }

    private void updateTODB(MapleClient c, String code) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET valid=0, user=?  WHERE code = ?");
            ps.setString(1, c.getPlayer().getName());
            ps.setString(2, code);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    private int getNXCode(String code, String type) {
        int item = -1;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `" + type + "` FROM nxcode WHERE code = ?");
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                item = rs.getInt(type);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
        }
        return item;
    }

    private boolean getNXCodeValid(String code, boolean validcode) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `valid` FROM nxcode WHERE code = ?");
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                validcode = rs.getInt("valid") != 0;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
        }
        return validcode;
    }
}
