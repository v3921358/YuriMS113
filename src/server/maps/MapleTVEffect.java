package server.maps;

import client.MapleCharacter;
import java.util.ArrayList;
import java.util.List;
import net.server.Server;
import server.TimerManager;
import tools.packets.MaplePacketCreator;

public class MapleTVEffect {

    private List<String> message = new ArrayList<>(5);
    private MapleCharacter user;
    private static boolean active;
    private int type;
    private int world;
    private MapleCharacter partner;

    public MapleTVEffect(MapleCharacter user_, MapleCharacter partner_, List<String> msg, int type_, int world_) {
        this.message = msg;
        this.user = user_;
        this.type = type_;
        this.world = world_;
        this.partner = partner_;
        broadcastTV(true);
    }

    public static boolean isActive() {
        return active;
    }

    private void setActive(boolean set) {
        active = set;
    }

    private void broadcastTV(boolean active_) {
        Server server = Server.getInstance();
        setActive(active_);
        if (active_) {
            server.broadcastMessage(world, MaplePacketCreator.enableTV());
            server.broadcastMessage(world, MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner));
            int delay = 15000;
            if (type == 4) {
                delay = 30000;
            } else if (type == 5) {
                delay = 60000;
            }
            TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    broadcastTV(false);
                }
            }, delay);
        } else {
            server.broadcastMessage(world, MaplePacketCreator.removeTV());
        }
    }
}
