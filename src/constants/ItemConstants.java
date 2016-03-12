package constants;

import client.inventory.MapleInventoryType;
import net.server.TimeType;

public final class ItemConstants {

    public final static int LOCK = 0x01;
    public final static int SPIKES = 0x02;
    public final static int COLD = 0x04;
    public final static int UNTRADEABLE = 0x08;
    public final static int KARMA = 0x10;
    public final static int PET_COME = 0x80;
    public final static int UNKNOWN_SKILL = 0x100;
    public final static float ITEM_ARMOR_EXP = 1 / 350000;
    public static final float ITEM_WEAPON_EXP = 1 / 700000;

    public final static boolean EXPIRING_ITEMS = true;

    public static int getFlagByInt(int type) {
        if (type == 128) {
            return PET_COME;
        } else if (type == 256) {
            return UNKNOWN_SKILL;
        }
        return 0;
    }

    public static boolean isThrowingStar(int itemId) {
        return itemId / 10000 == 207;
    }

    public static boolean isBullet(int itemId) {
        return itemId / 10000 == 233;
    }

    public static boolean isRechargable(int itemId) {
        return itemId / 10000 == 233 || itemId / 10000 == 207;
    }

    public static boolean isArrowForCrossBow(int itemId) {
        return itemId / 1000 == 2061;
    }

    public static boolean isArrowForBow(int itemId) {
        return itemId / 1000 == 2060;
    }

    public static boolean isPet(int itemId) {
        return itemId / 1000 == 5000;
    }

    public static MapleInventoryType getInventoryType(final int itemId) {
        final byte type = (byte) (itemId / 1000000);
        if (type < 1 || type > 5) {
            return MapleInventoryType.UNDEFINED;
        }
        return MapleInventoryType.getByType(type);
    }

    public static boolean isExpRateCard(int itemId) {
        if (5210000 <= itemId && itemId <= 5210005) {
            return true;
        }
        return false;
    }

    public static boolean isDropRateCard(int itemId) {
        if (5360000 <= itemId && itemId <= 5360005) {
            return true;
        }
        return false;
    }

    public static TimeType getExpRateCardTime(int itemId) {
        int offset = itemId - 5210000;
        if (offset > 0) {
            switch (offset) {
                case 1:
                    return TimeType.MIDNIGHT;
                case 2:
                    return TimeType.MORNING;
                case 3:
                    return TimeType.AFTERNOON;
                case 4:
                    return TimeType.NIGHT;
            }
        }
        return null;
    }

    public static TimeType getDropRateCardTime(int itemId) {
        int offset = itemId - 5360000;
        if (offset > 0) {
            switch (offset) {
                case 1:
                    return TimeType.MIDNIGHT;
                case 2:
                    return TimeType.MORNING;
                case 3:
                    return TimeType.AFTERNOON;
                case 4:
                    return TimeType.NIGHT;
            }
        }
        return null;
    }

    public static boolean isRing(int itemId) {
        return itemId >= 1112000 && itemId < 1113000;
    }// 112xxxx - p

}
