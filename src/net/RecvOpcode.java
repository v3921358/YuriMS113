package net;

public enum RecvOpcode {

    CUSTOM_PACKET(0x3713),
    LOGIN_PASSWORD(0x01, true),
    SERVERLIST_REQUEST(0x03),
    CHARLIST_REQUEST(0x04, false),
    CHAR_SELECT(0x06, false),
    PLAYER_LOGGEDIN(0x07, false),
    CHECK_CHAR_NAME(0x08),
    CREATE_CHAR(0x0B),
    PLAYER_DC(0x0C),
    DELETE_CHAR(0x0D),
    PONG(0x0E, false),
    STRANGE_DATA(0x0F),
    CLIENT_START_ERROR(0xFFFF),
    CLIENT_ERROR(0xFFFF),
    SERVERSTATUS_REQUEST(0x18, false),
    SET_GENDER(0x19),
    CHANGE_MAP(0x1E),
    CHANGE_CHANNEL(0x1F),
    ENTER_CASHSHOP(0x20),
    MOVE_PLAYER(0x21, false),
    CANCEL_CHAIR(0x22),
    USE_CHAIR(0x23),
    CLOSE_RANGE_ATTACK(0x25, false),
    RANGED_ATTACK(0x26),
    MAGIC_ATTACK(0x27, false),
    TOUCH_MONSTER_ATTACK(0x28),
    TAKE_DAMAGE(0x29, false),
    GENERAL_CHAT(0x2A),
    CLOSE_CHALKBOARD(0x2B),
    FACE_EXPRESSION(0x2C),
    USE_ITEMEFFECT(0x2D),
    USE_DEATHITEM(0x2E),
    MONSTER_BOOK_COVER(0x32),
    NPC_TALK(0x33),
    REMOTE_STORE(0x34),
    NPC_TALK_MORE(0x35),
    NPC_SHOP(0x36),
    STORAGE(0x37),
    HIRED_MERCHANT_REQUEST(0x38),
    FREDRICK_ACTION(0x999),
    DUEY_ACTION(0x3B),
    OWL(0x3C), //不知道是啥
    OWL_WARP(0x3D), //不知道是啥
    ITEM_SORT(0x3F),
    ITEM_GATHER(0x40),
    ITEM_MOVE(0x41),
    USE_ITEM(0x42),
    CANCEL_ITEM_EFFECT(0x43),
    USE_SUMMON_BAG(0x45),
    PET_FOOD(0x46),
    USE_MOUNT_FOOD(0x47),
    SCRIPTED_ITEM(0x48),
    USE_CASH_ITEM(0x49),
    USE_CATCH_ITEM(0x4B),
    USE_SKILL_BOOK(0x4C),
    USE_OWL_MINERVA(0x4D),//不知道是啥
    USE_TELEPORT_ROCK(0x4E),
    USE_RETURN_SCROLL(0x4F),
    USE_UPGRADE_SCROLL(0x50),
    DISTRIBUTE_AP(0x51),
    AUTO_DISTRIBUTE_AP(0x52),
    HEAL_OVER_TIME(0x53, false),
    DISTRIBUTE_SP(0x54),
    SPECIAL_MOVE(0x55),
    CANCEL_BUFF(0x56),
    SKILL_EFFECT(0x57),
    MESO_DROP(0x58),
    GIVE_FAME(0x59),
    CHAR_INFO_REQUEST(0x5B),
    SPAWN_PET(0x5C),
    CANCEL_DEBUFF(0x5D),
    CHANGE_MAP_SPECIAL(0x5E),
    USE_INNER_PORTAL(0x5F),
    TROCK_ADD_MAP(0x60),
    REPORT(0x64),
    QUEST_ACTION(0x65),
    //lolno
    SKILL_MACRO(0x68),
    SPOUSE_CHAT(0x69),
    USE_ITEM_REWARD(0x6A),
    MAKER_SKILL(0x6B),
    USE_TREASUER_CHEST(0x6C), // 開啟寶箱
    USE_REMOTE(0xFFFF), //??
    PARTYCHAT(0x70),
    WHISPER(0x71),
    MESSENGER(0x72),
    PLAYER_INTERACTION(0x73),
    PARTY_OPERATION(0x74),
    DENY_PARTY_REQUEST(0x75),
    GUILD_OPERATION(0x76),
    DENY_GUILD_REQUEST(0x77),
    BUDDYLIST_MODIFY(0x7A),
    NOTE_ACTION(0x7B),
    USE_DOOR(0x7D),
    CHANGE_KEYMAP(0x7F),
    RPS_ACTION(0x80),
    RING_ACTION(0x81),
    ALLIANCE_OPERATION(0x86),
    DENY_ALLIANCE_REQUEST(0x87),
    WEDDING_ACTION(0x8A),///

