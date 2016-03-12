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
package server;

import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.Pair;

/*
 * @author Flav
 */
public class CashShop {

    private final int accountId, characterId;
    private int nxCredit, maplePoint, nxPrepaid;
    private boolean opened;
    private ItemFactory factory;
    private final List<Item> inventory = new ArrayList<>();
    private final List<Integer> wishList = new ArrayList<>();
    private int notes = 0;

    public CashShop(int accountId, int characterId, int jobType) throws SQLException {
        this.accountId = accountId;
        this.characterId = characterId;

        if (jobType == 0) {
            factory = ItemFactory.CASH_EXPLORER;
        } else if (jobType == 1) {
            factory = ItemFactory.CASH_CYGNUS;
        } else if (jobType == 2) {
            factory = ItemFactory.CASH_ARAN;
        }

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT `nxCredit`, `maplePoint`, `nxPrepaid` FROM `accounts` WHERE `id` = ?");
            ps.setInt(1, accountId);
            rs = ps.executeQuery();

            if (rs.next()) {
                this.nxCredit = rs.getInt("nxCredit");
                this.maplePoint = rs.getInt("maplePoint");
                this.nxPrepaid = rs.getInt("nxPrepaid");
            }

            rs.close();
            ps.close();

            for (Pair<Item, MapleInventoryType> item : factory.loadItems(accountId, false)) {
                inventory.add(item.getLeft());
            }

            ps = con.prepareStatement("SELECT `sn` FROM `wishlists` WHERE `charid` = ?");
            ps.setInt(1, characterId);
            rs = ps.executeQuery();

            while (rs.next()) {
                wishList.add(rs.getInt("sn"));
            }

            rs.close();
            ps.close();
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }

