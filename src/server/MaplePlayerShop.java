package server;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.SendOpcode;
import server.maps.AbstractMapleMapObject;
import server.maps.MapleMapObjectType;
import tools.packets.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Matze
 */
public class MaplePlayerShop extends AbstractMapleMapObject {

    private MapleCharacter owner;
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private List<MaplePlayerShopItem> items = new ArrayList<>();
    private MapleCharacter[] slot = {null, null, null};
    private String description;
    private int boughtnumber = 0;
    private List<String> bannedList = new ArrayList<>();

    public MaplePlayerShop(MapleCharacter owner, String description) {
        this.setPosition(owner.getPosition());
        this.owner = owner;
        this.description = description;
    }

    public boolean hasFreeSlot() {
        return visitors[0] == null || visitors[1] == null || visitors[2] == null;
    }

    public boolean isOwner(MapleCharacter c) {
        return owner.equals(c);
    }

    public void addVisitor(MapleCharacter visitor) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] == null) {
                visitors[i] = visitor;
                if (this.getSlot(0) == null) {
                    this.setSlot(visitor, 0);
                    this.broadcast(MaplePacketCreator.getPlayerShopNewVisitor(visitor, 1));
                } else if (this.getSlot(1) == null) {
                    this.setSlot(visitor, 1);
                    this.broadcast(MaplePacketCreator.getPlayerShopNewVisitor(visitor, 2));
                } else if (this.getSlot(2) == null) {
                    this.setSlot(visitor, 2);
                    this.broadcast(MaplePacketCreator.getPlayerShopNewVisitor(visitor, 3));
                    visitor.getMap().broadcastMessage(MaplePacketCreator.addCharBox(this.getOwner(), 1));
                }
                break;
            }
        }
    }

    public void removeVisitor(MapleCharacter visitor) {
        if (visitor == owner) {
            owner.getMap().removeMapObject(this);
            owner.setPlayerShop(null);
        }
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null && visitors[i].getId() == visitor.getId()) {
                int slot_ = visitor.getSlot();
                visitors[i] = null;
                this.setSlot(null, i);
                visitor.setSlot(-1);
                this.broadcast(MaplePacketCreator.getPlayerShopRemoveVisitor(slot_ + 1));
                return;
            }
        }
    }

    public boolean isVisitor(MapleCharacter visitor) {
        return visitors[0] == visitor || visitors[1] == visitor || visitors[2] == visitor;
    }

    public void addItem(MaplePlayerShopItem item) {
        items.add(item);
    }

    public void removeItem(int item) {
        items.remove(item);
    }

    /**
     * no warnings for now o.op
     *
     * @param c
     * @param item
     * @param quantity
     */
    public void buy(MapleClient c, int item, short quantity) {
        if (isVisitor(c.getPlayer())) {
            MaplePlayerShopItem pItem = items.get(item);
            Item newItem = pItem.getItem().copy();
            newItem.setQuantity(newItem.getQuantity());
            if (quantity < 1 || pItem.getBundles() < 1 || newItem.getQuantity() > pItem.getBundles() || !pItem.isExist()) {
                return;
            } else if (newItem.getType() == 1 && newItem.getQuantity() > 1) {
                return;
            }
            synchronized (c.getPlayer()) {
                if (c.getPlayer().getMeso() >= (long) pItem.getPrice() * quantity) {
                    if (MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
                        c.getPlayer().gainMeso(-pItem.getPrice() * quantity, true);
                        owner.gainMeso(pItem.getPrice() * quantity, true);
                        pItem.setBundles((short) (pItem.getBundles() - quantity));
                        if (pItem.getBundles() < 1) {
                            pItem.setDoesExist(false);
                            if (++boughtnumber == items.size()) {
                                owner.setPlayerShop(null);
                                owner.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(owner));
                                this.removeVisitors();
                                owner.dropMessage(1, "由於你的商品已經賣完，所以商店被關閉了。");
                            }
                        }
                    } else {
                        c.getPlayer().dropMessage(1, "你的物品欄已滿。請確認物品欄是否有空位再來購買。");
                    }
                }
            }
        }
    }

    public void broadcastToVisitors(final byte[] packet) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                visitors[i].getClient().announce(packet);
            }
        }
    }

    public void removeVisitors() {
        try {
            for (int i = 0; i < 3; i++) {
                if (visitors[i] != null) {
                    visitors[i].getClient().announce(MaplePacketCreator.shopErrorMessage(10, 1));
                    removeVisitor(visitors[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (owner != null) {
            removeVisitor(getOwner());
        }
    }

    public static byte[] shopErrorMessage(int error, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0A);
        mplew.write(type);
        mplew.write(error);
        return mplew.getPacket();
    }

    public void broadcast(final byte[] packet) {
        if (owner.getClient() != null && owner.getClient().getSession() != null) {
            owner.getClient().announce(packet);
        }
        broadcastToVisitors(packet);
    }

    public void chat(MapleClient c, String chat) {
        byte s = 0;
        for (MapleCharacter mc : getVisitors()) {
            s++;
            if (mc != null) {
                if (mc.getName().equalsIgnoreCase(c.getPlayer().getName())) {
                    break;
                }
            } else if (s == 3) {
                s = 0;
            }
        }
        broadcast(MaplePacketCreator.getPlayerShopChat(c.getPlayer(), chat, s));
    }

    public void sendShop(MapleClient c) {
        c.announce(MaplePacketCreator.getPlayerShop(c, this, isOwner(c.getPlayer())));
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public MapleCharacter[] getVisitors() {
        return visitors;
    }

    public MapleCharacter getSlot(int s) {
        return slot[s];
    }

    private void setSlot(MapleCharacter person, int s) {
        slot[s] = person;
        if (person != null) {
            person.setSlot(s);
        }
    }

    public List<MaplePlayerShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void banPlayer(String name) {
        if (!bannedList.contains(name)) {
            bannedList.add(name);
        }
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null && visitors[i].getName().equals(name)) {
                visitors[i].getClient().announce(MaplePacketCreator.shopErrorMessage(5, 1));
                removeVisitor(visitors[i]);
            }
            return;
        }
    }

    public boolean isBanned(String name) {
        return bannedList.contains(name);
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removeCharBox(this.getOwner()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(MaplePacketCreator.addCharBox(this.getOwner(), 4));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }
}
