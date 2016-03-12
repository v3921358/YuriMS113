package server;

public enum MapleSquadType {

    ZAKUM(0),
    HORNTAIL(1),
    PINK_BEAN(2),
    UNKNOWN(-1);
    final byte type;

    private MapleSquadType(int type) {
        this.type = (byte) type;
    }
}
