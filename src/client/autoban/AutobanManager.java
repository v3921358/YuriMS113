package client.autoban;

import client.MapleCharacter;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class AutobanManager {

    

    public final static class AutoBanTimestampType {

        public final static int SS_ATTACK = 1;
        public final static int CS_ATTACK = 2;
        public final static int SS_PICKUP = 3;
        public final static int CS_PICKUP = 4;
        public final static int SS_HEAL_HP = 5;
        public final static int CS_HEAL_HP = 6;
        public final static int SS_HEAL_MP = 7;
        public final static int CS_HEAL_MP = 8;
        public final static int SS_USE_CASH_ITEM = 9;
    }

    private MapleCharacter chr;
    private Map<AutobanFactory, Integer> points = new HashMap<>();
    private Map<AutobanFactory, Long> lastTime = new HashMap<>();
    private int misses = 0;
    private int lastmisses = 0;
    private int samemisscount = 0;
    private Point lastPoisition;
    private int fastmovecount = 0;

    private long timestamp[] = new long[20];

    public AutobanManager(MapleCharacter chr) {
        this.chr = chr;
    }

    
    public void clearPoint(AutobanFactory fac)
    {
        points.put(fac, 1);
    }
            
            
    public void addPoint(AutobanFactory fac, String reason) {
        
        if(fac == fac.MOB_VAC || fac == fac.FAST_MOVE) 
        {
            
        }
        if (lastTime.containsKey(fac)) {
            if (lastTime.get(fac) < (System.currentTimeMillis() - fac.getExpire())) {
                points.put(fac, points.get(fac) / 2); //So the points are not completely gone.
            }
        }
        if (fac.getExpire() != -1) {
            lastTime.put(fac, System.currentTimeMillis());
        }

        if (points.containsKey(fac)) {
            points.put(fac, points.get(fac) + 1);
        } else {
            points.put(fac, 1);
        }

        if (points.get(fac) >= fac.getMaximum()) {
            chr.autoban("[自動鎖定系統]: " + fac.name() + " ;" + reason, 1);
            chr.sendPolice("你已經被#b外掛自動偵測鎖定系統#k鎖定。");
        }
    }

    public void addMiss() {
        this.misses++;
    }
    
    public void clearAllPoints()
    {
        points.clear();
        fastmovecount = 0;
    }

    public void resetMisses() {
        if (lastmisses == misses && misses > 6) {
            samemisscount++;
        }
        if (samemisscount > 4) {
            chr.autoban("[自動鎖定系統]: " + misses + " Miss 無敵", 1);
        } else if (samemisscount > 0) {
            this.lastmisses = misses;
        }
        this.misses = 0;
    }

    public void updateTimestamp(int type, long time) {
        this.timestamp[type] = time;
    }
    
    public long getTimestamp(int type) {
        return this.timestamp[type];
    }
    
    public void updatePosition(Point position) {
        if( this.lastPoisition != null && ( ( Math.abs(this.lastPoisition.x  - position.x)  > 350)  ||  ( Math.abs(this.lastPoisition.y  - position.y) > 2000 ) ) )
            fastmovecount++;
        this.lastPoisition = position;
    }
    
    public void resetFastMoveCount()
    {
        fastmovecount = 0;
    }
    
    public int getFastMoveCount() 
    {
        return this.fastmovecount;
    }

}