    public void updateCash() {
        Connection con = DatabaseConnection.getConnection();

        try (PreparedStatement ps = con.prepareStatement("SELECT `nxCredit`, `maplePoint`, `nxPrepaid` FROM `accounts` WHERE `id` = ?")) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.nxCredit = rs.getInt("nxCredit");
                    this.maplePoint = rs.getInt("maplePoint");
                    this.nxPrepaid = rs.getInt("nxPrepaid");
                }
            }

        } catch (SQLException ex) {
           FilePrinter.printError("CashShop.txt", ex);
        }
    }

    public int getCash(int type) {
        switch (type) {
            case 0:
                return nxCredit;
            case 1:
                return maplePoint;
        }
        return 0;
    }

    public void gainCash(int type, int cash) {
        switch (type) {
            case 0:
                nxCredit += cash;

                break;
            case 1:
                maplePoint += cash;

                break;

        }
        try {
            save();
        } catch (SQLException ex) {
            Logger.getLogger(CashShop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isOpened() {
        return opened;
    }

    public void open(boolean b) {
        opened = b;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public Item findByCashId(int cashId) {
        boolean isRing = false;
        Equip equip = null;
        for (Item item : inventory) {
            if (item.getType() == 1) {
                equip = (Equip) item;
                isRing = equip.getRingId() > -1;
            }
            if ((item.getPetId() > -1 ? item.getPetId() : isRing  && equip != null ? equip.getRingId() : item.getCashId()) == cashId) {
                return item;
            }
        }

        return null;
    }

    public void addToInventory(Item item) {
        inventory.add(item);
        try {
            save();
        } catch (SQLException ex) {
            Logger.getLogger(CashShop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeFromInventory(Item item) {
        inventory.remove(item);
        try {
            save();
        } catch (SQLException ex) {
            Logger.getLogger(CashShop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Integer> getWishList() {
        return wishList;
    }

    public void clearWishList() {
        wishList.clear();
    }

    public void addToWishList(int sn) {
        wishList.add(sn);
        try {
            save();
        } catch (SQLException ex) {
            Logger.getLogger(CashShop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void gift(int recipient, String from, String message, int sn) {
        gift(recipient, from, message, sn, -1);
    }

    public void gift(int recipient, String from, String message, int sn, int ringid) {
        PreparedStatement ps = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `gifts` VALUES (DEFAULT, ?, ?, ?, ?, ?)");
            ps.setInt(1, recipient);
            ps.setString(2, from);
            ps.setString(3, message);
            ps.setInt(4, sn);
            ps.setInt(5, ringid);
            ps.executeUpdate();
        } catch (SQLException ex) {
             FilePrinter.printError("CashShop.txt", ex);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                 FilePrinter.printError("CashShop.txt", ex);
            }
        }
    }

    public Item toItem(CashItemInfo cItem) {
        return toItem(cItem, "");
    }

    public Item toItem(CashItemInfo cItem, String gift) {

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item item;

        long period = cItem.getPeriod();
        if (period <= 0 || ItemConstants.isPet(cItem.getId())) {
            period = 45;
        }

        if (ii.getInventoryType(cItem.getId()) == MapleInventoryType.EQUIP) {
            Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(cItem.getId());

            eq.setExpiration((long) (System.currentTimeMillis() + (long) (period * 24 * 60 * 60 * 1000)));
            eq.setGiftFrom(gift);
            eq.setSN(cItem.getSN());
            item = eq;
        } else {

            if (ItemConstants.isPet(cItem.getId())) {
                int petId = MaplePet.createPet(cItem.getId());
                item = new Item(cItem.getId(), (byte) 0, (short) cItem.getCount(), petId);
            } else {
                item = new Item(cItem.getId(), (byte) 0, (short) cItem.getCount());

            }
            item.setExpiration((long) (System.currentTimeMillis() + (long) (period * 24 * 60 * 60 * 1000)));
            item.setGiftFrom(gift);
            item.setSN(cItem.getSN());

        }
        return item;
    }

    public List<Pair<Item, String>> loadGifts() {
        List<Pair<Item, String>> gifts = new ArrayList<>();
        Connection con = DatabaseConnection.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `gifts` WHERE `to` = ?");
            ps.setInt(1, characterId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                notes++;
                CashItemInfo cItem = CashItemFactory.getInstance().getItem(rs.getInt("sn"));
                Item item = toItem(cItem, rs.getString("from"));
                Equip equip;
                if (item.getType() == MapleInventoryType.EQUIP.getType()) {
                    equip = (Equip) item;
                    equip.setRingId(rs.getInt("ringid"));
                    gifts.add(new Pair<Item, String>(equip, rs.getString("message")));
                } else {
                    gifts.add(new Pair<>(item, rs.getString("message")));
                }

                List<CashItemInfo> packages = CashItemFactory.getInstance().getPackageItems(cItem.getId());
                if (packages != null && packages.size() > 0) {
                    for (CashItemInfo packageItem : packages) {
                        addToInventory(toItem(packageItem, rs.getString("from")));
                    }
                } else {
                    addToInventory(item);
                }
            }

            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM `gifts` WHERE `to` = ?");
            ps.setInt(1, characterId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            FilePrinter.printError("CashShop.txt", ex);
        }

        return gifts;
    }

    public int getAvailableNotes() {
        return notes;
    }

    public void decreaseNotes() {
        notes--;
    }

    public void save() throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `nxCredit` = ?, `maplePoint` = ?, `nxPrepaid` = ? WHERE `id` = ?");
        ps.setInt(1, nxCredit);
        ps.setInt(2, maplePoint);
        ps.setInt(3, nxPrepaid);
        ps.setInt(4, accountId);
        ps.executeUpdate();
        ps.close();
        List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

        for (Item item : inventory) {
            itemsWithType.add(new Pair<>(item, MapleItemInformationProvider.getInstance().getInventoryType(item.getItemId())));
        }

        factory.saveItems(itemsWithType, accountId);
        ps = con.prepareStatement("DELETE FROM `wishlists` WHERE `charid` = ?");
        ps.setInt(1, characterId);
        ps.executeUpdate();
        ps = con.prepareStatement("INSERT INTO `wishlists` VALUES (DEFAULT, ?, ?)");
        ps.setInt(1, characterId);

        for (int sn : wishList) {
            ps.setInt(2, sn);
            ps.executeUpdate();
        }

        ps.close();
    }
}
