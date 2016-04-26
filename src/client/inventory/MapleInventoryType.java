package client.inventory;

public enum MapleInventoryType {

    UNDEFINED(0,"未知"),
    EQUIP(1,"裝備"),
    USE(2,"消秏"),
    SETUP(3,"裝飾"),
    ETC(4,"其他"),
    CASH(5,"特殊"),
    EQUIPPED(-1); //Seems nexon screwed something when removing an item T_T
    final byte type;
    private String name = "未知";

    private MapleInventoryType(int type, String name) {
        this.type = (byte) type;
        this.name = name;
    }
    
    private MapleInventoryType(int type) {
        this.type = (byte) type;
    }

    public String getName(){
        return name;
    }
    
    public byte getType() {
        return type;
    }

    public short getBitfieldEncoding() {
        return (short) (2 << type);
    }

    public static MapleInventoryType getByType(byte type) {
        for (MapleInventoryType l : MapleInventoryType.values()) {
            if (l.getType() == type) {
                return l;
            }
        }
        return null;
    }

    public static MapleInventoryType getByWZName(String name) {
        if (name.equals("Install")) {
            return SETUP;
        } else if (name.equals("Consume")) {
            return USE;
        } else if (name.equals("Etc")) {
            return ETC;
        } else if (name.equals("Cash")) {
            return CASH;
        } else if (name.equals("Pet")) {
            return CASH;
        }
        return UNDEFINED;
    }
}