    OPEN_FAMILY(0x89),
    ADD_FAMILY(0x8A),
    DELETE_JUNIOR(0x8B),
    DELETE_SENIOR(0x8C),
    ACCEPT_FAMILY(0x8D),
    USE_FAMILY(0x8E),
    FAMILY_PRECEPT(0x8F),
    FAMILY_SUMMON(0x90),
    CLICK_GUIDE(0x91),
    ARAN_COMBO_COUNTER(0x92),
    MONSTER_CRC_CHANGE(0x93), // 
    BBS_OPERATION(0x94),
    UPDATE_CHAR_PROFILE(0x97), //星座
    ENTER_MTS(0x99),
    USE_SOLOMON_ITEM(0xA0),
    GAME_POLL(0xA1),
    USE_GACHA_EXP(0xA2),
    MOVE_PET(0xA4, false),
    PET_CHAT(0xA5),
    PET_COMMAND(0xA6),
    PET_LOOT(0xA7, false),
    PET_AUTO_POT(0xA8),
    PET_EXCLUDE_ITEMS(0xA9),
    MOVE_SUMMON(0xAC),
    SUMMON_ATTACK(0xAD),
    DAMAGE_SUMMON(0xAE),
    BEHOLDER(0xB2),//

    MOVE_LIFE(0xB6, false),
    AUTO_AGGRO(0xB7),
    MOB_DAMAGE_MOB_FRIENDLY(0xBA),
    MONSTER_BOMB(0xBB),
    MOB_DAMAGE_MOB(0xBC),
    MOB_NODE(0xBD),
    DISPLAY_NODE(0xBE),
    NPC_ACTION(0xBF, false),
    ITEM_PICKUP(0xC6, false),
    DAMAGE_REACTOR(0xC9),
    TOUCHING_REACTOR(0xCA),
    TEMP_SKILL(0xCB),
    MAPLETV(0xCC),//Don't know
    SNOWBALL(0xCD),     
    LEFT_KNOCKBACK(0xCE),
    COCONUT(0xCF),
    MATCH_TABLE(0xD1),//Would be cool if I ever get it to work :)
    MONSTER_CARNIVAL(0xD5),
    PARTY_SEARCH_START(0xD9, true),
    PARTY_SEARCH_END(0xDA, true), 
    PLAYER_UPDATE(0xE0),
    CHECK_CASH(0xE5),
    CASHSHOP_OPERATION(0xE6),
    COUPON_CODE(0xE7),
    OPEN_ITEMUI(0xEB),
    CLOSE_ITEMUI(0xEC),
    USE_ITEMUI(0xED),
    MTS_OPERATION(0xFD),
    USE_MAPLELIFE(0xFE),
    USE_HAMMER(0x104);

    private int code = -2;
    private boolean debugMode = true;

    private RecvOpcode(int code, boolean debugMode) {
        this.code = code;
        this.debugMode = debugMode;
    }

    RecvOpcode(int code) {
        this.code = code;
    }

    public int getValue() {
        return code;
    }

    public boolean getDebugMode() {
        return debugMode;
    }
}
