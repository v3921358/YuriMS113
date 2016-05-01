/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net;

public enum SendOpcode {

    LOGIN_STATUS(0x00, false),
    /* TWMS IGONRE */
    SERVERLIST(0x02, false),
    CHARLIST(0x03, false),
    SERVER_IP(0x04, false),
    CHAR_NAME_RESPONSE(0x05),
    ADD_NEW_CHAR_ENTRY(0x06),
    DELETE_CHAR_RESPONSE(0x07),
    CHANGE_CHANNEL(0x08),
    PING(0x09, false),
    CS_USE(0x0A),
    CHANNEL_SELECTED(0x0D),
    GET_HELLO(0x0E, false),
    RELOG_RESPONSE(0x0F),
    SECONDPW_ERROR(0x10),
    CHOOSE_GENDER(0x14),
    GENDER_SET(0x15),//maybe this is RELOG_RESPONSE, can't care less
    SERVERSTATUS(0x16, false),//CHECK_USER_LIMIT_RESULT

    /*CWvsContext::OnPacket*/
    INVENTORY_OPERATION(0x1B, false),//(0x1D),
    INVENTORY_GROW(0x1C),//(0x1E),
    UPDATE_STATS(0x1D, false),//(0x1F),
    TEMPORY_STAT_SET(0x1E),
    TEMPORY_STAT_RESET(0x1F),
    FORCED_STAT_SET(0x20),
    FORCED_STAT_RESET(0x21),
    UPDATE_SKILLS(0x22),
    SKILL_USE_RESULT(0x23),
    FAME_RESPONSE(0x24),
    SHOW_STATUS_INFO(0x25, false),
    SHOW_NOTES(0x26), //MEMO_RESULT
    MAP_TRANSFER_RESULT(0x27),
    ANTI_MACRO_RESULT(0x28),
    CLAIM_RESULT(0x29),
    CLAIM_AVAILABLE_TIME(0x2B),
    CLAIM_STATUS_CHANGED(0x2C),
    SET_TAMING_MOB_INFO(0x2F),
    QUEST_CLEAR(0x2E),
    ENTRUSTED_SHOP_CHECK_RESULT(0x2F),
    SKILL_LEARN_ITEM_RESULT(0x31),
    SORT_ITEM_RESULT(0x32),
    
    
    GATHER_ITEM_RESULT(0x33),
    SUE_CHARACTER_RESULT(0x37),
    TRADE_MONEY_LIMIT(0x39),
    SET_GENDER(0x3A),
    CHAR_INFO(0x36),
    PARTY_OPERATION(0x37),
    BUDDYLIST(0x38),
    GUILD_OPERATION(0x3A),
    ALLIANCE_OPERATION(0x3B),
    SPAWN_PORTAL(0x3C),
    SERVERMESSAGE(0x3D),
    INCUBATOR_RESULT(0x3E),
    SHOP_SCANNER_RESULT(0x3F),
    SHOP_LINK_RESULT(0x40),
    MARRIAGE_REQUEST(0x41),
    MARRIAGE_RESULT(0x42),
    WEDDING_GIFT_RESULT(0x43),//(0x4A),
    NOTIFY_MARRIED_PARTNER_MAP_TRANSFER(0x44),
    CASH_PET_FOOD_RESULT(0x45),
    SET_WEEK_EVENT_MESSAGE(0x46, false),
    SET_POTION_DISCOUNT_RATE(0x47),
    BRIDLE_MOB_CATCH_FAIL(0x4F),
    IMITATED_NPC_RESULT(0x4A), //113
    IMITATED_NPC_DATA(0x4B), // 113
    LIMITED_NPC_DISABLE_INFO(0x4C), // 113
    MONSTER_BOOK_SET_CARD(0x4D),
    MONSTER_BOOK_SET_COVER(0x4E),
    HOUR_CHANGED(0x4F),
    MINIMAP_ON_OFF(0x50),
    CONSULT_AUTHKEY_UPDATE(0x51),
    CLASS_COMPETITION_AUTHKEY_UPDATE(0x52),
    WEB_BOARD_AUTHKEY_UPDATE(0x53),
    SESSION_VALUE(0x54),
    //PARTY_VALUE(0x5B),
    //FIELD_SET_VARIABLE(0x5C),
    BONUS_EXP_CHANGED(0x55),//pendant of spirit etc (guess, not sure about the opcode in v83)
    FAMILY_CHART_RESULT(0x56),
    FAMILY_INFO_RESULT(0x57),
    FAMILY_RESULT(0x58),
    FAMILY_JOIN_REQUEST(0x59),
    FAMILY_JOIN_REQUEST_RESULT(0x5A),
    FAMILY_JOIN_ACCEPTED(0x5B),
    FAMILY_PRIVILEGE_LIST(0x5C, false),
    FAMILY_FAMOUS_POINT_INC_RESULT(0x5D),
    FAMILY_NOTIFY_LOGIN_OR_LOGOUT(0x5E), //? is logged in. LOLWUT
    FAMILY_SET_PRIVILEGE(0x5F),
    FAMILY_SUMMON_REQUEST(0x60),
    NOTIFY_LEVELUP(0x61),
    NOTIFY_MARRIAGE(0x62),
    NOTIFY_JOB_CHANGE(0x63),
    SET_BUY_EQUIP_EXT(0x64),
    SCRIPT_PROGRESS_MESSAGE(0x65),
    DATA_CRC_CHECK_FAILED(0x66),

