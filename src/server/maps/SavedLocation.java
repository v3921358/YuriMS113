package server.maps;

public class SavedLocation {

    private int mapid = 102000000, portal;

    public SavedLocation(int mapid, int portal) {
        this.mapid = mapid;
        this.portal = portal;
    }

    public int getMapId() {
        return mapid;
    }

    public int getPortal() {
        return portal;
    }
}
