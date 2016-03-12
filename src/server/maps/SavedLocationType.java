package server.maps;

public enum SavedLocationType {

    FREE_MARKET, EVENT, WORLDTOUR, FLORINA, INTRO, SUNDAY_MARKET, MIRROR, AMORIA, MULUNG_TC;

    public static SavedLocationType fromString(String Str) {
        return valueOf(Str);
    }
}
