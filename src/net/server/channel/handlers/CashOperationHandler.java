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

import client.MapleCharacter;
import client.MapleClient;
import client.MapleRing;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import net.AbstractMaplePacketHandler;
import server.CashItemFactory;
import server.CashItemInfo;
import server.CashShop;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.MTSCSPacket;

public final class CashOperationHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        CashShop cs = chr.getCashShop();

        if (!cs.isOpened()) {
            cs.updateCash();
            c.announce(MaplePacketCreator.enableActions());
            return;
        }

        if (slea.available() == 0) {
            // refresh 
            MTSCSPacket.showCash(chr);
            return;
        }

        /*
         buy
         [E6 00] 
         [03] [00] [01 2F 31 01]
        
         gift
         [E6 00] 
         [04] action
         [08 00] [31 39 39 34 30 33 32 36] sencond pw
         [05 2F 31 01] sn
         [05 00] [41 64 6D 69 6E]  to name
         [06 00] [71 71 71 71 71 0A] message
        
         to item 
            
         
         */
        final int action = slea.readByte();

        if (action == 0x03 || action == 0x1E) {

            final int useNX = slea.readByte();
            final int snCS = slea.readInt();

            CashItemInfo cItem = CashItemFactory.getInstance().getItem(snCS);
            if (cItem == null || !cItem.onSale() || cs.getCash(useNX) < cItem.getPrice()) {
                return;
            }

            if (action == 0x03) { // Item
                Item itemz = chr.getCashShop().toItem(cItem);
                cs.addToInventory(itemz);
                c.announce(MTSCSPacket.showBoughtCashItem(itemz, c.getAccID()));

            } else { // Package
                List<CashItemInfo> cashPackage = CashItemFactory.getInstance().getPackageItems(cItem.getId());
                List<Item> items = new ArrayList<Item>();
                for (CashItemInfo i : cashPackage) {
                    Item itemz = c.getPlayer().getCashShop().toItem(i);
                    cs.addToInventory(itemz);
                    items.add(itemz);
                }
                c.announce(MTSCSPacket.showBoughtCashPackage(items, c.getAccID()));
            }

            cs.gainCash(useNX, -cItem.getPrice());
            c.announce(MTSCSPacket.showCash(chr));

        } else if (action == 0x04) {//TODO check for gender

            String secondPassword = slea.readMapleAsciiString();
            final int sn = slea.readInt();
            String characterName = slea.readMapleAsciiString();
            String message = slea.readMapleAsciiString();

            CashItemInfo cItem = CashItemFactory.getInstance().getItem(sn);
            Item item = chr.getCashShop().toItem(cItem);

            Map<String, String> recipient = MapleCharacter.getCharacterFromDatabase(characterName);

            if (!canBuy(cItem, cs.getCash(0)) || message.length() < 1 || message.length() > 73) {
                return;
            }
            if (!c.check2ndPassword(secondPassword)) {
                c.announce(MTSCSPacket.sendGiftFail(197, 0, false));
                return;
            } else if (recipient == null) {
                c.announce(MTSCSPacket.sendGiftFail(172, 0, false));
                return;
            } else if (recipient.get("accountid").equals(String.valueOf(c.getAccID()))) {
                c.announce(MTSCSPacket.sendGiftFail(171, 0, false));
                return;
            } else if (cItem.getGender() < 2
                    && recipient.get("gender").equals(String.valueOf(cItem.getGender()))) {
                c.announce(MTSCSPacket.sendGiftFail(176, 0, false));
                return;

            }

            cs.gift(Integer.parseInt(recipient.get("id")), chr.getName(), message, cItem.getSN());
            c.announce(MTSCSPacket.sendGift(recipient.get("name"), cItem, cItem.getPrice() / 2, false));
            cs.gainCash(4, -cItem.getPrice());
            c.announce(MTSCSPacket.showCash(chr));
            try {
                chr.sendNote(recipient.get("name"), chr.getName() + " 送了你禮物! 趕快去商城確認看看.", (byte) 0); //fame or not
            } catch (SQLException ex) {
            }
            MapleCharacter receiver = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient.get("name"));
            if (receiver != null) {
                receiver.showNote();
            }
        } else if (action == 0x05) { // Modify wish list
            cs.clearWishList();
            for (byte i = 0; i < 10; i++) {
                int sn = slea.readInt();
                CashItemInfo cItem = CashItemFactory.getInstance().getItem(sn);
                if (cItem != null && cItem.onSale() && sn != 0) {
                    cs.addToWishList(sn);
                }
            }
            c.announce(MTSCSPacket.showWishList(chr));
        } else if (action == 0x06) { // Increase Inventory Slots
            //slea.skip(1);
            int cash = slea.readByte();
            byte mode = slea.readByte();
            //System.err.println(String.format("cash:%d \n mode:%d", cash, mode));

            if (mode == 0) {
                byte type = slea.readByte();
                if (cs.getCash(cash) < 100) {
                    return;
                }
                if (chr.gainSlots(type, 4, false)) {
                    c.announce(MTSCSPacket.showBoughtInventorySlots(type, chr.getSlots(type)));
                    cs.gainCash(cash, -100);
                    c.announce(MTSCSPacket.showCash(chr));
                }
            } else {
                CashItemInfo cItem = CashItemFactory.getInstance().getItem(slea.readInt());
                int type = (cItem.getId() - 9110000) / 1000;
                if (!canBuy(cItem, cs.getCash(cash))) {
                    return;
                }
                if (chr.gainSlots(type, 8, false)) {
                    c.announce(MTSCSPacket.showBoughtInventorySlots(type, chr.getSlots(type)));
                    cs.gainCash(cash, -cItem.getPrice());
                    c.announce(MTSCSPacket.showCash(chr));
                }
            }
        } else if (action == 0x07) { // Increase Storage Slots
            int cash = slea.readByte();
            byte mode = slea.readByte();
            if (mode == 0) {
                if (cs.getCash(cash) < 100) {
                    return;
                }
                if (chr.getStorage().gainSlots(4)) {
                    c.announce(MTSCSPacket.showBoughtStorageSlots(chr.getStorage().getSlots()));
                    cs.gainCash(cash, -100);
                    c.announce(MTSCSPacket.showCash(chr));
                }
            } else {
                CashItemInfo cItem = CashItemFactory.getInstance().getItem(slea.readInt());

                if (!canBuy(cItem, cs.getCash(cash))) {
                    return;
                }
                if (chr.getStorage().gainSlots(8)) {
                    c.announce(MTSCSPacket.showBoughtStorageSlots(chr.getStorage().getSlots()));
                    cs.gainCash(cash, -cItem.getPrice());
                    c.announce(MTSCSPacket.showCash(chr));
                }
            }
        } else if (action == 0x08) { // Increase Character Slots
            slea.skip(1);
            int cash = slea.readInt();
            CashItemInfo cItem = CashItemFactory.getInstance().getItem(slea.readInt());

            if (!canBuy(cItem, cs.getCash(cash))) {
                return;
            }

            if (c.gainCharacterSlot()) {
                c.announce(MTSCSPacket.showBoughtCharacterSlot(c.getCharacterSlots()));
                cs.gainCash(cash, -cItem.getPrice());
                c.announce(MTSCSPacket.showCash(chr));
            }
        } else if (action == 0x0D) { // Take from Cash Inventory
            /*
             [E6 00]
             [0D] 
             [A1 2C 96 55] id
             [00 00 00 00] skip
             [01 01 00]
             */
            Item item = cs.findByCashId(slea.readInt());
            if (item == null) {
                return;
            }
            if (chr.getInventory(MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId())).addItem(item) != -1) {
                cs.removeFromInventory(item);
                c.announce(MTSCSPacket.takeFromCashInventory(item));
            }
        } else if (action == 0x0E) { // Put into Cash Inventory
            int cashId = slea.readInt();
            slea.skip(4);
            MapleInventory mi = chr.getInventory(MapleInventoryType.getByType(slea.readByte()));
            Item item = mi.findByCashId(cashId);
            if (item == null) {
                return;
            }
            cs.addToInventory(item);
            mi.removeSlot(item.getPosition());
            c.announce(MTSCSPacket.putIntoCashInventory(item, c.getAccID()));
        } else if (action == 0x1D) { //crush ring (action 28)

            String secondPassword = slea.readMapleAsciiString();

            if (c.check2ndPassword(secondPassword)) {
                int SN = slea.readInt();
                String recipient = slea.readMapleAsciiString();
                String text = slea.readMapleAsciiString();
                CashItemInfo ring = CashItemFactory.getInstance().getItem(SN);
                MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (partner == null) {
                    c.announce(MTSCSPacket.sendGiftFail(176, 0, false));
                } else {
                    if (partner.getGender() == chr.getGender()) {
                        chr.dropMessage("You and your partner are the same gender, please buy a friendship ring.");
                        return;
                    }
                    Equip item = (Equip) chr.getCashShop().toItem(ring);
                    int ringid = MapleRing.createRing(ring.getId(), chr, partner);
                    item.setRingId(ringid);
                    cs.addToInventory(item);
                    c.announce(MTSCSPacket.showBoughtCashItem(item, c.getAccID()));
                    cs.gift(partner.getId(), chr.getName(), text, item.getSN(), (ringid + 1));
                    cs.gainCash(0, -ring.getPrice());
                    chr.addCrushRing(MapleRing.loadFromDb(ringid));
                    try {
                        chr.sendNote(partner.getName(), text, (byte) 1);
                    } catch (SQLException ex) {
                    }
                    partner.showNote();
                }

            } else {
                c.announce(MTSCSPacket.sendGiftFail(197, 0, false));
            }

            c.announce(MTSCSPacket.showCash(c.getPlayer()));
        } else if (action == 0x20) { // everything is 1 meso...
            int itemId = CashItemFactory.getInstance().getItem(slea.readInt()).getId();
            if (chr.getMeso() > 0) {
                if (itemId == 4031180 || itemId == 4031192 || itemId == 4031191) {
                    chr.gainMeso(-1, false);
                    MapleInventoryManipulator.addById(c, itemId, (short) 1);
                    c.announce(MTSCSPacket.showBoughtQuestItem(itemId));
                }
            }
            c.announce(MTSCSPacket.showCash(c.getPlayer()));
        } else if (action == 0x23) { //Friendship :3
            /*
             E6 00 
             23 
             08 00 5D 31 31 31 31 31 31 31 
             EB E8 3E 01 
             09 00 71 77 65 71 77 65 71 65 71 
             04 00 58 44 44 0A
             */
            String secondPassword = slea.readMapleAsciiString();

            if (c.check2ndPassword(secondPassword)) {
                int snID = slea.readInt();
                CashItemInfo ring = CashItemFactory.getInstance().getItem(snID);
                String sentTo = slea.readMapleAsciiString();
                String message = slea.readMapleAsciiString();

                MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(sentTo);

                if (partner == null) {
                    c.announce(MTSCSPacket.sendGiftFail(176, 0, false));
                } else {
                    Equip eq = (Equip) chr.getCashShop().toItem(ring);
                    int ringid = MapleRing.createRing(ring.getId(), chr, partner);
                    eq.setSN(snID);
                    eq.setRingId(ringid);
                    cs.addToInventory(eq);
                    c.announce(MTSCSPacket.showBoughtCashItem(eq, c.getAccID()));
                    cs.gift(partner.getId(), chr.getName(), message, eq.getSN(), (ringid + 1));
                    cs.gainCash(0, -ring.getPrice());
                    chr.addFriendshipRing(MapleRing.loadFromDb(ringid));
                    try {
                        chr.sendNote(partner.getName(), message, (byte) 1);
                    } catch (SQLException ex) {
                    }
                    partner.showNote();
                }
            } else {
                c.announce(MTSCSPacket.sendGiftFail(197, 0, false));
            }
            c.announce(MTSCSPacket.showCash(c.getPlayer()));
        } else if (action == 0x31) { // recv gift
            c.announce(MTSCSPacket.enableCSUse());
        } else {
            System.out.println(slea);
        }
    }

    private static boolean checkBirthday(MapleClient c, int idate) {
        int year = idate / 10000;
        int month = (idate - year * 10000) / 100;
        int day = idate - year * 10000 - month * 100;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month - 1, day);
        return c.checkBirthDate(cal);
    }

    public static boolean canBuy(CashItemInfo item, int cash) {
        return item != null && item.onSale() && item.getPrice() <= cash;
    }
}
