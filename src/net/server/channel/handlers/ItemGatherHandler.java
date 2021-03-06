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
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 *
 * @author BubblesDev
 */
public final class ItemGatherHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int timestamp = slea.readInt();
        byte inv = slea.readByte();
        if (inv < 0 || inv > 5) {
            chr.getAutobanManager().resetFastMoveCount();
            c.disconnect(false, false);
            return;
        }
        
        final MapleInventoryType invType = MapleInventoryType.getByType(inv);
        MapleInventory Inv = c.getPlayer().getInventory(invType);
        ArrayList<Item> itemMap = new ArrayList<>();
        for (Item item : Inv.list()) {
            itemMap.add(item.copy()); // clone all  items T___T.
        }
        
        for (Item itemStats : itemMap) {
            MapleInventoryManipulator.removeFromSlot(c, invType, itemStats.getPosition(), itemStats.getQuantity(), true, false);
        }
        
        final List<Item> sortedItems = sortItems(itemMap);
        for (Item item : sortedItems) {
            MapleInventoryManipulator.addFromDrop(c, item, false);
        }
        
        c.announce(CWvsContext.finishedGather(inv));
        c.announce(CWvsContext.enableActions());
        itemMap.clear();
        sortedItems.clear();
    }
    

    private static List<Item> sortItems(final List<Item> passedMap) {
        final List<Integer> itemIds = new ArrayList<>(); // empty list.
        for (Item item : passedMap) {
            itemIds.add(item.getItemId()); // adds all item ids to the empty list to be sorted.
        }
        Collections.sort(itemIds); // sorts item ids

        final List<Item> sortedList = new LinkedList<>(); // ordered list pl0x <3.

        for (Integer val : itemIds) {
            for (Item item : passedMap) {
                if (val == item.getItemId()) { // Goes through every index and finds the first value that matches
                    sortedList.add(item);
                    passedMap.remove(item);
                    break;
                }
            }
        }
        return sortedList;
    }
    
}
