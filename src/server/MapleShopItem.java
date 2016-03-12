package server;

public class MapleShopItem {

    private short buyable;
    private int itemId;
    private int price;
    private int pitch;

    public MapleShopItem(short buyable, int itemId, int price, int pitch) {
        this.buyable = buyable;
        this.itemId = itemId;
        this.price = price;
        this.pitch = pitch;
    }

    public short getBuyable() {
        return buyable;
    }

    public int getItemId() {
        return itemId;
    }

    public int getPrice() {
        return price;
    }

    public int getPitch() {
        return pitch;
    }
}
