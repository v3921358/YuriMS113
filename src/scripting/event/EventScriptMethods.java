/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripting.event;

import java.awt.Point;
import javax.script.Invocable;
import net.server.channel.Channel;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import tools.packets.CWvsContext;

/**
 *
 * @author yuri
 */
public class EventScriptMethods extends EventManager{
    
    public EventScriptMethods(Channel cserv, Invocable iv, String name) {
        super(cserv, iv, name);
    }
    
public MapleMap getMap(int mapid) {
        return this.getChannelServer().getMapFactory().getMap(mapid);
    }
    
    public void mapMsg(MapleMap map, int type, String message){
        map.broadcastMessage(CWvsContext.broadcastMsg(type, message));
    }

    public boolean haveMonster(MapleMap map, int mobid) {
        boolean found = false;
        for (MapleMapObject obj : map.getMapObjects()) {
            if (obj.getType().name() == "MONSTER") {
                MapleMonster mob = (MapleMonster) obj;
                if (mob.getId() == mobid) {
                    found = true;
                    break;
                }
            }
        }
        
        return found;
    }

    public void spawnMonster(MapleMap map, int mobid, int x, int y) {
        MapleMonster onemob;
        onemob = MapleLifeFactory.getMonster(mobid);
        Point pos = new Point(x, y);

        if (onemob != null) {
            map.spawnMonsterOnGroundBelow(onemob, pos);
        }
    }
    
}