    MAPLE_TV_USE_RES(0x67), //It's not blank, It's a popup nibs
    
    GUILD_BBS_PACKET(0x68),
    
    AVATAR_MEGAPHONE_RES(0x53),
    SET_AVATAR_MEGAPHONE(0x54),
    CLEAR_AVATAR_MEGAPHONE(0x6E),
    CANCEL_NAME_CHANGE_RESULT(0x6F),
    CANCEL_TRANSFER_WORLD_RESULT(0x76),
    DESTROY_SHOP_RESULT(0x77),
    
    FAKE_GM_NOTICE(0x70),//bad asses
    SUCCESS_IN_USE_GACHAPON_BOX(0x71),
    NEW_YEAR_CARD_RES(0x72),
    RANDOM_MORPH_RES(0x73),
    
    CANCEL_NAME_CHANGE_BY_OTHER(0x76),
    /* ??? */
    MACRO_SYS_DATA_INIT(0x7A),
    /*CStage::OnPacket*/
    SET_FIELD(0x7b, false),
    SET_ITC(0x7C),
    SET_CASH_SHOP(0x7D),
    SET_BACK_EFFECT(0x7E),
    SET_MAP_OBJECT_VISIBLE(0x7F),//CMapLoadable::OnSetMapObjectVisible O_O
    CLEAR_BACK_EFFECT(0x80),
    /*CField::OnPacket*/
    BLOCKED_MAP(0x81),//TransferFieldRequestIgnored
    BLOCKED_SERVER(0x82),
    FORCED_MAP_EQUIP(0x83),//FIELD_SPECIFIC_DATA
    MULTICHAT(0x84),
    WHISPER(0x85),
    SPOUSE_CHAT(0x86),
    SUMMON_ITEM_INAVAILABLE(0x89), //You can't use it in this map

