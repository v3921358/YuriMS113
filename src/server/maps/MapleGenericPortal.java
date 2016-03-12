package server.maps;

import client.MapleClient;
import java.awt.Point;
import scripting.portal.PortalScriptManager;
import server.MaplePortal;
import tools.packets.MaplePacketCreator;

public class MapleGenericPortal implements MaplePortal {

    private String name;
    private String target;
    private Point position;
    private int targetmap;
    private int type;
    private boolean status = true;
    private int id;
    private String scriptName;
    private boolean portalState;

    public MapleGenericPortal(int type) {
        this.type = type;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void setPortalStatus(boolean newStatus) {
        this.status = newStatus;
    }

    @Override
    public boolean getPortalStatus() {
        return status;
    }

    @Override
    public int getTargetMapId() {
        return targetmap;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTargetMapId(int targetmapid) {
        this.targetmap = targetmapid;
    }

    @Override
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public void enterPortal(MapleClient c) {
        boolean changed = false;
        if (getScriptName() != null) {
            changed = PortalScriptManager.getInstance().executePortalScript(this, c);
        } else if (getTargetMapId() != 999999999) {
            MapleMap to = c.getPlayer().getEventInstance() == null ? c.getChannelServer().getMapFactory().getMap(getTargetMapId()) : c.getPlayer().getEventInstance().getMapInstance(getTargetMapId());
            MaplePortal pto = to.getPortal(getTarget());
            if (pto == null) {// fallback for missing portals - no real life case anymore - intresting for not implemented areas
                pto = to.getPortal(0);
            }
            c.getPlayer().changeMap(to, pto); //late resolving makes this harder but prevents us from loading the whole world at once
            changed = true;
        }
        if (!changed) {
            c.announce(MaplePacketCreator.enableActions());
        }
    }

    @Override
    public void setPortalState(boolean state) {
        this.portalState = state;
    }

    @Override
    public boolean getPortalState() {
        return portalState;
    }
}
