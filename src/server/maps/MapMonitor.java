package server.maps;

import java.util.concurrent.ScheduledFuture;
import server.MaplePortal;
import server.TimerManager;

public class MapMonitor {

    private ScheduledFuture<?> monitorSchedule;
    private MapleMap map;
    private MaplePortal portal;

    public MapMonitor(final MapleMap map, String portal) {
        this.map = map;
        this.portal = map.getPortal(portal);
        this.monitorSchedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (map.getCharacters().size() < 1) {
                    cancelAction();
                }
            }
        }, 5000);
    }

    private void cancelAction() {
        monitorSchedule.cancel(false);
        map.killAllMonsters();
        map.clearDrops();
        if (portal != null) {
            portal.setPortalStatus(MaplePortal.OPEN);
        }
        map.resetReactors();
    }
}