    FIELD_EFFECT(0x87),
    FIELD_OBSTACLE_ONOFF(0x88),
    FIELD_OBSTACLE_ONOFF_STATUS(0x89),
    //FIELD_OBSTACLE_ALL_RESET(0x8A),
    MAP_EFFECT(0x88),
    CASH_SONG(0x89),
    ADMIN_RESULT(0x8A),
    OX_QUIZ(0x8B),//QUIZ
    GMEVENT_INSTRUCTIONS(0x8C),//DESC
    CLOCK(0x8D),
    BOAT_EFFECT(0x8E),
    BOAT_PACKET(0x8F),
    STOP_CLOCK(0x93),
    PYRAMID_GAUGE(0x94),
    PYRAMID_SCORE(0x95),
    MOVE_PLATFORM(0x96),//尚未完成
    /* CUserPool::OnPacket */
    SPAWN_PLAYER(0x99),
    REMOVE_PLAYER_FROM_MAP(0x9A),
    /*  CUserPool__OnUserCommonPacket */
    CHATTEXT(0x9B),
    CHALKBOARD(0x9C),
    UPDATE_CHAR_BOX(0x9D),
    SHOW_CONSUME_EFFECT(0x9E),
    SHOW_UPGRADE_EFFECT(0x9F),
    FISHING_CAUGHT(0xA0),//尚未完成
    HIT_BY_USER(0xA1), //[4][4]
    /* CUser__OnPetPacket */
    SPAWN_PET(0xA2),
    MOVE_PET(0xA5, false),
    PET_CHAT(0xA6),
    PET_LoadExceptionList(0xA8),
    PET_NAMECHANGE(0xA7),
    PET_COMMAND(0xA9),
    /* CUser__OnSummonPacket */
    SPAWN_SUMMON(0xAA),
    REMOVE_SUMMON(0xAB),
    MOVE_SUMMON(0xAC),
    SUMMON_ATTACK(0xAD),
    SUMMON_SKILL(0xAE),
    DAMAGE_SUMMON(0xAF),
    /* CUserPool__OnUserRemotePacket */
    MOVE_PLAYER(0xB1, false),
    /* CUserRemote__OnAttack */
    CLOSE_RANGE_ATTACK(0xB2, false),
    RANGED_ATTACK(0xB3),
    MAGIC_ATTACK(0xB4),
    ENERGY_ATTACK(0xB5),
    SKILL_EFFECT(0xB6),
    CANCEL_SKILL_EFFECT(0xB7),
    DAMAGE_PLAYER(0xB8),
    FACIAL_EXPRESSION(0xB9),
    SHOW_ITEM_EFFECT(0xBA),
    SHOW_UPGRADE_TOMB_EFFECT(0xBB), //[4][4][4]
    ACTIVE_PORTABLE_CHAIR(0xBD),
    // 0xBC ?? sub_9A4751((void *)v3, Format);
    AVARTAR_MODFIED(0xBE),
    SHOW_FOREIGN_EFFECT(0xBF),
    SET_TEMPORARY_STAT(0xC0),
    RESET_TEMPORARY_STAT(0xC1),
    RRCEIVE_HP(0xC2),
    GUILD_NAME_CHANGED(0xC3),
    GUILD_MARK_CHANGED(0xC4),
    THROW_GRENADE(0xC5), // [4][4][4][4]
    /* CUserLocal__OnPacket*/
    CANCEL_CHAIR(0xC6),
    SHOW_ITEM_GAIN_INCHAT(0xC7),
    DOJO_WARP_UP(0xC8),
    MESOBAG_SUCCESS(0xCA),
    MESOBAG_FAILURE(0xCB),
    UPDATE_QUEST_INFO(0xCC),
    PET_FLAG_CHANGE(0xCE),
    PLAYER_HINT(0xD1),
    OPEN_UI(0xD5),//(0xDC),
    LOCK_UI(0xD6),//(0xDD),
    DISABLE_UI(0xD7),//(0xDE),
    SPAWN_GUIDE(0xD8),
    TALK_GUIDE(0xD9),
    SHOW_COMBO(0xDA),
    COOLDOWN(0xE3),
    /* CMobPool__OnPacket */
    SPAWN_MONSTER(0xE5, false),//(0xEC),
    KILL_MONSTER(0xE6, false),//(0xED),
    SPAWN_MONSTER_CONTROL(0xE7, false),
    MOB_CRC_KEY_CHANGED(0xF2),
    /* CMobPool::OnMobPacket */
    MOVE_MONSTER(0xE8, false),
    MOVE_MONSTER_RESPONSE(0xE9, false),
    APPLY_MONSTER_STATUS(0xEB),
    CANCEL_MONSTER_STATUS(0xEC),
    DAMAGE_MONSTER(0xEF),
    SHOW_MONSTER_HP(0xF3, false),
    SHOW_DRAGGED(0xF4), //CATCH
    CATCH_MONSTER(0xF5),
    MOONSTER_SPEAKING(0xF6), //[4][4]
    MONSTER_ATTACK_BY_MONSTER(0xF7),
    SHOW_MAGNET(0xFD),
    /* CNpcPool__OnPacket */
    SPAWN_NPC(0xF9, false),
    REMOVE_NPC(0xFA),
    SPAWN_NPC_REQUEST_CONTROLLER(0xFB, false),
    NPC_ACTION(0xFC, false),
    /* CEmployeePool::OnPacket */
    SPAWN_HIRED_MERCHANT(0x103),
    DESTROY_HIRED_MERCHANT(0x104),
    SPAWN_MINI_ROOM_BALLOON(0x106),
    /* CDropPool__OnPacket */
    DROP_ITEM_FROM_MAPOBJECT(0x107, false),
    REMOVE_ITEM_FROM_MAP(0x108, false),
    /* CMessageBoxPool__OnPacket */
    KITE_MESSAGE(0x109),
    SPAWN_KITE(0x10A),
    REMOVE_KITE(0x10B),
    /* CAffectedAreaPool__OnPacket */
    SPAWN_MIST(0x10C),
    REMOVE_MIST(0x10D),
    /* CTownPortalPool::OnPacket */
    SPAWN_DOOR(0x10E),
    REMOVE_DOOR(0x10F),
    /* CReactorPool::OnPacket */
    REACTOR_HIT(0x113),
    REACTOR_SPAWN(0x115),
    REACTOR_DESTROY(0x116),
    SNOWBALL_STATE(0x119),
    HIT_SNOWBALL(0x11A),
    SNOWBALL_MESSAGE(0x11B),
    LEFT_KNOCK_BACK(0x11C),
    COCONUT_HIT(0x11D),
    COCONUT_SCORE(0x11E),
    GUILD_BOSS_HEALER_MOVE(0x11F),
    GUILD_BOSS_PULLEY_STATE_CHANGE(0x120),
    MONSTER_CARNIVAL_START(0x121),
    MONSTER_CARNIVAL_OBTAINED_CP(0x122),
    MONSTER_CARNIVAL_PARTY_CP(0x123),
    MONSTER_CARNIVAL_SUMMON(0x124),
    MONSTER_CARNIVAL_MESSAGE(0x125),
    MONSTER_CARNIVAL_DIED(0x126),
    MONSTER_CARNIVAL_LEAVE(0x127),
    ARIANT_ARENA_USER_SCORE(0x129),
    SHEEP_RANCH_INFO(0x12B),
    SHEEP_RANCH_CLOTHES(0x12C),
    ARIANT_SCORE(0x12D),
    HORNTAIL_CAVE(0x12E),
    ZAKUM_SHRINE(0x12F),
    /* CScriptMan::OnScriptMessage */
    NPC_TALK(0x13C),
    /* CShopDlg::OnPacket */
    OPEN_NPC_SHOP(0x13D),
    CONFIRM_SHOP_TRANSACTION(0x13E),
    /* CAdminShopDlg__OnPacket */
    ADMIN_SHOP_MESSAGE(0x13F),//lame :P
    ADMIN_SHOP(0x140),
    STORAGE(0x141),
    /* CStoreBankDlg::OnPacket */
    FREDRICK_MESSAGE(0x142),
    FREDRICK(0x143),
    RPS_GAME(0x144),
    MESSENGER(0x145),
    PLAYER_INTERACTION(0x146),
    TOURNAMENT(0x147),
    TOURNAMENT_MATCH_TABLE(0x148),
    TOURNAMENT_SET_PRIZE(0x149),
    TOURNAMENT_UEW(0x14A),
    TOURNAMENT_CHARACTERS(0x14B),//they never coded this :|

    WEDDING_PROGRESS(0x14C),//byte step, int groomid, int brideid
    WEDDING_CEREMONY_END(0x14D),
    PARCEL(0x14E),
    CHARGE_PARAM_RESULT(0x15A),
    QUERY_CASH_RESULT(0x157),
    CASHSHOP_OPERATION(0x158),
    KEYMAP(0x163, false),
    AUTO_HP_POT(0x164),
    AUTO_MP_POT(0x165),
    SEND_TV(0x166),
    REMOVE_TV(0x167),
    ENABLE_TV(0x168),
    MTS_OPERATION2(0x16C),
    MTS_OPERATION(0x16D),
    VICIOUS_HAMMER(0x174);

    private int code = -2;
    private boolean debugMode = true;

    private SendOpcode(int code, boolean debugMode) {
        this.code = code;
        this.debugMode = debugMode;
    }

    private SendOpcode(int code) {
        this.code = code;
    }

    public int getValue() {
        return code;
    }

    public boolean getDebugMode() {
        return debugMode;
    }
}
