package client.inventory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Item implements Comparable<Item> {

    private int id, cashId, sn;
    private byte position;
    private short quantity;
    private int petid = -1;
    private int job=-1;
    private MaplePet pet = null;
    private String owner = "";
    protected List<String> log;
    private byte flag;
    private long expiration = -1;
    private String giftFrom = "";

    public Item(int id, byte position, short quantity) {
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.log = new LinkedList<>();
        this.flag = 0;
    }

    public Item(int id, byte position, short quantity, int petid) {
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.petid = petid;
        if (petid > -1) {
            this.pet = MaplePet.loadFromDb(id, position, petid, this.expiration);
        }
        this.flag = 0;
        this.log = new LinkedList<>();
    }

    public Item(int id, byte position, short quantity, byte flag, int petid) {
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.petid = petid;
        if (petid > -1) {
            this.pet = MaplePet.loadFromDb(id, position, petid, this.expiration);
        }

        this.flag = flag;
        this.log = new LinkedList<>();
    }

    public Item copy() {
        Item ret = new Item(id, position, quantity, petid);
        ret.flag = flag;
        ret.owner = owner;
        ret.expiration = expiration;
        ret.log = new LinkedList<>(log);
        return ret;
    }

    public void setJob(short job) {
        this.job = job;
    }
    
    public int getJob() {
        return this.job;
    }
    
    public void setPosition(byte position) {
        this.position = position;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    public int getItemId() {
        return id;
    }

    public int getCashId() {
        if (cashId == 0) {
            cashId = new Random().nextInt(Integer.MAX_VALUE) + 1;
        }
        return cashId;
    }

    public byte getPosition() {
        return position;
    }

    public short getQuantity() {
        return quantity;
    }

    public byte getType() {
        if (getPetId() > -1) {
            return 3;
        }
        return 2;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPetId() {
        return petid;
    }

    public void setPetId(int id) {
        this.petid = id;
        this.pet = MaplePet.loadFromDb(id, position, petid, this.expiration);
    }

    public int compareTo(Item other) {
        if (this.id < other.getItemId()) {
            return -1;
        } else if (this.id > other.getItemId()) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Item: " + id + " quantity: " + quantity;
    }

    public List<String> getLog() {
        return Collections.unmodifiableList(log);
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte b) {
        this.flag = b;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expire) {
        this.expiration = expire;
    }

    public int getSN() {
        return sn;
    }

    public void setSN(int sn) {
        this.sn = sn;
    }

    public String getGiftFrom() {
        return giftFrom;
    }

    public void setGiftFrom(String giftFrom) {
        this.giftFrom = giftFrom;
    }

    public MaplePet getPet() {
        return pet;
    }
}
