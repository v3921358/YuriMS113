package server.life;

import client.MapleCharacter;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;

public class SpawnPoint {

    private int monster, mobTime, team, fh, f;
    private Point pos;
    private long nextPossibleSpawn;
    private int mobInterval = 5000;
    private AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private boolean immobile;

    public SpawnPoint(final MapleMonster monster, Point pos, boolean immobile, int mobTime, int mobInterval, int team) {
        this.monster = monster.getId();
        this.pos = new Point(pos);
        this.mobTime = mobTime;
        this.team = team;
        this.fh = monster.getFh();
        this.f = monster.getF();
        this.immobile = immobile;
        this.mobInterval = mobInterval;
        this.nextPossibleSpawn = System.currentTimeMillis();
    }

    public boolean shouldSpawn() {
        if (mobTime < 0) {
            return false;
        }
        // regular spawnpoints should spawn a maximum of 3 monsters; immobile spawnpoints or spawnpoints with mobtime a
        // maximum of 1
        if (((mobTime != 0 || immobile) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 1) {
            return false;
        }
        return nextPossibleSpawn <= System.currentTimeMillis();
    }

    public MapleMonster getMonster() {
        MapleMonster mob = new MapleMonster(MapleLifeFactory.getMonster(monster));
        mob.setPosition(new Point(pos));
        mob.setTeam(team);
        mob.setFh(fh);
        mob.setF(f);
        spawnedMonsters.incrementAndGet();
        mob.addListener(new MonsterListener() {
            @Override
            public void monsterKilled(MapleMonster monster, MapleCharacter highestDamageChar) {
                nextPossibleSpawn = System.currentTimeMillis();
                if (mobTime > 0) {
                    nextPossibleSpawn += mobTime * 1000;
                } else {
                    nextPossibleSpawn += monster.getAnimationTime("die1");
                }
                spawnedMonsters.decrementAndGet();
            }
        });
        if (mobTime == 0) {
            nextPossibleSpawn = System.currentTimeMillis() + mobInterval;
        }
        return mob;
    }

    public Point getPosition() {
        return pos;
    }

    public final int getF() {
        return f;
    }

    public final int getFh() {
        return fh;
    }
}
