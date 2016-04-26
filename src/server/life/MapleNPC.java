package server.life;

import client.MapleClient;
import server.MapleShopFactory;
import server.maps.MapleMapObjectType;
import tools.packets.MaplePacketCreator;

public class MapleNPC extends AbstractLoadedMapleLife {

    private MapleNPCStats stats;

    public MapleNPC(int id, MapleNPCStats stats) {
        super(id);
        this.stats = stats;
    }

    public boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public void sendShop(MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (this.getId() > 9010010 && this.getId() < 9010014) {
            client.announce(MaplePacketCreator.spawnNPCRequestController(this, false));
        } else {
            client.announce(MaplePacketCreator.spawnNPC(this));
            client.announce(MaplePacketCreator.spawnNPCRequestController(this, true));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removeNPC(getObjectId()));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.NPC;
    }

    public String getName() {
        return stats.getName();
    }
}
