package client.inventory;

import com.mysql.jdbc.Statement;
import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import tools.DatabaseConnection;
import server.MapleItemInformationProvider;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;

public class MaplePet {

    private String name;
    private int id;
    private int itemid;
    private int closeness = 0;
    private byte level = 1;
    private int fullness = 100;
    private int Fh;
    private Point pos;
    private int stance;
    private boolean summoned;
    private short flags;
    private boolean changed = false;
    private byte inventorypos;
    private long expiration;

    public static enum PetFlag {

        ITEM_PICKUP(0x01, 5190000, 5191000),
        EXPAND_PICKUP(0x02, 5190002, 5191002), //idk
        AUTO_PICKUP(0x04, 5190003, 5191003), //idk
        UNPICKABLE(0x08, 5190005, -1), //not coded
        LEFTOVER_PICKUP(0x10, 5190004, 5191004), //idk
        HP_CHARGE(0x20, 5190001, 5191001),
        MP_CHARGE(0x40, 5190006, -1),
        PET_BUFF(0x80, -1, -1), //idk
        PET_DRAW(0x100, 5190007, -1), //nfs
        PET_DIALOGUE(0x200, 5190008, -1); //nfs

        private final int i, item, remove;

        private PetFlag(int i, int item, int remove) {
            this.i = i;
            this.item = item;
            this.remove = remove;
        }

        public final int getValue() {
            return i;
        }

        public final boolean check(int flag) {
            return (flag & i) == i;
        }

        public static final PetFlag getByAddId(final int itemId) {
            for (PetFlag flag : PetFlag.values()) {
                if (flag.item == itemId) {
                    return flag;
                }
            }
            return null;
        }

        public static final PetFlag getByDelId(final int itemId) {
            for (PetFlag flag : PetFlag.values()) {
                if (flag.remove == itemId) {
                    return flag;
                }
            }
            return null;
        }
    }

    private MaplePet(int itemid, byte position, int uniqueid, long expiration) {
        this.id = uniqueid;
        this.itemid = itemid;
        this.inventorypos = position;
        this.expiration = expiration;
    }

    public static MaplePet loadFromDb(int itemid, byte position, int petid, long expiration) {
        try {
            MaplePet ret = new MaplePet(itemid, position, petid, expiration);
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, level, closeness, fullness, summoned, flags FROM pets WHERE petid = ?"); // Get pet details..
            ps.setInt(1, petid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret.setName(rs.getString("name"));
            ret.setCloseness(Math.min(rs.getInt("closeness"), 30000));
            ret.setLevel((byte) Math.min(rs.getByte("level"), 30));
            ret.setFullness(Math.min(rs.getInt("fullness"), 100));
            ret.setSummoned(rs.getInt("summoned") == 1);
            ret.setFlags((byte) rs.getInt("flags"));
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException e) {
            return null;
        }
    }

    public void saveToDb() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ?, summoned = ?, flags = ? WHERE petid = ?");
            ps.setString(1, getName());
            ps.setInt(2, getLevel());
            ps.setInt(3, getCloseness());
            ps.setInt(4, getFullness());
            ps.setInt(5, isSummoned() ? 1 : 0);
            ps.setInt(6, flags);
            ps.setInt(7, getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public static int createPet(int itemid) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO pets (name, level, closeness, fullness, summoned) VALUES (?, 1, 0, 100, 0)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, MapleItemInformationProvider.getInstance().getName(itemid));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int ret = -1;
            if (rs.next()) {
                ret = rs.getInt(1);
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException e) {
            return -1;
        }
    }

    public static int createPet(int itemid, byte level, int closeness, int fullness) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO pets (name, level, closeness, fullness, summoned) VALUES (?, ?, ?, ?, 0)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, MapleItemInformationProvider.getInstance().getName(itemid));
            ps.setByte(2, level);
            ps.setInt(3, closeness);
            ps.setInt(4, fullness);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int ret = -1;
            if (rs.next()) {
                ret = rs.getInt(1);
                rs.close();
                ps.close();
            }
            return ret;
        } catch (SQLException e) {
            return -1;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemid() {
        return itemid;
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public int getCloseness() {
        return closeness;
    }

    public void setCloseness(int closeness) {
        this.closeness = closeness;
    }

    public void gainCloseness(int x) {
        this.closeness += x;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public int getFullness() {
        return fullness;
    }

    public void setFullness(int fullness) {
        this.fullness = fullness;
    }

    public final short getFlags() {
        return flags;
    }

    public final void setFlags(final int fffh) {
        this.flags = (short) fffh;
        this.changed = true;
    }

    public int getFh() {
        return Fh;
    }

    public void setFh(int Fh) {
        this.Fh = Fh;
    }

    public Point getPos() {
        return pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public int getStance() {
        return stance;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

    public boolean isSummoned() {
        return summoned;
    }

    public void setSummoned(boolean yes) {
        this.summoned = yes;
    }

    public int getItemId() {
        return this.itemid;
    }

    public byte getInventorypos() {
        return inventorypos;
    }

    public void setInventorypos(byte inventorypos) {
        this.inventorypos = inventorypos;
    }

    public boolean canConsume(int itemId) {
        for (int petId : MapleItemInformationProvider.getInstance().petsCanConsume(itemId)) {
            if (petId == this.getItemId()) {
                return true;
            }
        }
        return false;
    }

    public void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    this.setPos(((LifeMovement) move).getPosition());
                }
                this.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}
