package server.maps;

import client.MapleClient;
import java.awt.Rectangle;
import java.util.List;
import scripting.reactor.ReactorScriptManager;
import server.TimerManager;
import tools.FilePrinter;
import tools.packets.MaplePacketCreator;
import tools.Pair;

public class MapleReactor extends AbstractMapleMapObject {

    private final int rid;
    private final MapleReactorStats stats;
    private byte state;
    private int delay;
    private MapleMap map;
    private String name;
    private boolean timerActive;
    private boolean alive;

    public MapleReactor(MapleReactorStats stats, int rid) {
        this.stats = stats;
        this.rid = rid;
        alive = true;
    }

    public void setTimerActive(boolean active) {
        this.timerActive = active;
    }

    public boolean isTimerActive() {
        return timerActive;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getState() {
        return state;
    }

    public int getId() {
        return rid;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.REACTOR;
    }

    public final byte getFacingDirection() {
        return stats.getFacingDirection();
    }

    public final void setFacingDirection(byte value) {
        this.stats.setFacingDirection(value);
    }

    public int getReactorType() {
        return stats.getType(state);
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public MapleMap getMap() {
        return map;
    }

    public Pair<Integer, Integer> getReactItem(byte index) {
        return stats.getReactItem(state, index);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(makeDestroyData());
    }

    public final byte[] makeDestroyData() {
        return MaplePacketCreator.destroyReactor(this);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(makeSpawnData());
    }

    public final byte[] makeSpawnData() {
        return MaplePacketCreator.spawnReactor(this);
    }

    public void delayedHitReactor(final MapleClient c, long delay) {
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                hitReactor(c);
            }
        }, delay);
    }

    public void hitReactor(MapleClient c) {
        hitReactor(0, (short) 0, 0, c);
    }

    public void hitReactor(int charPos, short stance, int skillid, MapleClient c) {
        try {
            if (stats.getType(state) < 999 && stats.getType(state) != -1) {//type 2 = only hit from right (kerning swamp plants), 00 is air left 02 is ground left
                if (!(stats.getType(state) == 2 && (charPos == 0 || charPos == 2))) { //get next state
                    for (byte b = 0; b < stats.getStateSize(state); b++) {//YAY?
                        List<Integer> activeSkills = stats.getActiveSkills(state, b);
                        if (activeSkills != null) {
                            if (!activeSkills.contains(skillid)) {
                                continue;
                            }
                        }
                        state = stats.getNextState(state, b);
                        if (stats.getNextState(state, b) == -1) {//end of reactor
                            if (stats.getType(state) < 100) {//reactor broken
                                if (delay > 0) {
                                    map.destroyReactor(getObjectId());
                                } else {//trigger as normal
                                    map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                                }
                            } else {//item-triggered on final step
                                map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                            }
                            ReactorScriptManager.getInstance().act(c, this);
                        } else { //reactor not broken yet
                            map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                            if (state == stats.getNextState(state, b)) {//current state = next state, looping reactor
                                ReactorScriptManager.getInstance().act(c, this);
                            }
                        }
                        break;
                    }
                }
            } else {
                state++;
                map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                ReactorScriptManager.getInstance().act(c, this);
            }
        } catch (Exception e) {
           FilePrinter.printError("MapleReactor.txt", e);
        }
    }

    public Rectangle getArea() {
        return new Rectangle(getPosition().x + stats.getTL().x, getPosition().y + stats.getTL().y, stats.getBR().x - stats.getTL().x, stats.getBR().y - stats.getTL().y);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
