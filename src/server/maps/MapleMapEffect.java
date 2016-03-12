package server.maps;

import client.MapleClient;
import tools.packets.MaplePacketCreator;

public class MapleMapEffect {

    private String msg;
    private int itemId;
    private boolean active = true;

    public MapleMapEffect(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
    }

    public final byte[] makeDestroyData() {
        return MaplePacketCreator.removeMapEffect();
    }

    public final byte[] makeStartData() {
        return MaplePacketCreator.startMapEffect(msg, itemId, active);
    }

    public void sendStartData(MapleClient client) {
        client.announce(makeStartData());
    }
}
