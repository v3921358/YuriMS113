package client.autoban;

import client.MapleCharacter;

public enum AutobanFactory {

    MOB_COUNT,
    FIX_DAMAGE(10, 15000),
    HIGH_HP_HEALING,
    FAST_HP_HEALING(15),
    FAST_MP_HEALING(15),
    GACHA_EXP,
    TUBI(20, 15000),
    SHORT_ITEM_VAC(10, 15000),
    ITEM_VAC(10, 15000),
    MOB_VAC(80000000, 5000),
    FAST_MOVE(20, 15000),
    FAST_ATTACK(10, 30000),
    MPCON(25, 30000);

    private int points;
    private long expiretime;
    private boolean enable = false;

    private AutobanFactory() {
        this(1, -1);
    }

    private AutobanFactory(int points) {
        this.points = points;
        this.expiretime = -1;
    }

    private AutobanFactory(int points, long expire) {
        this.points = points;
        this.expiretime = expire;
    }

    public int getMaximum() {
        return points;
    }

    public long getExpire() {
        return expiretime;
    }

    public void addPoint(AutobanManager ban, String reason) {

        ban.addPoint(this, reason);
    }

    public void clearPoint(AutobanManager ban) {
        ban.clearPoint(this);
    }

    public void autoban(MapleCharacter chr, String value) {
        if (this.enable) {
            chr.autoban("[自動鎖定] 原因 (" + this.name() + ": " + value + ")", 1);
            chr.sendPolice("系統偵測到修改遊戲數據，將進行鎖定");
        }
    }
}
