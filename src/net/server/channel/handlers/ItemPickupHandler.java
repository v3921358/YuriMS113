package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import client.autoban.AutobanManager.AutoBanTimestampType;

import java.awt.Point;
import net.AbstractMaplePacketHandler;
import net.server.world.MaplePartyCharacter;
import scripting.item.ItemScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleItemInformationProvider.scriptedItem;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class ItemPickupHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) {

        MapleCharacter chr = c.getPlayer();

        if (chr == null) {
            return;
        }
        AutobanManager abm = chr.getAutobanManager();
        int cs_timestamp = slea.readInt();
        long ss_timestamp = System.currentTimeMillis();

        int cs_offset_time = cs_timestamp - ((int) abm.getTimestamp(AutoBanTimestampType.CS_PICKUP) == 0 ? cs_timestamp - 9000 : (int) abm.getTimestamp(AutoBanTimestampType.CS_PICKUP));
        long ss_offset_time = ss_timestamp - abm.getTimestamp(AutoBanTimestampType.SS_PICKUP);

        if (cs_offset_time < 7000 || ss_offset_time < 20) {
            chr.getAutobanManager().resetFastMoveCount();
            c.disconnect(false, false);
            return;
        }

        abm.updateTimestamp(AutoBanTimestampType.CS_PICKUP, cs_timestamp);
        abm.updateTimestamp(AutoBanTimestampType.SS_PICKUP, ss_timestamp);

        slea.readByte();

        Point cpos = slea.readPos();

        int oid = slea.readInt();

        MapleMapObject ob = chr.getMap().getMapObject(oid);

        if (ob == null) {
            return;
        }

        if (chr.getMapId() > 209000000 && chr.getMapId() < 209000016) {//happyville trees
            MapleMapItem mapitem = (MapleMapItem) ob;
            if (mapitem.getDropper().getObjectId() == c.getPlayer().getObjectId()) {
                if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), false)) {
                    chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                    chr.getMap().removeMapObject(ob);
                } else {
                    c.announce(MaplePacketCreator.enableActions());
                    return;
                }
                mapitem.setPickedUp(true);
            } else {
                c.announce(MaplePacketCreator.getInventoryFull());
                c.announce(MaplePacketCreator.getShowInventoryFull());
                return;
            }
            c.announce(MaplePacketCreator.enableActions());
            return;
        }

        if (ob instanceof MapleMapItem) {
            MapleMapItem mapitem = (MapleMapItem) ob;
            synchronized (mapitem) {

                if (mapitem.getQuest() > 0 && !chr.needQuestItem(mapitem.getQuest(), mapitem.getItemId())) {
                    c.announce(MaplePacketCreator.showItemUnavailable());
                    c.announce(MaplePacketCreator.enableActions());
                    return;
                }
                if (mapitem.isPickedUp()) {
                    c.announce(MaplePacketCreator.getInventoryFull());
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return;
                }

                if (mapitem.getOwner() != chr.getId() && ((!mapitem.isPlayerDrop() && mapitem.getDropType() == 0) || (mapitem.isPlayerDrop() && chr.getMap().getEverlast()))) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }

                final double cs_distance = cpos.distanceSq(mapitem.getPosition());
                final double ss_distance = chr.getPosition().distanceSq(mapitem.getPosition());
                abm.updatePosition(cpos);
                if (cs_distance > 2500) {
                    AutobanFactory.SHORT_ITEM_VAC.addPoint(chr.getAutobanManager(), cpos.toString() + cs_distance);
                } else if (ss_distance > 230000.0) {
                    AutobanFactory.ITEM_VAC.addPoint(chr.getAutobanManager(), cpos.toString() + ss_distance);
                }

                if (mapitem.getMeso() > 0) {
                    
                    if (chr.getParty() != null) {
                        int mesosamm = mapitem.getMeso();
                        if (mesosamm > 50000 * chr.getMesoRate()) {
                            return;
                        }
                        int partynum = 0;
                        for (MaplePartyCharacter partymem : chr.getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getMapId() == chr.getMap().getId() && partymem.getChannel() == c.getChannel()) {
                                partynum++;
                            }
                        }
                        for (MaplePartyCharacter partymem : chr.getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getMapId() == chr.getMap().getId()) {
                                MapleCharacter somecharacter = c.getChannelServer().getPlayerStorage().getCharacterById(partymem.getId());
                                if (somecharacter != null) {
                                    somecharacter.gainMeso(mesosamm / partynum, true, true, false);
                                }
                            }
                        }
                    } else {
                        chr.gainMeso(mapitem.getMeso(), true, true, false);
                    }
                    
                } else if (mapitem.getItem().getItemId() / 10000 == 243) {
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    scriptedItem info = ii.getScriptedItemInfo(mapitem.getItem().getItemId());
                    if (info.runOnPickup()) {
                        ItemScriptManager ism = ItemScriptManager.getInstance();
                        String scriptName = info.getScript();
                        if (ism.scriptExists(scriptName)) {
                            ism.getItemScript(c, scriptName);
                        }

                    } else {
                        if (!MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
                            c.announce(MaplePacketCreator.enableActions());
                            return;
                        }
                    }
                } else if (useItem(c, mapitem.getItem().getItemId())) {
                    if (mapitem.getItem().getItemId() / 10000 == 238) {
                        chr.getMonsterBook().addCard(c, mapitem.getItem().getItemId());
                    }
                } else if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
                    
                } else if (mapitem.getItem().getItemId() == 4031868) {
                    chr.getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chr.getName(), chr.getItemQuantity(4031868, false), false));
                } else {
                    c.announce(MaplePacketCreator.enableActions());
                    return;
                }
                mapitem.setPickedUp(true);
                chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                chr.getMap().removeMapObject(ob);
            }
        }

        c.announce(MaplePacketCreator.enableActions());
    }

    static boolean useItem(final MapleClient c, final int id) {
        if (id / 1000000 == 2) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (ii.isConsumeOnPickup(id)) {
                if (id > 2022430 && id < 2022434) {
                    for (MapleCharacter mc : c.getPlayer().getMap().getCharacters()) {
                        if (mc.getParty() == c.getPlayer().getParty()) {
                            ii.getItemEffect(id).applyTo(mc);
                        }
                    }
                } else {
                    ii.getItemEffect(id).applyTo(c.getPlayer());
                }
                return true;
            }
        }
        return false;
    }
}
