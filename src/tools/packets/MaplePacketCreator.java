package tools.packets;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCharacter.SkillEntry;
import client.MapleClient;
import client.MapleDisease;
import client.MapleKeyBinding;
import client.MapleMount;
import client.MapleQuestStatus;
import client.MapleRing;
import client.MapleStat;
import client.Skill;
import client.SkillMacro;
import client.inventory.Equip;
import client.inventory.Equip.ScrollResult;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import java.awt.Point;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.SendOpcode;
import net.server.PlayerCoolDownValueHolder;
import net.server.Server;
import net.server.channel.handlers.PlayerInteractionHandler;
import net.server.channel.handlers.SummonDamageHandler.SummonAttackEntry;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuildSummary;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import server.DueyPackages;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePlayerShopItem;
import server.MapleShopItem;
import server.MapleTrade;
import server.events.MapleSnowball;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MobSkill;
import server.maps.HiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMist;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.movement.LifeMovementFragment;
import server.partyquest.MonsterCarnivalParty;
import tools.HexTool;
import tools.Pair;
import tools.Randomizer;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Frz
 */
public class MaplePacketCreator {

    private static void addCharStats(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId()); // character id
        mplew.writeAsciiString(chr.getName(), 15);
        mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.writeInt(chr.getHair()); // hair
        for (int i = 0; i < 3; i++) {
            if (chr.getPet(i) != null) {
                mplew.writeLong(chr.getPet(i).getExpiration());
            } else {
                mplew.writeLong(0);
            }
        }

        mplew.write(chr.getLevel()); // level
        mplew.writeShort(chr.getJob().getId()); // job
        mplew.writeShort(chr.getStr()); // str
        mplew.writeShort(chr.getDex()); // dex
        mplew.writeShort(chr.getInt()); // int
        mplew.writeShort(chr.getLuk()); // luk
        mplew.writeShort(chr.getHp()); // hp (?)
        mplew.writeShort(chr.getMaxHp()); // maxhp
        mplew.writeShort(chr.getMp()); // mp (?)
        mplew.writeShort(chr.getMaxMp()); // maxmp
        mplew.writeShort(chr.getRemainingAp()); // remaining ap
        mplew.writeShort(chr.getRemainingSp()); // remaining sp
        mplew.writeInt(chr.getExp()); // current exp
        mplew.writeShort(chr.getFame()); // fame
        mplew.writeInt(chr.getGachaExp()); //Gacha Exp
        mplew.writeLong(0);
        mplew.writeInt(chr.getMapId()); // current map id
        mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
        mplew.writeZeroBytes(25); // 台版以前到現在都有
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);

    }

    public static void addCharLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair()); // hair
        addCharEquips(mplew, chr);
    }

    public static void addCharacterInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeLong(-1);
        mplew.write(0);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity());

        if (chr.getLinkedName() == null) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getLinkedName());
        }

        addInventoryInfo(mplew, chr);
        addSkillInfo(mplew, chr);
        addQuestInfo(mplew, chr);
        addMiniGameInfo(mplew, chr);
        addRingInfo(mplew, chr);
        addTeleportInfo(mplew, chr);
        addMonsterBookInfo(mplew, chr);
        addAreaInfo(mplew, chr);//assuming it stayed here xd
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
    }

    private static void addTeleportInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final int[] tele = chr.getTrockMaps();
        final int[] viptele = chr.getVipTrockMaps();
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(tele[i]);
        }
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(viptele[i]);
        }
    }

    public static void addMiniGameInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(0);
        /*for (int m = size; m > 0; m--) {//nexon does this :P
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         }*/
    }

    private static void addAreaInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Map<Short, String> areaInfos = chr.getAreaInfos();
        mplew.writeShort(areaInfos.size());
        for (Short area : areaInfos.keySet()) {
            mplew.writeShort(area);
            mplew.writeMapleAsciiString(areaInfos.get(area));
        }
    }

    private static void addCharEquips(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<Item> ii = MapleItemInformationProvider.getInstance().canWearEquipment(chr, equip.list());
        Map<Byte, Integer> myEquip = new LinkedHashMap<>();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap<>();
        for (Item item : ii) {
            byte pos = (byte) (item.getPosition() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if (pos > 100 && pos != 111) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        Item cWeapon = equip.getItem((byte) -111);
        mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
        for (int i = 0; i < 3; i++) {
            if (chr.getPet(i) != null) {
                mplew.writeInt(chr.getPet(i).getItemId());
            } else {
                mplew.writeInt(0);
            }
        }
    }

    public static void addCharEntry(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean viewall) {
        addCharStats(mplew, chr);
        addCharLook(mplew, chr, false);
        mplew.write(0);
        mplew.write(0);
        if (chr.isGM()) {
            return;
        }

        /*    if (ServerConstants.ENABLE_RANKING) {
         mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled) Short??
         mplew.writeInt(chr.getRank()); // world rank
         mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
         mplew.writeInt(chr.getJobRank()); // job rank
         mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)
         }*/
    }

    private static void addQuestInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(chr.getStartedQuestsSize());
        for (MapleQuestStatus q : chr.getStartedQuests()) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeMapleAsciiString(q.getQuestData());
            if (q.getQuest().getInfoNumber() > 0) {
                mplew.writeShort(q.getQuest().getInfoNumber());
                mplew.writeMapleAsciiString(Integer.toString(q.getMedalProgress()));
            }
        }
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeLong(PacketUtil.getTime(q.getCompletionTime()));
        }
    }

    public static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item) {
        addItemInfo(mplew, item, false);
    }

    public static void addExpirationTime(final MaplePacketLittleEndianWriter mplew, long time) {
        mplew.writeLong(PacketUtil.getTime(time));
    }

    public static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item, boolean zeroPosition) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean isCash = ii.isCash(item.getItemId());
        boolean isPet = item.getPetId() > -1;
        boolean isRing = false;
        Equip equip = null;
        byte pos = item.getPosition();
        if (item.getType() == 1) {
            equip = (Equip) item;
            isRing = equip.getRingId() > -1;
        }
        if (!zeroPosition) {
            if (equip != null) {
                if (pos < 0) {
                    pos *= -1;
                }
                mplew.write(pos > 100 ? pos - 100 : pos);
            } else {
                mplew.write(pos);
            }
        }
        mplew.write(item.getType());
        mplew.writeInt(item.getItemId());
        mplew.writeBool(isCash);
        if (isCash) {
            mplew.writeLong(isPet ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
        }
        if (isPet) {
            addPetItemInfo(mplew, item, item.getPet());
        } else {
            addExpirationTime(mplew, item.getExpiration());

            if (equip == null) {
                mplew.writeShort(item.getQuantity());
                mplew.writeMapleAsciiString(item.getOwner());
                mplew.writeShort(item.getFlag()); // flag

                if (ItemConstants.isRechargable(item.getItemId())) {
                    mplew.writeInt(2);
                    mplew.writeShort(0x54);
                    mplew.write(0);
                    mplew.write(0x34);
                }
                return;
            }
            mplew.write(equip.getUpgradeSlots()); // upgrade slots
            mplew.write(equip.getLevel()); // level
            mplew.writeShort(equip.getStr()); // str
            mplew.writeShort(equip.getDex()); // dex
            mplew.writeShort(equip.getInt()); // int
            mplew.writeShort(equip.getLuk()); // luk
            mplew.writeShort(equip.getHp()); // hp
            mplew.writeShort(equip.getMp()); // mp
            mplew.writeShort(equip.getWatk()); // watk
            mplew.writeShort(equip.getMatk()); // matk
            mplew.writeShort(equip.getWdef()); // wdef
            mplew.writeShort(equip.getMdef()); // mdef
            mplew.writeShort(equip.getAcc()); // accuracy 
            mplew.writeShort(equip.getAvoid()); // avoid
            mplew.writeShort(equip.getHands()); // hands
            mplew.writeShort(equip.getSpeed()); // speed
            mplew.writeShort(equip.getJump()); // jump
            mplew.writeMapleAsciiString(equip.getOwner()); // owner name
            mplew.writeShort(equip.getFlag()); //Item Flags

            if (isCash) {
                for (int i = 0; i < 10; i++) {
                    mplew.write(0x40);
                }
            } else {
                mplew.write(0);
                mplew.write(equip.getItemLevel()); //Item Level
                mplew.writeShort(0);
                mplew.writeShort(equip.getItemExp()); //Works pretty weird :s
                mplew.writeInt(0);//(equip.getDurability());
                mplew.writeInt(equip.getVicious()); //WTF NEXON ARE YOU SERIOUS?
                mplew.writeShort(0);//(equip.getHpR());
                mplew.writeShort(0);//(equip.getMpR());
            }
            mplew.writeLong(PacketUtil.getTime(-2));
        }
    }

    public static final void addPetItemInfo(final MaplePacketLittleEndianWriter mplew, final Item item, final MaplePet pet) {
        addExpirationTime(mplew, item.getExpiration()); //always
        mplew.writeAsciiString(pet.getName(), 13);
        mplew.write(pet.getLevel());
        mplew.writeShort(pet.getCloseness());
        mplew.write(pet.getFullness());
        addExpirationTime(mplew, item.getExpiration());
        mplew.writeShort(0);
        mplew.writeShort(pet.getFlags());
        mplew.writeShort(0);
        for (int i = 0; i < 4; i++) {
            mplew.write(0); //0x40 before, changed to 0?
        }

    }

    private static void addInventoryInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {

        mplew.writeLong(PacketUtil.getTime(System.currentTimeMillis()));
        mplew.writeInt(chr.getMeso()); // mesos
        mplew.writeInt(chr.getId());
        mplew.writeInt(0);
        mplew.writeInt(0);

        for (byte i = 1; i <= 5; i++) {
            mplew.write(chr.getInventory(MapleInventoryType.getByType(i)).getSlotLimit());
        }

        mplew.writeLong(PacketUtil.getTime(-2));

        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<Item> equippedC = iv.list();
        List<Item> equipped = new ArrayList<>(equippedC.size());

        for (Item item : equippedC) {
            equipped.add((Item) item);
        }
        Collections.sort(equipped);
        for (Item item : equipped) {
            if (item.getPosition() < 0 && item.getPosition() > -100) {
                addItemInfo(mplew, item);
            }
        }
        mplew.write(0); // start of equipped nx
        for (Item item : equipped) {
            if (item.getPosition() <= -100 && item.getPosition() > -1000) {
                addItemInfo(mplew, item);
            }
        }

        mplew.write(0); // start of equip inventory
        for (Item item : chr.getInventory(MapleInventoryType.EQUIP).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (Item item : chr.getInventory(MapleInventoryType.USE).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (Item item : chr.getInventory(MapleInventoryType.SETUP).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (Item item : chr.getInventory(MapleInventoryType.ETC).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (Item item : chr.getInventory(MapleInventoryType.CASH).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
    }

    private static void addSkillInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {

        Map<Skill, MapleCharacter.SkillEntry> skills = chr.getSkills();
        mplew.writeShort(skills.size());
        for (Iterator<Entry<Skill, SkillEntry>> it = skills.entrySet().iterator(); it.hasNext();) {
            Entry<Skill, MapleCharacter.SkillEntry> skill = it.next();
            mplew.writeInt(skill.getKey().getId());
            mplew.writeInt(skill.getValue().skillevel);
            if (skill.getKey().isFourthJob()) {
                mplew.writeInt(skill.getValue().masterlevel);
            }
        }

        mplew.writeShort(chr.getAllCooldowns().size());
        for (PlayerCoolDownValueHolder cooling : chr.getAllCooldowns()) {
            mplew.writeInt(cooling.skillId);
            int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
            mplew.writeShort(timeLeft / 1000);
        }
    }

    private static void addMonsterBookInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMonsterBookCover()); // cover
        mplew.write(0);
        Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
        mplew.writeShort(cards.size());
        for (Entry<Integer, Integer> all : cards.entrySet()) {
            mplew.writeShort(all.getKey() % 10000); // Id
            mplew.write(all.getValue()); // Level
        }
    }

    /**
     * Sends a hello packet.
     *
     * @param mapleVersion The maple client version.
     * @param sendIv the IV used by the server for sending
     * @param recvIv the IV used by the server for receiving
     * @return
     */
    public static byte[] getHello(short mapleVersion, byte[] sendIv, byte[] recvIv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(0x0E);
        mplew.writeShort(mapleVersion);
        mplew.writeShort(1);
        mplew.write(49);
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(6);
        return mplew.getPacket();
    }

    /**
     * Sends a ping packet.
     *
     * @return The packet.
     */
    public static byte[] getPing() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendOpcode.PING.getValue());
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the new channel.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @return The server IP packet.
     */
    public static byte[] getChannelChange(InetAddress inetAddr, int port) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        return mplew.getPacket();
    }

    public static byte[] enableTV() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.ENABLE_TV.getValue());
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Removes TV
     *
     * @return The Remove TV Packet
     */
    public static byte[] removeTV() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendOpcode.REMOVE_TV.getValue());
        return mplew.getPacket();
    }

    /**
     * Sends MapleTV
     *
     * @param chr The character shown in TV
     * @param messages The message sent with the TV
     * @param type The type of TV
     * @param partner The partner shown with chr
     * @return the SEND_TV packet
     */
    public static byte[] sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SEND_TV.getValue());
        mplew.write(partner != null ? 3 : 1);
        mplew.write(type); //Heart = 2  Star = 1  Normal = 0
        addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        if (partner != null) {
            mplew.writeMapleAsciiString(partner.getName());
        } else {
            mplew.writeShort(0);
        }
        for (int i = 0; i < messages.size(); i++) {
            if (i == 4 && messages.get(4).length() > 15) {
                mplew.writeMapleAsciiString(messages.get(4).substring(0, 15));
            } else {
                mplew.writeMapleAsciiString(messages.get(i));
            }
        }
        mplew.writeInt(1337); // time limit shit lol 'Your thing still start in blah blah seconds'
        if (partner != null) {
            addCharLook(mplew, partner, false);
        }
        return mplew.getPacket();
    }

    /**
     * Gets character info for a character.
     *
     * @param chr The character to get info about.
     * @return The character info packet.
     */
    public static byte[] getCharInfo(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(Randomizer.nextInt());
        }
        addCharacterInfo(mplew, chr);
        mplew.writeLong(PacketUtil.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }

    /**
     * Gets an empty stat update.
     *
     * @return The empy stat update packet.
     */
    public static byte[] enableActions() {
        return updatePlayerStats(PacketUtil.EMPTY_STATUPDATE, true);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The stats to update.
     * @return The stat update packet.
     */
    public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats) {
        return updatePlayerStats(stats, false);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The list of stats to update.
     * @param itemReaction Result of an item reaction(?)
     * @return The stat update packet.
     */
    public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_STATS.getValue());

        mplew.write(itemReaction ? 1 : 0);
        int updateMask = 0;

        for (Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }

        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
                @Override
                public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        mplew.writeInt(updateMask);
        for (Pair<MapleStat, Integer> statupdate : mystats) {
            if (statupdate.getLeft().getValue() >= 1) {
                if (statupdate.getLeft().getValue() == 0x1) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() <= 0x4) {
                    mplew.writeInt(statupdate.getRight());
                } else if (statupdate.getLeft().getValue() < 0x20) {
                    mplew.write(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else {
                    mplew.writeInt(statupdate.getRight().intValue());
                }
            }
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to change maps.
     *
     * @param to The <code>MapleMap</code> to warp to.
     * @param spawnPoint The spawn portal number to spawn at.
     * @param chr The character warping to <code>to</code>
     * @return The map change packet.
     */
    public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.writeInt(2);//updated
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getHp());
        mplew.write(0);
        mplew.writeLong(PacketUtil.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a portal.
     *
     * @param townId The ID of the town the portal goes to.
     * @param targetId The ID of the target.
     * @param pos Where to put the portal.
     * @return The portal spawn packet.
     */
    public static byte[] spawnPortal(int townId, int targetId, Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
        mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if (pos != null) {
            mplew.writePos(pos);
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a door.
     *
     * @param oid The door's object ID.
     * @param pos The position of the door.
     * @param town
     * @return The remove door packet.
     */
    public static byte[] spawnDoor(int oid, Point pos, boolean town) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.SPAWN_DOOR.getValue());
        mplew.writeBool(town);
        mplew.writeInt(oid);
        mplew.writePos(pos);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a door.
     *
     * @param oid The door's ID.
     * @param town
     * @return The remove door packet.
     */
    public static byte[] removeDoor(int oid, boolean town) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
        if (town) {
            mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
            mplew.writeInt(999999999);
            mplew.writeInt(999999999);
        } else {
            mplew.writeShort(SendOpcode.REMOVE_DOOR.getValue());
            mplew.write(0);
            mplew.writeInt(oid);
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a special map object.
     *
     * @param summon
     * @param skillLevel The level of the skill used.
     * @param animated Animated spawn?
     * @return The spawn packet for the map object.
     */
    public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
        mplew.writeShort(SendOpcode.SPAWN_SUMMON.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkill());
        mplew.write(0x01);
        mplew.write(summon.getSkillLevel());
        mplew.writePos(summon.getPosition());
        mplew.write(summon.getSkill() == 32111006 ? 5 : 4); //reaper = 5?
        mplew.writeShort(0/*summon.getFh()*/);
        mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 = follow (4th mage summons?), 2/4 = only tele follow, 3 = bird follow
        mplew.write(summon.isPuppet() ? 0 : 1); // 0 and the summon can't attack - but puppets don't attack with 1 either ^.-
        mplew.write(animated ? 0 : 1);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a special map object.
     *
     * @param summon
     * @param animated Animated removal?
     * @return The packet removing the object.
     */
    public static byte[] removeSummon(MapleSummon summon, boolean animated) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.REMOVE_SUMMON.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 4 : 1); // ?
        return mplew.getPacket();
    }

    /**
     * Gets the response to a relog request.
     *
     * @return The relog response packet.
     */
    public static byte[] getRelogResponse() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.RELOG_RESPONSE.getValue());
        mplew.write(1);//1 O.O Must be more types ):
        return mplew.getPacket();
    }

    public static byte[] broadcastMsg(String message) {
        return broadcastMessage(4, 0, message, false);
    }

    public static byte[] broadcastMsg(int type, String message) {
        return broadcastMessage(type, 0, message, false);
    }

    public static byte[] broadcastMsg(int type, int channel, String message) {
        return broadcastMessage(type, channel, message, false);
    }

    public static byte[] broadcastMsg(int type, int channel, String message, boolean smegaEar) {
        return broadcastMessage(type, channel, message, smegaEar);
    }

    /**
     * Gets a server message packet.
     *
     * Possible values for <code>type</code>:<br> 0: [Notice]<br> 1: Popup<br>
     * 2: Megaphone<br> 3: Super Megaphone<br> 4: Scrolling message at top<br>
     * 5: Pink Text<br> 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @param servermessage Is this a scrolling ticker?
     * @return The server notice packet.
     */
    private static byte[] broadcastMessage(int type, int channel, String message, boolean megaEar) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (type == 4) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);
        switch (type) {
            case 3:
            case 11:
            case 12:
                mplew.write(channel - 1); // channel
                mplew.writeBool(megaEar);
                break;
            case 9:
                mplew.write(channel - 1);
                break;
            case 6:
            case 7:
                mplew.writeInt(0);

        }
        return mplew.getPacket();
    }

    public static byte[] getMultiMegaphone(List<String> messages, int channel, boolean showEar) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(0x0A);
        mplew.writeAsciiString(messages.get(1));
        mplew.write(messages.size());
        if (messages.size() > 1) {
            mplew.writeAsciiString(messages.get(1));
        }
        if (messages.size() > 2) {
            mplew.writeAsciiString(messages.get(2));
        }
        return mplew.getPacket();
    }

    public static byte[] getItemMegaphone(String msg, boolean whisper, int channel, Item item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);
        if (item == null) {
            mplew.write(0);
        } else {
            mplew.write(item.getPosition());
            addItemInfo(mplew, item, true);
        }
        return mplew.getPacket();
    }

    /**
     * Sends a Avatar Super Megaphone packet.
     *
     * @param chr The character name.
     * @param medal The medal text.
     * @param channel Which channel.
     * @param itemId Which item used.
     * @param message The message sent.
     * @param ear Whether or not the ear is shown for whisper.
     * @return
     */
    public static byte[] getAvatarMega(MapleCharacter chr, String medal, int channel, int itemId, List<String> message, boolean ear) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_AVATAR_MEGAPHONE.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(medal + chr.getName());
        for (String s : message) {
            mplew.writeMapleAsciiString(s);
        }
        mplew.writeInt(channel - 1); // channel
        mplew.writeBool(ear);
        addCharLook(mplew, chr, true);
        return mplew.getPacket();
    }

    public static byte[] spawnNPC(MapleNPC life) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(24);
        mplew.writeShort(SendOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write(life.getF() == 1 ? 0 : 1);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(23);
        mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write(life.getF() == 1 ? 0 : 1);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.writeBool(MiniMap);
        return mplew.getPacket();
    }

    /**
     * Gets a general chat packet.
     *
     * @param cidfrom The character ID who sent the chat.
     * @param text The text of the chat.
     * @param whiteBG
     * @param show
     * @return The general chat packet.
     */
    public static byte[] getChatText(int cidfrom, String text, boolean gm, int show) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.writeBool(gm);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show an EXP increase.
     *
     * @param gain The amount of EXP gained.
     * @param inChat In the chat box?
     * @param white White text or yellow?
     * @return The exp gained packet.
     */
    public static byte[] getShowExpGain(int gain, int equip, boolean inChat, boolean white) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.writeBool(white);
        mplew.writeInt(gain);
        mplew.writeBool(inChat);
        mplew.writeInt(0); // monster book bonus (Bonus Event Exp)
        mplew.writeShort(0); //Weird stuff
        mplew.writeInt(0); //wedding bonus
        mplew.write(0); //0 = party bonus, 1 = Bonus Event party Exp () x0
        mplew.writeInt(0); // party bonus
        mplew.writeInt(equip); //equip bonus
        mplew.writeInt(0); //Internet Cafe Bonus
        mplew.writeInt(0); //Rainbow Week Bonus
        if (inChat) {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a fame gain.
     *
     * @param gain How many fame gained.
     * @return The meso gain packet.
     */
    public static byte[] getShowFameGain(int gain) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @return The meso gain packet.
     */
    public static byte[] getShowMesoGain(int gain) {
        return getShowMesoGain(gain, false);
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @param inChat Show in the chat window?
     * @return The meso gain packet.
     */
    public static byte[] getShowMesoGain(int gain, boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.writeShort(1); //v83
        } else {
            mplew.write(5);
        }
        mplew.writeInt(gain);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity How many items gained.
     * @return The item gain packet.
     */
    public static byte[] getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    /**
     * Gets a packet telling the client to show an item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity The number of items gained.
     * @param inChat Show in the chat window?
     * @return The item gain packet.
     */
    public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (inChat) {
            mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(3);
            mplew.write(1);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        } else {
            mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] killMonster(int oid, boolean animation) {
        return killMonster(oid, animation ? 1 : 0);
    }

    /**
     * Gets a packet telling the client that a monster was killed.
     *
     * @param oid The objectID of the killed monster.
     * @param animation 0 = dissapear, 1 = fade out, 2+ = special
     * @return The kill monster packet.
     */
    public static byte[] killMonster(int oid, int animation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(animation);
        mplew.write(animation);
        return mplew.getPacket();
    }

    public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod);
        mplew.writeInt(drop.getObjectId());
        mplew.writeBool(drop.getMeso() > 0); // 1 mesos, 0 item, 2 and above all item meso bag,
        mplew.writeInt(drop.getItemId()); // drop object ID
        mplew.writeInt(drop.getOwner()); // owner charid/paryid :)
        mplew.write(drop.getDropType()); // 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
        mplew.writePos(dropto);
        mplew.writeInt(drop.getDropType() == 0 ? drop.getOwner() : 0); //test

        if (mod != 2) {
            mplew.writePos(dropfrom);
            mplew.writeShort(0);//Fh?
        }
        if (drop.getMeso() == 0) {
            addExpirationTime(mplew, drop.getItem().getExpiration());
        }
        mplew.write(drop.isPlayerDrop() ? 0 : 1); //pet EQP pickup
        return mplew.getPacket();
    }

    /**
     * Gets a packet spawning a player as a mapobject to other clients.
     *
     * @param chr The character to spawn to other clients.
     * @return The spawn player packet.
     */
    public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel()); //v83
        mplew.writeMapleAsciiString(chr.getName());

        if (chr.getGuildId() < 1) {
            mplew.writeMapleAsciiString("");
            mplew.writeShort(0);
            mplew.write(0);
            mplew.writeShort(0);
            mplew.write(0);
        } else {
            MapleGuildSummary gs = chr.getClient().getWorldServer().getGuildSummary(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
        }

        long fbuffmask = 0xFFFC0000000000L; //becomes F8000000 after bb?
        mplew.writeLong(fbuffmask);

        List<Pair<Integer, Boolean>> buffvalue = new ArrayList<Pair<Integer, Boolean>>();

        long buffmask = 0;

        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden()) {
            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            buffmask |= MapleBuffStat.COMBO.getValue();
            buffvalue.add(new Pair<Integer, Boolean>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue()), false));
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            buffmask |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            buffmask |= MapleBuffStat.MORPH.getValue();
            buffvalue.add(new Pair<Integer, Boolean>(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue()), true));
        }

        mplew.writeLong(buffmask);
        for (Pair<Integer, Boolean> i : buffvalue) {
            if (i.right) {
                mplew.writeShort(i.left.shortValue());
            } else {
                mplew.write(i.left.byteValue());
            }
        }

        int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        mplew.writeShort(0); //start of energy charge
        mplew.writeLong(0);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0); //start of dash_speed
        mplew.writeLong(0);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0); //start of dash_jump
        mplew.writeLong(0);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0); //start of Monster Riding
        int buffSrc = chr.getBuffSource(MapleBuffStat.MONSTER_RIDING);
        final Item mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null && mount != null) {
            mplew.writeInt(mount.getItemId());
            mplew.writeInt(buffSrc);
        } else {
            mplew.writeLong(0);
        }
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0); //speed infusion behaves differently here
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeInt(1);
        mplew.writeLong(0); //homing beacon
        mplew.write(0);
        mplew.writeShort(0);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeInt(0); //and finally, something ive no idea
        mplew.writeLong(0);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        mplew.writeLong(0); // 台版自己加的
        mplew.write(1); // 台版自己加的
        mplew.writeInt(CHAR_MAGIC_SPAWN); // 台版自己加的
        mplew.writeShort(chr.getJob().getId());
        addCharLook(mplew, chr, false);
        mplew.writeInt(chr.getInventory(MapleInventoryType.CASH).countById(5110000));
        mplew.writeInt(chr.getItemEffect());
        mplew.writeInt(0); // 台版自己加的
        mplew.writeInt(0); // 只有台陸版有
        mplew.writeInt(-1); // 只有台陸版有
        mplew.writeInt(ItemConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
        mplew.writePos(chr.getPosition());
        mplew.write(chr.getStance());
        mplew.writeShort(chr.getFh());//chr.getFh()
        mplew.write(0);

        if (chr.getMount() == null) {
            mplew.writeInt(1); // mob level
            mplew.writeInt(0); // mob exp  
            mplew.writeInt(0); //tiredness
        } else {
            mplew.writeInt(chr.getMount().getLevel());
            mplew.writeInt(chr.getMount().getExp());
            mplew.writeInt(chr.getMount().getTiredness());
        }

        if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr)) {
            if (chr.getPlayerShop().hasFreeSlot()) {
                addAnnounceBox(mplew, chr.getPlayerShop(), chr.getPlayerShop().getVisitors().length);
            } else {
                addAnnounceBox(mplew, chr.getPlayerShop(), 1);
            }
        } else if (chr.getMiniGame() != null && chr.getMiniGame().isOwner(chr)) {
            if (chr.getMiniGame().hasFreeSlot()) {
                addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 1, 0);
            } else {
                addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 2, 1);
            }
        } else {
            mplew.write(0);
        }

        if (chr.getChalkboard() != null) {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getChalkboard());
        } else {
            mplew.write(0);
        }

        addRingLook(mplew, chr, true);
        addRingLook(mplew, chr, false);
        addMarriageRingLook(mplew, chr);
        mplew.skip(2);
        //mplew.write(chr.getTeam());//only needed in specific fields
        return mplew.getPacket();
    }

    private static void addRingLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean crush) {
        List<MapleRing> rings;
        if (crush) {
            rings = chr.getCrushRings();
        } else {
            rings = chr.getFriendshipRings();
        }
        mplew.writeBool(rings.size() > 0);
        mplew.writeInt(rings.size());
        for (MapleRing ring : rings) {
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
            mplew.writeInt(ring.getItemId());
        }
    }

    private static void addMarriageRingLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if (chr.getMarriageRing() != null && !chr.getMarriageRing().equipped()) {
            mplew.write(0);
            return;
        }
        mplew.writeBool(chr.getMarriageRing() != null);
        if (chr.getMarriageRing() != null) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing().getPartnerChrId());
            mplew.writeInt(chr.getMarriageRing().getRingId());
        }
    }

    /**
     * Adds a announcement box to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to add an announcement box
     * to.
     * @param shop The shop to announce.
     */
    private static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop, int availability) {
        mplew.write(4);
        mplew.writeInt(shop.getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        mplew.write(1);
        mplew.write(1);
        mplew.write(shop.getItems().size());
        mplew.write(shop.getVisitors().length);
        mplew.write(availability);
    }

    private static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MapleMiniGame game, int gametype, int type, int ammount, int joinable) {
        mplew.write(gametype);
        mplew.writeInt(game.getObjectId()); // gameid/shopid
        mplew.writeMapleAsciiString(game.getDescription()); // desc
        mplew.write(0);
        mplew.write(type);
        mplew.write(ammount);
        mplew.write(2);
        mplew.write(joinable);
    }

    public static byte[] facialExpression(MapleCharacter from, int expression) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
        mplew.writeShort(SendOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        return mplew.getPacket();
    }

    private static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static byte[] movePlayer(MapleCharacter chr, List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.writePos(chr.getPosition());
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static byte[] moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writePos(startPos);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static byte[] moveMonster(int useskill, int skill, int skill_1, int skill_2, int skill_3, int skill_4, int oid, Point startPos, List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.write(useskill);
        mplew.write(skill);
        mplew.write(skill_1);
        mplew.write(skill_2);
        mplew.write(skill_3);
        mplew.write(skill_4);
        mplew.writePos(startPos);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static byte[] summonAttack(int cid, int summonSkillId, byte direction, List<SummonAttackEntry> allDamage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //b2 00 29 f7 00 00 9a a3 04 00 c8 04 01 94 a3 04 00 06 ff 2b 00
        mplew.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(direction);
        mplew.write(4);
        mplew.write(allDamage.size());
        for (SummonAttackEntry attackEntry : allDamage) {
            mplew.writeInt(attackEntry.getMonsterOid()); // oid
            mplew.write(6); // who knows
            mplew.writeInt(attackEntry.getDamage()); // damage
        }
        return mplew.getPacket();
    }

    public static byte[] closeRangeAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLOSE_RANGE_ATTACK.getValue());
        addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
        return mplew.getPacket();
    }

    public static byte[] rangedAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.RANGED_ATTACK.getValue());
        addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, projectile, damage, speed, direction, display);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] magicAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, Map<Integer, List<Integer>> damage, int charge, int speed, int direction, int display) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAGIC_ATTACK.getValue());
        addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
        if (charge != -1) {
            mplew.writeInt(charge);
        }
        return mplew.getPacket();
    }

    private static void addAttackBody(LittleEndianWriter lew, MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
        lew.writeInt(chr.getId());
        lew.write(numAttackedAndDamage);
        lew.write(skilllevel);
        if (skilllevel > 0) {
            lew.write(skilllevel);
            lew.writeInt(skill);
        } else {
            lew.write(0);
        }
        lew.write(0);
        lew.write(display);
        lew.write(direction);
        lew.write(speed);
        lew.write(stance);
        lew.writeInt(projectile);
        for (Integer oned : damage.keySet()) {
            List<Integer> onedList = damage.get(oned);
            if (onedList != null) {
                lew.writeInt(oned.intValue());
                lew.write(0x07);
                if (skill == 4211006) {
                    lew.write(onedList.size());
                }
                for (Integer eachd : onedList) {
                    lew.writeInt(eachd.intValue());
                }
            }
        }
    }

    private static int doubleToShortBits(double d) {
        return (int) (Double.doubleToLongBits(d) >> 48);
    }

    public static byte[] getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        mplew.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        MapleShopItem i = items.get(0);
        mplew.writeShort(items.size()); // item count

        for (MapleShopItem item : items) {

            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
            if (!ItemConstants.isRechargable(item.getItemId())) {
                mplew.writeShort(1); // stacksize o.o
                mplew.writeShort(item.getBuyable());
            } else {
                mplew.writeZeroBytes(6);
                mplew.writeShort(doubleToShortBits(ii.getPrice(item.getItemId())));
                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
            }
        }
        return mplew.getPacket();
    }

    /* 00 = /
     * 01 = You don't have enough in stock
     * 02 = You do not have enough mesos
     * 03 = Please check if your inventory is full or not
     * 05 = You don't have enough in stock
     * 06 = Due to an error, the trade did not happen
     * 07 = Due to an error, the trade did not happen
     * 08 = /
     * 0D = You need more items
     * 0E = CRASH; LENGTH NEEDS TO BE LONGER :O
     */
    public static byte[] shopTransaction(byte code) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        mplew.write(code);
        return mplew.getPacket();
    }

    public static byte[] updateInventorySlotLimit(int type, int newLimit) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.INVENTORY_GROW.getValue());
        mplew.write(type);
        mplew.write(newLimit);
        return mplew.getPacket();
    }

    public static byte[] modifyInventory(boolean updateTick, final List<ModifyInventory> mods) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.INVENTORY_OPERATION.getValue());
        mplew.writeBool(updateTick);
        mplew.write(mods.size());
        int addMovement = -1;
        for (ModifyInventory mod : mods) {
            mplew.write(mod.getMode());
            mplew.write(mod.getInventoryType());
            mplew.writeShort(mod.getMode() == 2 ? mod.getOldPosition() : mod.getPosition());
            switch (mod.getMode()) {
                case 0: {//add item
                    addItemInfo(mplew, mod.getItem(), true);
                    break;
                }
                case 1: {//update quantity
                    mplew.writeShort(mod.getQuantity());
                    break;
                }
                case 2: {//move                  
                    mplew.writeShort(mod.getPosition());
                    if (mod.getPosition() < 0 || mod.getOldPosition() < 0) {
                        addMovement = mod.getOldPosition() < 0 ? 1 : 2;
                    }
                    break;
                }
                case 3: {//remove
                    if (mod.getPosition() < 0) {
                        addMovement = 2;
                    }
                    break;
                }
            }
            mod.clear();
        }
        if (addMovement > -1) {
            mplew.write(addMovement);
        }
        return mplew.getPacket();
    }

    public static byte[] getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_UPGRADE_EFFECT.getValue());
        mplew.writeInt(chr);
        switch (scrollSuccess) {
            case SUCCESS:
                mplew.writeShort(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case FAIL:
                mplew.writeShort(0);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case CURSE:
                mplew.write(0);
                mplew.write(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] removePlayerFromMap(int cid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);
        return mplew.getPacket();
    }

    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/> 4 -
     * explode<br/> cid is ignored for 0 and 1
     *
     * @param oid
     * @param animation
     * @param cid
     * @return
     */
    public static byte[] removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, false, 0);
    }

    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/> 4 -
     * explode<br/> cid is ignored for 0 and 1.<br /><br />Flagging pet as true
     * will make a pet pick up the item.
     *
     * @param oid
     * @param animation
     * @param cid
     * @param pet
     * @param slot
     * @return
     */
    public static byte[] removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation); // expire
        mplew.writeInt(oid);
        if (animation == 2 || animation == 3 || animation == 5) {
            mplew.writeInt(cid);
            if (pet) {
                mplew.write(slot);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] updateCharLook(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.AVARTAR_MODFIED.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        addCharLook(mplew, chr, false);
        addRingLook(mplew, chr, true);
        addRingLook(mplew, chr, false);
        addMarriageRingLook(mplew, chr);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(skill);
        mplew.writeInt(damage);
        mplew.writeInt(monsteridfrom);
        mplew.write(direction);
        if (pgmr) {
            mplew.write(pgmr_1);
            mplew.write(is_pg ? 1 : 0);
            mplew.writeInt(oid);
            mplew.write(6);
            mplew.writeShort(pos_x);
            mplew.writeShort(pos_y);
            mplew.write(0);
        } else {
            mplew.writeShort(0);
        }
        mplew.writeInt(damage);
        if (fake > 0) {
            mplew.writeInt(fake);
        }
        return mplew.getPacket();
    }

    public static byte[] charNameResponse(String charname, boolean nameUsed) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] addNewCharEntry(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(0);
        addCharEntry(mplew, chr, false);
        return mplew.getPacket();
    }

    /**
     * state 0 = del ok state 12 = invalid bday state 14 = incorrect pic
     *
     * @param cid
     * @param state
     * @return
     */
    public static byte[] deleteCharResponse(int cid, int state) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DELETE_CHAR_RESPONSE.getValue());
        mplew.writeInt(cid);
        mplew.write(state);
        return mplew.getPacket();
    }

    /**
     *
     * @param chr
     * @param isSelf
     * @return
     */
    public static byte[] charInfo(MapleCharacter chr) {
        //3D 00 0A 43 01 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob().getId());
        mplew.writeShort(chr.getFame());
        mplew.write(chr.getMarriageRing() != null ? 1 : 0);
        String guildName = "";
        String allianceName = "";
        MapleGuildSummary gs = chr.getClient().getWorldServer().getGuildSummary(chr.getGuildId());
        if (chr.getGuildId() > 0 && gs != null) {
            guildName = gs.getName();
            MapleAlliance alliance = Server.getInstance().getAlliance(gs.getAllianceId());
            if (alliance != null) {
                allianceName = alliance.getName();
            }
        }
        mplew.writeMapleAsciiString(guildName);
        mplew.writeMapleAsciiString(allianceName);

        mplew.writeMapleAsciiString(chr.getProfileMessage()); // 角色訊息
        mplew.write(chr.getProfileEmotion());// 表情
        mplew.write(chr.getProfileConstellation());// 星座
        mplew.write(chr.getProfileBloodType());// 血型
        mplew.write(chr.getProfileBirthMonth());// 月
        mplew.write(chr.getProfileBirthDay());// 日

        MaplePet[] pets = chr.getPets();
        Item inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -114);
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.write(pets[i].getId());
                mplew.writeInt(pets[i].getItemId()); // petid
                mplew.writeMapleAsciiString(pets[i].getName());
                mplew.write(pets[i].getLevel()); // pet level
                mplew.writeShort(pets[i].getCloseness()); // pet closeness
                mplew.write(pets[i].getFullness()); // pet fullness
                mplew.writeShort(0);
                mplew.writeInt(inv != null ? inv.getItemId() : 0);
            }
        }
        mplew.write(0); //end of pets

        if (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            final int itemid = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId();
            final MapleMount mount = chr.getMount();
            final boolean canwear = MapleItemInformationProvider.getInstance().getReqLevel(itemid) <= chr.getLevel();
            mplew.write(canwear ? 1 : 0);
            if (canwear) {
                mplew.writeInt(mount.getLevel());
                mplew.writeInt(mount.getExp());
                mplew.writeInt(mount.getTiredness());
            }
        } else {
            mplew.write(0);
        }

        mplew.write(chr.getCashShop().getWishList().size());
        for (int sn : chr.getCashShop().getWishList()) {
            mplew.writeInt(sn);
        }

        mplew.writeInt(chr.getMonsterBook().getBookLevel());
        mplew.writeInt(chr.getMonsterBook().getNormalCard());
        mplew.writeInt(chr.getMonsterBook().getSpecialCard());
        mplew.writeInt(chr.getMonsterBook().getTotalCards());
        mplew.writeInt(chr.getMonsterBookCover() > 0 ? MapleItemInformationProvider.getInstance().getCardMobId(chr.getMonsterBookCover()) : 0);

        Item medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -21);

        if (medal != null) {
            mplew.writeInt(medal.getItemId());
        } else {
            mplew.writeInt(0);
        }

        ArrayList<Short> medalQuests = new ArrayList<>();
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        for (MapleQuestStatus q : completed) {
            if (q.getQuest().getId() >= 29000) { // && q.getQuest().getId() <= 29923
                medalQuests.add(q.getQuest().getId());
            }
        }

        Collections.sort(medalQuests);
        mplew.writeShort(medalQuests.size());
        for (Short s : medalQuests) {
            mplew.writeShort(s);
        }
        return mplew.getPacket();
    }

    /**
     * It is important that statups is in the correct order (see decleration
     * order in MapleBuffStat) since this method doesn't do automagical
     * reordering.
     *
     * @param buffid
     * @param bufflength
     * @param statups
     * @return
     */
    //1F 00 00 00 00 00 03 00 00 40 00 00 00 E0 00 00 00 00 00 00 00 00 E0 01 8E AA 4F 00 00 C2 EB 0B E0 01 8E AA 4F 00 00 C2 EB 0B 0C 00 8E AA 4F 00 00 C2 EB 0B 44 02 8E AA 4F 00 00 C2 EB 0B 44 02 8E AA 4F 00 00 C2 EB 0B 00 00 E0 7A 1D 00 8E AA 4F 00 00 00 00 00 00 00 00 03
    public static byte[] giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TEMPORY_STAT_SET.getValue());
        boolean special = false;
        writeLongMask(mplew, statups);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft().equals(MapleBuffStat.MONSTER_RIDING)
                    || statup.getLeft().equals(MapleBuffStat.HOMING_BEACON)) {
                special = true;
            }
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.writeInt(bufflength);
        }
        mplew.writeInt(0);
        mplew.writeInt(statups.get(0).getRight()); //Homing beacon ...

        if (special) {
            mplew.skip(1);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param cid
     * @param statups
     * @param mount
     * @return
     */
    public static byte[] showMonsterRiding(int cid, List<Pair<MapleBuffStat, Integer>> statups, int itemId, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.SET_TEMPORARY_STAT.getValue());
        mplew.writeInt(cid);

        writeLongMask(mplew, statups);

        mplew.writeShort(0);
        mplew.writeInt(itemId);
        mplew.writeInt(skillId);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    /*        mplew.writeInt(cid);
     writeLongMask(mplew, statups);
     for (Pair<MapleBuffStat, Integer> statup : statups) {
     if (morph) {
     mplew.writeInt(statup.getRight().intValue());
     } else {
     mplew.writeShort(statup.getRight().shortValue());
     }
     }
     mplew.writeShort(0);
     mplew.write(0);*/
    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static byte[] forfeitQuest(short quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static byte[] completeQuest(short quest, long time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(2);
        mplew.writeLong(time);
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @param npc
     * @param progress
     * @return
     */
    public static byte[] updateQuestInfo(short quest, int npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(8); //0x0A in v95
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] addQuestTimeLimit(final short quest, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(6);
        mplew.writeShort(1);//Size but meh, when will there be 2 at the same time? And it won't even replace the old one :)
        mplew.writeShort(quest);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static byte[] removeQuestTimeLimit(final short quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(7);
        mplew.writeShort(1);//Position
        mplew.writeShort(quest);
        return mplew.getPacket();
    }

    public static byte[] updateQuest(final short quest, final String status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(1);
        mplew.writeMapleAsciiString(status);
        return mplew.getPacket();
    }

    private static <E> long getLongMaskD(List<Pair<MapleDisease, Integer>> statups) {
        long mask = 0;
        for (Pair<MapleDisease, Integer> statup : statups) {
            mask |= statup.getLeft().getValue();
        }
        return mask;
    }

    public static byte[] giveDebuff(List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TEMPORY_STAT_SET.getValue());
        long mask = getLongMaskD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (Pair<MapleDisease, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeShort(skill.getSkillId());
            mplew.writeShort(skill.getSkillLevel());
            mplew.writeInt((int) skill.getDuration());
        }
        mplew.writeShort(0); // ??? wk charges have 600 here o.o
        mplew.writeShort(900);//Delay
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] giveForeignDebuff(int cid, List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_TEMPORARY_STAT.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (int i = 0; i < statups.size(); i++) {
            mplew.writeShort(skill.getSkillId());
            mplew.writeShort(skill.getSkillLevel());
        }
        mplew.writeShort(0); // same as give_buff
        mplew.writeShort(900);//Delay
        return mplew.getPacket();
    }

    public static byte[] cancelForeignDebuff(int cid, long mask) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.RESET_TEMPORARY_STAT.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        return mplew.getPacket();
    }

    public static byte[] giveForeignBuff(int cid, List<Pair<MapleBuffStat, Integer>> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_TEMPORARY_STAT.getValue());
        mplew.writeInt(cid);
        writeLongMask(mplew, statups);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
        }
        mplew.writeInt(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.RESET_TEMPORARY_STAT.getValue());
        mplew.writeInt(cid);
        writeLongMaskFromList(mplew, statups);
        return mplew.getPacket();
    }

    public static byte[] cancelBuff(List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TEMPORY_STAT_RESET.getValue());
        writeLongMaskFromList(mplew, statups);
        mplew.write(1);//?
        return mplew.getPacket();
    }

    private static void writeLongMask(final MaplePacketLittleEndianWriter mplew, List<Pair<MapleBuffStat, Integer>> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    private static void writeLongMaskFromList(final MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (MapleBuffStat statup : statups) {
            if (statup.isFirst()) {
                firstmask |= statup.getValue();
            } else {
                secondmask |= statup.getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    public static byte[] cancelDebuff(long mask) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
        mplew.writeShort(SendOpcode.TEMPORY_STAT_RESET.getValue());
        mplew.writeLong(0);
        mplew.writeLong(mask);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopNewVisitor(MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopRemoveVisitor(int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot > 0) {
            mplew.write(slot);
        }
        return mplew.getPacket();
    }

    public static byte[] getTradePartnerAdd(MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(1);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        return mplew.getPacket();
    }

    public static byte[] getTradeInvite(MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.INVITE.getCode());
        mplew.write(3);
        mplew.writeMapleAsciiString(c.getName());
        mplew.write(new byte[]{(byte) 0xB7, (byte) 0x50, 0, 0});
        return mplew.getPacket();
    }

    public static byte[] getTradeMesoSet(byte number, int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SET_MESO.getCode());
        mplew.write(number);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    public static byte[] getTradeItemAdd(byte number, Item item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SET_ITEMS.getCode());
        mplew.write(number);
        mplew.write(item.getPosition());
        addItemInfo(mplew, item, true);
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopItemUpdate(MaplePlayerShop shop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        mplew.write(shop.getItems().size());
        for (MaplePlayerShopItem item : shop.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param shop
     * @param owner
     * @return
     */
    public static byte[] getPlayerShop(MapleClient c, MaplePlayerShop shop, boolean owner) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(4);
        mplew.write(4);
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());
        mplew.write(1);
        addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(shop.getDescription());
        List<MaplePlayerShopItem> items = shop.getItems();
        mplew.write(0x10);
        mplew.write(items.size());
        for (MaplePlayerShopItem item : items) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true);
        }
        return mplew.getPacket();
    }

    public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(3);
        mplew.write(2);
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            addCharLook(mplew, trade.getPartner().getChr(), false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
        }
        mplew.write(number);
        addCharLook(mplew, c.getPlayer(), false);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.write(0xFF);
        return mplew.getPacket();
    }

    public static byte[] getTradeConfirmation() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CONFIRM.getCode());
        return mplew.getPacket();
    }

    public static byte[] getTradeCompletion(byte number) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(number);
        mplew.write(6);
        return mplew.getPacket();
    }

    public static byte[] getTradeCancel(byte number) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(number);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static byte[] addCharBox(MapleCharacter c, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getPlayerShop(), type);
        return mplew.getPacket();
    }

    public static byte[] removeCharBox(MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Possible values for <code>speaker</code>:<br> 0: Npc talking (left)<br>
     * 1: Npc talking (right)<br> 2: Player talking (left)<br> 3: Player talking
     * (left)<br>
     *
     * @param npc Npcid
     * @param msgType
     * @param talk
     * @param endBytes
     * @param speaker
     * @return
     */
    public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte speaker) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(msgType);
        mplew.write(speaker);
        mplew.writeMapleAsciiString(talk);
        mplew.write(HexTool.getByteArrayFromHexString(endBytes));
        return mplew.getPacket();
    }

    public static byte[] getDimensionalMirror(String talk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(9010022);
        mplew.write(0x0E);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(talk);
        return mplew.getPacket();
    }

    public static final byte[] getMapSelection(final int npcid, final String sel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npcid);
        mplew.writeShort(0xD);
        mplew.writeInt(0);
        mplew.writeInt(5);
        mplew.writeMapleAsciiString(sel);

        return mplew.getPacket();
    }

    public static byte[] getNPCTalkStyle(int npc, String talk, int styles[]) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(7);
        mplew.write(0); //speaker
        mplew.writeMapleAsciiString(talk);
        mplew.write(styles.length);
        for (int i = 0; i < styles.length; i++) {
            mplew.writeInt(styles[i]);
        }
        return mplew.getPacket();
    }

    public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(3);
        mplew.write(0); //speaker
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] getNPCTalkText(int npc, String talk, String def) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // Doesn't matter
        mplew.writeInt(npc);
        mplew.write(2);
        mplew.write(0); //speaker
        mplew.writeMapleAsciiString(talk);
        mplew.writeMapleAsciiString(def);//:D
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] showBuffeffect(int cid, int skillid, int effectid) {
        return showBuffeffect(cid, skillid, effectid, (byte) 3);
    }

    public static byte[] showBuffeffect(int cid, int skillid, int effectid, byte direction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effectid); //buff level
        mplew.writeInt(skillid);
        mplew.write(1); //skill level = 1 for the lulz
        mplew.write(1); //actually skill level ? 0 = dosnt show
        if (direction != (byte) 3) {
            mplew.write(direction);
        }
        return mplew.getPacket();
    }

    public static byte[] showOwnBuffEffect(int skillid, int effectid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(0xA9);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] showOwnBerserk(int skilllevel, boolean Berserk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(0xA9);
        mplew.write(skilllevel);
        mplew.write(Berserk ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] showBerserk(int cid, int skilllevel, boolean Berserk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(0xA9);
        mplew.write(skilllevel);
        mplew.write(Berserk ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        addExpirationTime(mplew, expiration);
        mplew.write(4);
        return mplew.getPacket();
    }

    public static byte[] getShowQuestCompletion(int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUEST_CLEAR.getValue());
        mplew.writeShort(id);
        return mplew.getPacket();
    }

    public static byte[] getKeymap(Map<Integer, MapleKeyBinding> keybindings) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.KEYMAP.getValue());
        mplew.write(0);
        for (int x = 0; x < 90; x++) {
            MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
            if (binding != null) {
                mplew.write(binding.getType());
                mplew.writeInt(binding.getAction());
            } else {
                mplew.write(0);
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] getWhisper(String sender, int channel, String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1); // I guess this is the channel
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    /**
     *
     * @param target name of the target character
     * @param reply error code: 0x0 = cannot find char, 0x1 = success
     * @return the MaplePacket
     */
    public static byte[] getWhisperReply(String target, byte reply) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(0x0A); // whisper?
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);
        return mplew.getPacket();
    }

    public static byte[] getInventoryFull() {
        return modifyInventory(true, Collections.EMPTY_LIST);
    }

    public static byte[] getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

    public static byte[] showItemUnavailable() {
        return getShowInventoryStatus(0xfe);
    }

    public static byte[] getShowInventoryStatus(int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.STORAGE.getValue());
        mplew.write(0x16);
        mplew.writeInt(npcId);
        mplew.write(slots);
        mplew.writeShort(0x7E);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        mplew.writeShort(0);
        mplew.write((byte) items.size());
        for (Item item : items) {
            addItemInfo(mplew, item, true);
        }
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    /*
     * 0x0A = Inv full
     * 0x0B = You do not have enough mesos
     * 0x0C = One-Of-A-Kind error
     */
    public static byte[] getStorageError(byte i) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.STORAGE.getValue());
        mplew.write(i);
        return mplew.getPacket();
    }

    public static byte[] mesoStorage(byte slots, int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.STORAGE.getValue());
        mplew.write(0x13);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.STORAGE.getValue());
        mplew.write(0xD);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (Item item : items) {
            addItemInfo(mplew, item, true);
        }
        return mplew.getPacket();
    }

    public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.STORAGE.getValue());
        mplew.write(0x9);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (Item item : items) {
            addItemInfo(mplew, item, true);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param remhppercentage
     * @return
     */
    public static byte[] showMonsterHP(int oid, int remhppercentage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_MONSTER_HP.getValue());
        mplew.writeInt(oid);
        mplew.write(remhppercentage);
        return mplew.getPacket();
    }

    public static byte[] showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(5);
        mplew.writeInt(oid);
        mplew.writeInt(currHP);
        mplew.writeInt(maxHP);
        mplew.write(tagColor);
        mplew.write(tagBgColor);
        return mplew.getPacket();
    }

    public static byte[] giveFameResponse(int mode, String charname, int newfame) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeShort(newfame);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * status can be: <br> 0: ok, use giveFameResponse<br> 1: the username is
     * incorrectly entered<br> 2: users under level 15 are unable to toggle with
     * fame.<br> 3: can't raise or drop fame anymore today.<br> 4: can't raise
     * or drop fame for this character for this month anymore.<br> 5: received
     * fame, use receiveFame()<br> 6: level of fame neither has been raised nor
     * dropped due to an unexpected error
     *
     * @param status
     * @return
     */
    public static byte[] giveFameErrorResponse(int status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
        mplew.write(status);
        return mplew.getPacket();
    }

    public static byte[] receiveFame(int mode, String charnameFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static byte[] partyCreated(int partyid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(8);
        mplew.writeInt(partyid);
        mplew.writeInt(999999999);
        mplew.writeInt(999999999);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] partyInvite(MapleCharacter from) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * 10: A beginner can't create a party. 1/11/14/19: Your request for a party
     * didn't work due to an unexpected error. 13: You have yet to join a party.
     * 16: Already have joined a party. 17: The party you're trying to join is
     * already in full capacity. 19: Unable to find the requested character in
     * this channel.
     *
     * @param message
     * @return
     */
    public static byte[] partyStatusMessage(int message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    /**
     * 23: 'Char' have denied request to the party.
     *
     * @param message
     * @param charname
     * @return
     */
    public static byte[] partyStatusMessage(int message, String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.writeMapleAsciiString(charname);
        return mplew.getPacket();
    }

    private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter mplew, boolean leaving) {
        List<MaplePartyCharacter> partymembers = new ArrayList<>(party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            mplew.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            mplew.writeAsciiString(partychar.getName(), 15);
        }
        for (MaplePartyCharacter partychar : partymembers) {
            mplew.writeInt(partychar.getJobId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            mplew.writeInt(partychar.getLevel());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                mplew.writeInt(partychar.getChannel() - 1);
            } else {
                mplew.writeInt(-2);
            }
        }
        mplew.writeInt(party.getLeader().getId());
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                mplew.writeInt(partychar.getMapId());
            } else {
                mplew.writeInt(0);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                mplew.writeInt(partychar.getDoorTown());
                mplew.writeInt(partychar.getDoorTarget());
                mplew.writeInt(partychar.getDoorSkill());
                mplew.writeInt(partychar.getDoorPosition().x);
                mplew.writeInt(partychar.getDoorPosition().y);
            } else {
                mplew.writeInt(leaving ? 999999999 : 0);
                mplew.writeLong(leaving ? 999999999 : 0);
                mplew.writeLong(leaving ? -1 : 0);
            }
        }
    }

    public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE:
                mplew.write(0x0C);
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                mplew.write(op == PartyOperation.DISBAND ? 0 : 1);
                if (op == PartyOperation.DISBAND) {
                    mplew.writeInt(party.getId());
                } else {
                    mplew.write(op == PartyOperation.EXPEL ? 1 : 0);
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, op == PartyOperation.LEAVE);
                }
                break;
            case JOIN:
                mplew.write(0xF);
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                mplew.write(0x7);
                mplew.writeInt(party.getId());
                addPartyStatus(forChannel, party, mplew, op == PartyOperation.LOG_ONOFF);
                break;
            case CHANGE_LEADER:
            case CHANGE_LEADER_DC:
                mplew.write(0x1B);
                mplew.writeInt(target.getId());
                mplew.write(op == PartyOperation.CHANGE_LEADER_DC ? 1 : 0);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] partyPortal(int townId, int targetId, Point position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.writeShort(0x23);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writePos(position);
        return mplew.getPacket();
    }

    public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.RRCEIVE_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);
        return mplew.getPacket();
    }

    /**
     * mode: 0 buddychat; 1 partychat; 2 guildchat
     *
     * @param name
     * @param chattext
     * @param mode
     * @return
     */
    public static byte[] multiChat(String name, String chattext, int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);
        return mplew.getPacket();
    }

    private static void writeIntMask(final MaplePacketLittleEndianWriter mplew, Map<MonsterStatus, Integer> stats) {
        int firstmask = 0;
        int secondmask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            if (stat.isFirst()) {
                firstmask |= stat.getValue();
            } else {
                secondmask |= stat.getValue();
            }
        }
        mplew.writeInt(firstmask);
        mplew.writeInt(secondmask);
    }

    public static byte[] applyMonsterStatus(final int oid, final MonsterStatusEffect mse) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeLong(0);
        writeIntMask(mplew, mse.getStati());
        for (Map.Entry<MonsterStatus, Integer> stat : mse.getStati().entrySet()) {
            mplew.writeShort(stat.getValue());
            if (mse.isMonsterSkill()) {
                mplew.writeShort(mse.getMobSkill().getSkillId());
                mplew.writeShort(mse.getMobSkill().getSkillLevel());
            } else {
                mplew.writeInt(mse.getSkill().getId());
            }
            mplew.writeShort(-1); // might actually be the buffTime but it's not displayed anywhere
        }
        mplew.writeShort(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CANCEL_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeLong(0);
        mplew.writeInt(0);
        int mask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            mask |= stat.getValue();
        }
        mplew.writeInt(mask);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] getClock(int time) { // time in seconds
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLOCK.getValue());
        mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static byte[] getClockTime(int hour, int min, int sec) { // Current Time
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLOCK.getValue());
        mplew.write(1); //Clock-Type
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);
        return mplew.getPacket();
    }

    public static byte[] spawnKite(int oid, int itemid, String name, String msg, Point pos, int ft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_KITE.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(msg);
        mplew.writeMapleAsciiString(name);
        mplew.writeShort(pos.x);
        mplew.writeShort(ft);
        return mplew.getPacket();
    }

    public static byte[] removeKite(int oid, boolean status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REMOVE_KITE.getValue());
        mplew.writeBool(status);
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    public static byte[] sendKiteFailure() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.KITE_MESSAGE.getValue());
        return mplew.getPacket();
    }

    public static byte[] spawnMist(int oid, int ownerCid, int skill, int level, MapleMist mist) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(mist.isMobMist() ? 0 : mist.isPoisonMist() ? 1 : 2);
        mplew.writeInt(ownerCid);
        mplew.writeInt(mist.getSourceSkill() != null ? mist.getSourceSkill().getId() : skill);
        mplew.write(level);
        mplew.writeShort(mist.getSkillDelay()); // Skill delay
        mplew.writeInt(mist.getBox().x);
        mplew.writeInt(mist.getBox().y);
        mplew.writeInt(mist.getBox().x + mist.getBox().width);
        mplew.writeInt(mist.getBox().y + mist.getBox().height);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] removeMist(int oid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] damageMonster(int oid, int damage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt(damage > Integer.MAX_VALUE ? Integer.MAX_VALUE : damage);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] healMonster(int oid, int heal) {
        return damageMonster(oid, -heal);
    }

    public static byte[] itemEffect(int characterid, int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static byte[] showChair(int characterid, int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ACTIVE_PORTABLE_CHAIR.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static byte[] cancelChair(int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeShort(id);
        }
        return mplew.getPacket();
    }

    // is there a way to spawn reactors non-animated?
    public static byte[] spawnReactor(MapleReactor reactor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getId());
        mplew.write(reactor.getState());
        mplew.writePos(pos);
        mplew.write(reactor.getFacingDirection()); // stance
        mplew.writeMapleAsciiString(reactor.getName());
        return mplew.getPacket();
    }

    public static byte[] triggerReactor(MapleReactor reactor, int stance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(pos);
        mplew.writeShort(stance);
        mplew.write(0);
        mplew.write(4); // frame delay, set to 5 since there doesn't appear to be a fixed formula for it
        return mplew.getPacket();
    }

    public static byte[] destroyReactor(MapleReactor reactor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(pos);
        return mplew.getPacket();
    }

    public static byte[] playCashSong(int itemid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASH_SONG.getValue());
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }

    public static byte[] musicChange(String song) {
        return environmentChange(song, 6);
    }

    public static byte[] showEffect(String effect) {
        return environmentChange(effect, 3);
    }

    public static byte[] playSound(String sound) {
        return environmentChange(sound, 4);
    }

    public static byte[] environmentChange(String env, int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);
        return mplew.getPacket();
    }

    public static byte[] startMapEffect(String msg, int itemid, boolean active) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);
        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        return mplew.getPacket();
    }

    public static byte[] removeMapEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_EFFECT.getValue());
        mplew.write(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] mapEffect(String path) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(3);
        mplew.writeMapleAsciiString(path);
        return mplew.getPacket();
    }

    public static byte[] mapSound(String path) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(4);
        mplew.writeMapleAsciiString(path);
        return mplew.getPacket();
    }

    public static void addThread(final MaplePacketLittleEndianWriter mplew, ResultSet rs) throws SQLException {
        mplew.writeInt(rs.getInt("localthreadid"));
        mplew.writeInt(rs.getInt("postercid"));
        mplew.writeMapleAsciiString(rs.getString("name"));
        mplew.writeLong(PacketUtil.getTime(rs.getLong("timestamp")));
        mplew.writeInt(rs.getInt("icon"));
        mplew.writeInt(rs.getInt("replycount"));
    }

    public static byte[] showGuildRanks(int npcid, ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("GP"));
            mplew.writeInt(rs.getInt("logo"));
            mplew.writeInt(rs.getInt("logoColor"));
            mplew.writeInt(rs.getInt("logoBG"));
            mplew.writeInt(rs.getInt("logoBGColor"));
        }
        return mplew.getPacket();
    }

    public static byte[] updateGP(int gid, int GP) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x48);
        mplew.writeInt(gid);
        mplew.writeInt(GP);
        return mplew.getPacket();
    }

    public static byte[] skillEffect(MapleCharacter from, int skillId, int level, byte flags, int speed, byte direction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(flags);
        mplew.write(speed);
        mplew.write(direction); //Mmmk
        return mplew.getPacket();
    }

    public static byte[] skillCancel(MapleCharacter from, int skillId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        return mplew.getPacket();
    }

    public static byte[] showMagnet(int mobid, byte success) { // Monster Magnet
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success);
        mplew.skip(10); //Mmmk
        return mplew.getPacket();
    }

    /**
     * Sends a player hint.
     *
     * @param hint The hint it's going to send.
     * @param width How tall the box is going to be.
     * @param height How long the box is going to be.
     * @return The player hint packet.
     */
    public static byte[] sendHint(String hint, int width, int height) {
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] messengerInvite(String from, int messengerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x03);
        mplew.writeMapleAsciiString(from);
        mplew.write(0);
        mplew.writeInt(messengerid);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] sendSpouseChat(MapleCharacter wife, String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
        mplew.writeMapleAsciiString(wife.getName());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x00);
        mplew.write(position);
        addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static byte[] removeMessengerPlayer(int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x02);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x07);
        mplew.write(position);
        addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static byte[] joinMessenger(int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x01);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static byte[] messengerChat(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x06);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] messengerNote(String text, int mode, int mode2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);
        return mplew.getPacket();
    }

    public static void addPetInfo(final MaplePacketLittleEndianWriter mplew, MaplePet pet, boolean showpet) {
        mplew.write(1);
        if (showpet) {
            mplew.write(0);
        }

        mplew.writeInt(pet.getItemId());
        mplew.writeMapleAsciiString(pet.getName());
        mplew.writeInt(pet.getId());
        mplew.writeInt(0);
        mplew.writePos(pet.getPos());
        mplew.write(pet.getStance());
        mplew.writeInt(pet.getFh());
    }

    public static byte[] showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_PET.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getPetIndex(pet));
        if (remove) {
            mplew.write(0);
            mplew.write(hunger ? 1 : 0);
        } else {
            addPetInfo(mplew, pet, true);
        }
        return mplew.getPacket();
    }

    public static byte[] movePet(int cid, int pid, byte slot, List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_PET.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        mplew.writeInt(pid);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static byte[] petChat(int cid, byte index, int act, String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PET_CHAT.getValue());
        mplew.writeInt(cid);
        mplew.write(index);
        mplew.write(0);
        mplew.write(act);
        mplew.writeMapleAsciiString(text);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] commandResponse(int cid, byte index, int animation, boolean success) {
        //AE 00 01 00 00 00 00 01 00 00
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PET_COMMAND.getValue());
        mplew.writeInt(cid);
        mplew.write(index);
        mplew.write((animation == 1 || !success) ? 1 : 0);
        mplew.write(animation);
        if (animation == 1) {
            mplew.write(0);
        } else {
            mplew.writeShort(success ? 1 : 0);
        }
        return mplew.getPacket();
    }

    public static byte[] showOwnPetLevelUp(byte index) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(4);
        mplew.write(0);
        mplew.write(index); // Pet Index
        return mplew.getPacket();
    }

    public static byte[] showPetLevelUp(MapleCharacter chr, byte index) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(4);
        mplew.write(0);
        mplew.write(index);
        return mplew.getPacket();
    }

    public static byte[] changePetFlag(int uniqueId, boolean added, int flagAdded) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PET_FLAG_CHANGE.getValue());

        mplew.writeLong(uniqueId);
        mplew.write(added ? 1 : 0);
        mplew.writeShort(flagAdded);

        return mplew.getPacket();
    }

    public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PET_NAMECHANGE.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] petStatUpdate(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_STATS.getValue());
        int mask = 0;
        mask |= MapleStat.PET.getValue();
        mplew.write(0);
        mplew.writeInt(mask);
        MaplePet[] pets = chr.getPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.writeInt(pets[i].getId());
                mplew.writeInt(0);
            } else {
                mplew.writeLong(0);
            }
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] sendMesobagFailed() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESOBAG_FAILURE.getValue());
        return mplew.getPacket();
    }

    public static byte[] sendMesobagSuccess(int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESOBAG_SUCCESS.getValue());
        mplew.writeInt(mesos);
        return mplew.getPacket();
    }

    public static byte[] showForcedEquip(int team) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FORCED_MAP_EQUIP.getValue());
        if (team > -1) {
            mplew.write(team);   // 00 = red, 01 = blue
        }
        return mplew.getPacket();
    }

    public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        return mplew.getPacket();
    }

    public static byte[] skillCooldown(int sid, int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(time);//Int in v97
        return mplew.getPacket();
    }

    public static byte[] skillBookSuccess(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SKILL_LEARN_ITEM_RESULT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] getMacros(SkillMacro[] macros) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MACRO_SYS_DATA_INIT.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        return mplew.getPacket();
    }

    public static byte[] getPlayerNPC(PlayerNPCs npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.IMITATED_NPC_RESULT.getValue());
        mplew.write(0x01);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(0); // direction
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        Map<Byte, Integer> equip = npc.getEquips();
        Map<Byte, Integer> myEquip = new LinkedHashMap<>();
        for (byte position : equip.keySet()) {
            byte pos = (byte) (position * -1);
            if (pos > 100) {
                pos -= 100;
                myEquip.put(pos, equip.get(position));
            } else {
                if (myEquip.get(pos) == null) {
                    myEquip.put(pos, equip.get(position));
                }
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.writeShort(-1);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon);
        } else {
            mplew.writeInt(0);
        }
        for (int i = 0; i < 12; i++) {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static byte[] updateAriantPQRanking(String name, int score, boolean empty) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ARIANT_SCORE.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        return mplew.getPacket();
    }

    public static byte[] catchMonster(int monsobid, int itemid, byte success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CATCH_MONSTER.getValue());
        mplew.writeInt(monsobid);
        mplew.writeInt(itemid);
        mplew.write(success);
        return mplew.getPacket();
    }

    public static byte[] catchMessage(int message) { // not done, I guess
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BRIDLE_MOB_CATCH_FAIL.getValue());
        mplew.write(message); // 1 = too strong, 2 = Elemental Rock
        mplew.writeInt(0);//Maybe itemid?
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] updateMount(int charid, MapleMount mount, boolean levelup) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_TAMING_MOB_INFO.getValue());
        mplew.writeInt(charid);
        mplew.writeInt(mount.getLevel());
        mplew.writeInt(mount.getExp());
        mplew.writeInt(mount.getTiredness());
        mplew.write(levelup ? (byte) 1 : (byte) 0);
        return mplew.getPacket();
    }

    public static byte[] boatEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
        mplew.writeShort(SendOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(effect); // 0A 04 balrog
        //this packet had the other ones o.o

        return mplew.getPacket();
    }

    public static byte[] boatPacket(boolean type) {//don't think this is correct..
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BOAT_PACKET.getValue());
        mplew.write(type ? 1 : 2);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(1);
        mplew.write(0);
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(1);
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", true));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", true));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", true));
        mplew.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(1);
            mplew.writeInt(visitor.getMiniGamePoints("wins", true));
            mplew.writeInt(visitor.getMiniGamePoints("ties", true));
            mplew.writeInt(visitor.getMiniGamePoints("losses", true));
            mplew.writeInt(2000);
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameReady(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.READY.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameUnReady(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UN_READY.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameStart(MapleMiniGame game, int loser) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.START.getCode());
        mplew.write(loser);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameSkipOwner(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SKIP.getCode());
        mplew.write(0x01);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameRequestTie(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.REQUEST_TIE.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameDenyTie(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ANSWER_TIE.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameFull() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(0);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameSkipVisitor(MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.writeShort(PlayerInteractionHandler.Action.SKIP.getCode());
        return mplew.getPacket();
    }

    public static byte[] getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.MOVE_OMOK.getCode());
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameNewVisitor(MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMiniGamePoints("wins", true));
        mplew.writeInt(c.getMiniGamePoints("ties", true));
        mplew.writeInt(c.getMiniGamePoints("losses", true));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameRemoveVisitor() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(1);
        return mplew.getPacket();
    }

    private static byte[] getMiniGameResult(MapleMiniGame game, int win, int lose, int tie, int result, int forfeit, boolean omok) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.GET_RESULT.getCode());
        if (tie == 0 && forfeit != 1) {
            mplew.write(0);
        } else if (tie == 1) {
            mplew.write(1);
        } else if (forfeit == 1) {
            mplew.write(2);
        }
        mplew.write(0); // owner
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOwner().getMiniGamePoints("wins", omok) + win); // wins
        mplew.writeInt(game.getOwner().getMiniGamePoints("ties", omok) + tie); // ties
        mplew.writeInt(game.getOwner().getMiniGamePoints("losses", omok) + lose); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getMiniGamePoints("wins", omok) + lose); // wins
        mplew.writeInt(game.getVisitor().getMiniGamePoints("ties", omok) + tie); // ties
        mplew.writeInt(game.getVisitor().getMiniGamePoints("losses", omok) + win); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setMiniGamePoints(game.getVisitor(), result, omok);
        return mplew.getPacket();
    }

    public static byte[] getMiniGameOwnerWin(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 1, 0, true);
    }

    public static byte[] getMiniGameVisitorWin(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 2, 0, true);
    }

    public static byte[] getMiniGameTie(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 0, 1, 3, 0, true);
    }

    public static byte[] getMiniGameOwnerForfeit(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 1, true);
    }

    public static byte[] getMiniGameVisitorForfeit(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 1, true);
    }

    public static byte[] getMiniGameClose() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(1);
        mplew.write(3);
        return mplew.getPacket();
    }

    public static byte[] getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(2);
        mplew.write(2);
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(2);
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", false));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", false));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", false));
        mplew.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(2);
            mplew.writeInt(visitor.getMiniGamePoints("wins", false));
            mplew.writeInt(visitor.getMiniGamePoints("ties", false));
            mplew.writeInt(visitor.getMiniGamePoints("losses", false));
            mplew.writeInt(2000);
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.START.getCode());
        mplew.write(loser);
        mplew.write(0x0C);
        int last = 13;
        if (game.getMatchesToWin() > 10) {
            last = 31;
        } else if (game.getMatchesToWin() > 6) {
            last = 21;
        }
        for (int i = 1; i < last; i++) {
            mplew.writeInt(game.getCardId(i));
        }
        return mplew.getPacket();
    }

    public static byte[] getMatchCardNewVisitor(MapleCharacter c, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMiniGamePoints("wins", false));
        mplew.writeInt(c.getMiniGamePoints("ties", false));
        mplew.writeInt(c.getMiniGamePoints("losses", false));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static byte[] getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SELECT_CARD.getCode());
        mplew.write(turn);
        if (turn == 1) {
            mplew.write(slot);
        } else if (turn == 0) {
            mplew.write(slot);
            mplew.write(firstslot);
            mplew.write(type);
        }
        return mplew.getPacket();
    }

    public static byte[] getMatchCardOwnerWin(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 0, false);
    }

    public static byte[] getMatchCardVisitorWin(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 0, false);
    }

    public static byte[] getMatchCardTie(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 0, 1, 3, 0, false);
    }

    public static byte[] fredrickMessage(byte operation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FREDRICK_MESSAGE.getValue());
        mplew.write(operation);
        return mplew.getPacket();
    }

    public static byte[] getFredrick(byte op) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FREDRICK.getValue());
        mplew.write(op);

        switch (op) {
            case 0x24:
                mplew.skip(8);
                break;
            default:
                mplew.write(0);
                break;
        }

        return mplew.getPacket();
    }

    public static byte[] getFredrick(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FREDRICK.getValue());
        mplew.write(0x23);
        mplew.writeInt(9030000); // Fredrick
        mplew.writeInt(32272); //id
        mplew.skip(5);
        mplew.writeInt(chr.getMerchantMeso());
        mplew.write(0);
        try {
            List<Pair<Item, MapleInventoryType>> items = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
            mplew.write(items.size());

            for (int i = 0; i < items.size(); i++) {
                addItemInfo(mplew, items.get(i).getLeft(), true);
            }
        } catch (SQLException e) {
        }
        mplew.skip(3);
        return mplew.getPacket();
    }

    public static byte[] addOmokBox(MapleCharacter c, int ammount, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getMiniGame(), 1, 0, ammount, type);
        return mplew.getPacket();
    }

    public static byte[] removeOmokBox(MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] addMatchCardBox(MapleCharacter c, int ammount, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getMiniGame(), 2, 0, ammount, type);
        return mplew.getPacket();
    }

    public static byte[] removeMatchcardBox(MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] getPlayerShopChat(MapleCharacter c, String chat, byte slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(slot);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static byte[] getTradeChat(MapleCharacter c, String chat, boolean owner) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantBox() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
        mplew.write(0x07);
        return mplew.getPacket();
    }

    public static byte[] owlOfMinerva(MapleClient c, int itemid, List<HiredMerchant> hms, List<MaplePlayerShopItem> items) { //Thanks moongra, you save me some time :)
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOP_SCANNER_RESULT.getValue()); // header.
        mplew.write(6);
        mplew.writeInt(0);
        mplew.writeInt(itemid);
        mplew.writeInt(hms.size());
        for (HiredMerchant hm : hms) {
            for (MaplePlayerShopItem item : items) {
                mplew.writeMapleAsciiString(hm.getOwner());
                mplew.writeInt(hm.getMapId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.writeInt(item.getItem().getQuantity());
                mplew.writeInt(item.getBundles());
                mplew.writeInt(item.getPrice());
                mplew.writeInt(hm.getOwnerId());
                mplew.write(hm.getFreeSlot() == -1 ? 1 : 0);
                MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(hm.getOwnerId());
                if ((chr != null) && (c.getChannel() == hm.getChannel())) {
                    mplew.write(1);
                } else {
                    mplew.write(2);
                }

                if (item.getItem().getItemId() / 1000000 == 1) {
                    addItemInfo(mplew, item.getItem(), true);
                }
            }
        }
        return mplew.getPacket();
    }

    public static byte[] retrieveFirstMessage() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
        mplew.write(0x09);
        return mplew.getPacket();
    }

    public static byte[] remoteChannelChange(byte ch) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
        mplew.write(0x10);
        mplew.writeInt(0);//No idea yet
        mplew.write(ch);
        return mplew.getPacket();
    }
    /*
     * Possible things for ENTRUSTED_SHOP_CHECK_RESULT
     * 0x0E = 00 = Renaming Failed - Can't find the merchant, 01 = Renaming succesful
     * 0x10 = Changes channel to the store (Store is open at Channel 1, do you want to change channels?)
     * 0x11 = You cannot sell any items when managing.. blabla
     * 0x12 = FKING POPUP LOL
     */

    public static byte[] getHiredMerchant(MapleCharacter chr, HiredMerchant hm, boolean firstTime) {//Thanks Dustin
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(0x05);
        mplew.write(0x04);
        mplew.writeShort(hm.getVisitorSlot(chr) + 1);
        mplew.writeInt(hm.getItemId());
        mplew.writeMapleAsciiString("Hired Merchant");
        for (int i = 0; i < 3; i++) {
            if (hm.getVisitors()[i] != null) {
                mplew.write(i + 1);
                addCharLook(mplew, hm.getVisitors()[i], false);
                mplew.writeMapleAsciiString(hm.getVisitors()[i].getName());
            }
        }
        mplew.write(-1);
        if (hm.isOwner(chr)) {
            mplew.writeShort(hm.getMessages().size());
            for (int i = 0; i < hm.getMessages().size(); i++) {
                mplew.writeMapleAsciiString(hm.getMessages().get(i).getLeft());
                mplew.write(hm.getMessages().get(i).getRight());
            }
        } else {
            mplew.writeShort(0);
        }
        mplew.writeMapleAsciiString(hm.getOwner());
        if (hm.isOwner(chr)) {
            mplew.writeInt(hm.getTimeLeft());
            mplew.write(firstTime ? 1 : 0);
            //List<SoldItem> sold = hm.getSold();
            mplew.write(0);//sold.size()
            /*for (SoldItem s : sold) { fix this
             mplew.writeInt(s.getItemId());
             mplew.writeShort(s.getQuantity());
             mplew.writeInt(s.getMesos());
             mplew.writeMapleAsciiString(s.getBuyer());
             }*/
            mplew.writeInt(chr.getMerchantMeso());//:D?
        }
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.write(0x10); //SLOTS, which is 16 for most stores...slotMax
        mplew.writeInt(chr.getMeso());
        mplew.write(hm.getItems().size());
        if (hm.getItems().isEmpty()) {
            mplew.write(0);//Hmm??
        } else {
            for (MaplePlayerShopItem item : hm.getItems()) {
                mplew.writeShort(item.getBundles());
                mplew.writeShort(item.getItem().getQuantity());
                mplew.writeInt(item.getPrice());
                addItemInfo(mplew, item.getItem(), true);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] updateHiredMerchant(HiredMerchant hm, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        mplew.writeInt(chr.getMeso());
        mplew.write(hm.getItems().size());
        for (MaplePlayerShopItem item : hm.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true);
        }
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantChat(String message, byte slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(slot);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantVisitorLeave(int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot != 0) {
            mplew.write(slot);
        }
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantOwnerLeave() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] leaveHiredMerchant(int slot, int status2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(slot);
        mplew.write(status2);
        return mplew.getPacket();
    }

    public static byte[] hiredMerchantVisitorAdd(MapleCharacter chr, int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        return mplew.getPacket();
    }

    public static byte[] spawnHiredMerchant(HiredMerchant hm) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_HIRED_MERCHANT.getValue());
        mplew.writeInt(hm.getOwnerId());
        mplew.writeInt(hm.getItemId());
        mplew.writeShort((short) hm.getPosition().getX());
        mplew.writeShort((short) hm.getPosition().getY());
        mplew.writeShort(0);
        mplew.writeMapleAsciiString(hm.getOwner());
        mplew.write(0x05);
        mplew.writeInt(hm.getObjectId());
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.write(hm.getItemId() % 10);
        mplew.write(new byte[]{1, 4});
        return mplew.getPacket();
    }

    //營業執照
    public static byte[] spawnMiniBalloonHiredMerchant(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_MINI_ROOM_BALLOON.getValue());
        mplew.writeInt(chr.getId());
        addAnnounceBox(mplew, chr.getPlayerShop(), 1);
        return mplew.getPacket();
    }

    public static byte[] destroyHiredMerchant(int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.getValue());
        mplew.writeInt(id);
        return mplew.getPacket();
    }

    public static byte[] spawnPlayerNPC(PlayerNPCs npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(npc.getObjectId());
        mplew.writeInt(npc.getId());
        mplew.writeShort(npc.getPosition().x);
        mplew.writeShort(npc.getCY());
        mplew.write(1);
        mplew.writeShort(npc.getFH());
        mplew.writeShort(npc.getRX0());
        mplew.writeShort(npc.getRX1());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] sendYellowTip(String tip) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_WEEK_EVENT_MESSAGE.getValue());
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(tip);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] giveInfusion(int buffid, int bufflength, int speed) {//This ain't correct
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TEMPORY_STAT_SET.getValue());
        mplew.writeLong(MapleBuffStat.SPEED_INFUSION.getValue());
        mplew.writeLong(0);
        mplew.writeShort(speed);
        mplew.writeInt(buffid);
        mplew.write(0);
        mplew.writeShort(bufflength);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] givePirateBuff(List<Pair<MapleBuffStat, Integer>> statups, int buffid, int duration) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TEMPORY_STAT_SET.getValue());
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> stat : statups) {
            mplew.writeInt(stat.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.skip(5);
            mplew.writeShort(duration);
        }
        mplew.skip(3);
        return mplew.getPacket();
    }

    public static byte[] giveForeignDash(int cid, int buffid, int time, List<Pair<MapleBuffStat, Integer>> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_TEMPORARY_STAT.getValue());
        mplew.writeInt(cid);
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeInt(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.skip(5);
            mplew.writeShort(time);
        }
        mplew.writeShort(0);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static byte[] giveForeignInfusion(int cid, int speed, int duration) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SET_TEMPORARY_STAT.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(MapleBuffStat.SPEED_INFUSION.getValue());
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(speed);
        mplew.writeInt(5121009);
        mplew.writeLong(0);
        mplew.writeInt(duration);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static byte[] noteSendMsg() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
        mplew.write(4);
        return mplew.getPacket();
    }

    /*
     *  0 = Player online, use whisper
     *  1 = Check player's name
     *  2 = Receiver inbox full
     */
    public static byte[] noteError(byte error) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
        mplew.write(5);
        mplew.write(error);
        return mplew.getPacket();
    }

    public static byte[] showNotes(ResultSet notes, int count) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
        mplew.write(3);
        mplew.write(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(notes.getInt("id"));
            mplew.writeMapleAsciiString(notes.getString("from") + " ");//Stupid nexon forgot space lol
            mplew.writeMapleAsciiString(notes.getString("message"));
            mplew.writeLong(PacketUtil.getTime(notes.getLong("timestamp")));
            mplew.write(notes.getByte("fame"));//FAME :D
            notes.next();
        }
        return mplew.getPacket();
    }

    public static byte[] useChalkboard(MapleCharacter chr, boolean close) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHALKBOARD.getValue());
        mplew.writeInt(chr.getId());
        if (close) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        return mplew.getPacket();
    }

    public static byte[] trockRefreshMapList(MapleCharacter chr, boolean delete, boolean vip) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_TRANSFER_RESULT.getValue());
        mplew.write(delete ? 2 : 3);
        if (vip) {
            mplew.write(1);
            int[] map = chr.getVipTrockMaps();
            for (int i = 0; i < 10; i++) {
                mplew.writeInt(map[i]);
            }
        } else {
            mplew.write(0);
            int[] map = chr.getTrockMaps();
            for (int i = 0; i < 5; i++) {
                mplew.writeInt(map[i]);
            }
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param target
     * @param mapid
     * @param MTSmapCSchannel 0: MTS 1: Map 2: CS 3: Different Channel
     * @return
     */
    public static byte[] getFindReply(String target, int mapid, int MTSmapCSchannel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(target);
        mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
        mplew.writeInt(mapid); // -1 if mts, cs
        if (MTSmapCSchannel == 1) {
            mplew.write(new byte[8]);
        }
        return mplew.getPacket();
    }

    public static byte[] sendAutoHpPot(int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.AUTO_HP_POT.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }

    public static byte[] sendAutoMpPot(int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.AUTO_MP_POT.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }

    public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);
        return mplew.getPacket();
    }

    public static byte[] updateGender(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.SET_GENDER.getValue());
        mplew.write(chr.getGender());
        return mplew.getPacket();
    }

    public static byte[] enableReport() { // by snow
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.CLAIM_STATUS_CHANGED.getValue());
        mplew.write(7);
        return mplew.getPacket();
    }

    public static byte[] giveFinalAttack(int skillid, int time) {//packets found by lailainoob
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TEMPORY_STAT_SET.getValue());
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.write(0);//some 80 and 0 bs DIRECTION
        mplew.write(0x80);//let's just do 80, then 0
        mplew.writeInt(0);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(time);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] updateAreaInfo(int area, String info) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0x0A); //0x0B in v95
        mplew.writeShort(area);//infoNumber
        mplew.writeMapleAsciiString(info);
        return mplew.getPacket();
    }

    public static byte[] questProgress(short id, String process) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(id);
        mplew.write(1);
        mplew.writeMapleAsciiString(process);
        return mplew.getPacket();
    }

    public static byte[] getItemMessage(int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(7);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static byte[] addCard(boolean full, int cardid, int level) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.MONSTER_BOOK_SET_CARD.getValue());
        mplew.write(full ? 0 : 1);
        mplew.writeInt(cardid);
        mplew.writeInt(level);
        return mplew.getPacket();
    }

    public static byte[] showGainCard() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x0D);
        return mplew.getPacket();
    }

    public static byte[] showForeginCardEffect(int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(id);
        mplew.write(0x0D);
        return mplew.getPacket();
    }

    public static byte[] changeCover(int cardid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.MONSTER_BOOK_SET_COVER.getValue());
        mplew.writeInt(cardid);
        return mplew.getPacket();
    }

    public static byte[] aranGodlyStats() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FORCED_STAT_SET.getValue());
        mplew.write(new byte[]{(byte) 0x1F, (byte) 0x0F, 0, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xFF, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0x78, (byte) 0x8C});
        return mplew.getPacket();
    }

    public static byte[] showIntro(String path) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(path);
        return mplew.getPacket();
    }

    public static byte[] showInfo(String path) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x17);
        mplew.writeMapleAsciiString(path);
        mplew.writeInt(1);
        return mplew.getPacket();
    }

    public static byte[] removeNPC(int oid) { //Make npc's invisible
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(0);
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    /**
     * Sends a report response
     *
     * Possible values for <code>mode</code>:<br> 0: You have succesfully
     * reported the user.<br> 1: Unable to locate the user.<br> 2: You may only
     * report users 10 times a day.<br> 3: You have been reported to the GM's by
     * a user.<br> 4: Your request did not go through for unknown reasons.
     * Please try again later.<br>
     *
     * @param mode The mode
     * @return Report Reponse packet
     */
    public static byte[] reportResponse(byte mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SUE_CHARACTER_RESULT.getValue());
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static byte[] sendHammerData(int hammerUsed) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(0x39);
        mplew.writeInt(0);
        mplew.writeInt(hammerUsed);
        return mplew.getPacket();
    }

    public static byte[] sendHammerMessage() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(0x3D);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] playPortalSound() {
        return showSpecialEffect(7);
    }

    public static byte[] showMonsterBookPickup() {
        return showSpecialEffect(14);
    }

    public static byte[] showEquipmentLevelUp() {
        return showSpecialEffect(15);
    }

    public static byte[] showItemLevelup() {
        return showSpecialEffect(15);
    }

    /**
     * 6 = Exp did not drop (Safety Charms) 7 = Enter portal sound 8 = Job
     * change 9 = Quest complete 10 = Recovery 14 = Monster book pickup 15 =
     * Equipment levelup 16 = Maker Skill Success 19 = Exp card [500, 200, 50]
     *
     * @param effect
     * @return
     */
    public static byte[] showSpecialEffect(int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effect);
        return mplew.getPacket();
    }

    public static byte[] showForeignEffect(int cid, int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        return mplew.getPacket();
    }

    public static byte[] showOwnRecovery(byte heal) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x0A);
        mplew.write(heal);
        return mplew.getPacket();
    }

    public static byte[] showRecovery(int cid, byte amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(0x0A);
        mplew.write(amount);
        return mplew.getPacket();
    }

    public static byte[] showWheelsLeft(int left) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x15);
        mplew.write(left);
        return mplew.getPacket();
    }

    public static byte[] updateQuestFinish(short quest, int npc, short nextquest) { //Check
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue()); //0xF2 in v95
        mplew.write(8);//0x0A in v95
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeShort(nextquest);
        return mplew.getPacket();
    }

    public static byte[] showInfoText(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] questError(short quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(0x0A);
        mplew.writeShort(quest);
        return mplew.getPacket();
    }

    public static byte[] questFailure(byte type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(type);//0x0B = No meso, 0x0D = Worn by character, 0x0E = Not having the item ?
        return mplew.getPacket();
    }

    public static byte[] questExpire(short quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(0x0F);
        mplew.writeShort(quest);
        return mplew.getPacket();
    }

    /**
     * Gets a gm effect packet (ie. hide, banned, etc.)
     *
     * Possible values for <code>type</code>:<br> 0x04: You have successfully
     * blocked access.<br>
     * 0x05: The unblocking has been successful.<br> 0x06 with Mode 0: You have
     * successfully removed the name from the ranks.<br> 0x06 with Mode 1: You
     * have entered an invalid character name.<br> 0x10: GM Hide, mode
     * determines whether or not it is on.<br> 0x1E: Mode 0: Failed to send
     * warning Mode 1: Sent warning<br> 0x13 with Mode 0: + mapid 0x13 with Mode
     * 1: + ch (FF = Unable to find merchant)
     *
     * @param type The type
     * @param mode The mode
     * @return The gm effect packet
     */
    public static byte[] getGMEffect(int type, byte mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
        mplew.write(type);
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static byte[] findMerchantResponse(boolean map, int extra) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
        mplew.write(0x13);
        mplew.write(map ? 0 : 1); //00 = mapid, 01 = ch
        if (map) {
            mplew.writeInt(extra);
        } else {
            mplew.write(extra); //-1 = unable to find
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] disableMinimap() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
        mplew.writeShort(0x1C);
        return mplew.getPacket();
    }

    public static byte[] sendMesoLimit() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TRADE_MONEY_LIMIT.getValue()); //Players under level 15 can only trade 1m per day
        return mplew.getPacket();
    }

    public static byte[] sendEngagementRequest(String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MARRIAGE_REQUEST.getValue()); //<name> has requested engagement. Will you accept this proposal?
        mplew.write(0);
        mplew.writeMapleAsciiString(name); // name
        mplew.writeInt(10); // playerid
        return mplew.getPacket();
    }

    public static byte[] sendGroomWishlist() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MARRIAGE_REQUEST.getValue()); //<name> has requested engagement. Will you accept this proposal?
        mplew.write(9);
        return mplew.getPacket();
    }

    public static byte[] sendBrideWishList(List<Item> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WEDDING_GIFT_RESULT.getValue());
        mplew.write(0x0A);
        mplew.writeLong(-1); // ?
        mplew.writeInt(0); // ?
        mplew.write(items.size());
        for (Item item : items) {
            addItemInfo(mplew, item, true);
        }
        return mplew.getPacket();
    }

    public static byte[] addItemToWeddingRegistry(MapleCharacter chr, Item item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WEDDING_GIFT_RESULT.getValue());
        mplew.write(0x0B);
        mplew.writeInt(0);
        for (int i = 0; i < 0; i++) // f4
        {
            mplew.write(0);
        }

        addItemInfo(mplew, item, true);
        return mplew.getPacket();
    }

    public static byte[] removeItemFromDuey(boolean remove, int Package) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARCEL.getValue());
        mplew.write(0x17);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);
        return mplew.getPacket();
    }

    public static byte[] sendDueyMSG(byte operation) {
        return sendDuey(operation, null);
    }

    public static byte[] sendDuey(byte operation, List<DueyPackages> packages) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARCEL.getValue());
        mplew.write(operation);
        if (operation == 8) {
            mplew.write(0);
            mplew.write(packages.size());
            for (DueyPackages dp : packages) {
                mplew.writeInt(dp.getPackageId());
                mplew.writeAsciiString(dp.getSender());
                for (int i = dp.getSender().length(); i < 13; i++) {
                    mplew.write(0);
                }
                mplew.writeInt(dp.getMesos());
                mplew.writeLong(PacketUtil.getTime(dp.sentTimeInMilliseconds()));
                mplew.writeLong(0); // Contains message o____o.
                for (int i = 0; i < 48; i++) {
                    mplew.writeInt(Randomizer.nextInt(Integer.MAX_VALUE));
                }
                mplew.writeInt(0);
                mplew.write(0);
                if (dp.getItem() != null) {
                    mplew.write(1);
                    addItemInfo(mplew, dp.getItem(), true);
                } else {
                    mplew.write(0);
                }
            }
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static byte[] sendDojoAnimation(byte firstByte, String animation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(firstByte);
        mplew.writeMapleAsciiString(animation);
        return mplew.getPacket();
    }

    public static byte[] getDojoInfo(String info) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.write(new byte[]{(byte) 0xB7, 4});//QUEST ID f5
        mplew.writeMapleAsciiString(info);
        return mplew.getPacket();
    }

    public static byte[] getDojoInfoMessage(String message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    /**
     * Gets a "block" packet (ie. the cash shop is unavailable, etc)
     *
     * Possible values for <code>type</code>:<br> 1: The portal is closed for
     * now.<br> 2: You cannot go to that place.<br> 3: Unable to approach due to
     * the force of the ground.<br> 4: You cannot teleport to or on this
     * map.<br> 5: Unable to approach due to the force of the ground.<br> 6:
     * This map can only be entered by party members.<br> 7: The Cash Shop is
     * currently not available. Stay tuned...<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static byte[] blockedMessage(int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BLOCKED_MAP.getValue());
        mplew.write(type);
        return mplew.getPacket();
    }

    /**
     * Gets a "block" packet (ie. the cash shop is unavailable, etc)
     *
     * Possible values for <code>type</code>:<br> 1: You cannot move that
     * channel. Please try again later.<br> 2: You cannot go into the cash shop.
     * Please try again later.<br> 3: The Item-Trading Shop is currently
     * unavailable. Please try again later.<br> 4: You cannot go into the trade
     * shop, due to limitation of user count.<br> 5: You do not meet the minimum
     * level requirement to access the Trade Shop.<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static byte[] blockedMessage2(int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BLOCKED_SERVER.getValue());
        mplew.write(type);
        return mplew.getPacket();
    }

    public static byte[] updateDojoStats(MapleCharacter chr, int belt) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.write(new byte[]{(byte) 0xB7, 4}); //?
        mplew.writeMapleAsciiString("pt=" + chr.getDojoPoints() + ";belt=" + belt + ";tuto=" + (chr.getFinishedDojoTutorial() ? "1" : "0"));
        return mplew.getPacket();
    }

    /**
     * Sends a "levelup" packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br> 0: <Family> ? has reached Lv.
     * ?.<br> - The Reps you have received from ? will be reduced in half. 1:
     * <Family> ? has reached Lv. ?.<br> 2: <Guild> ? has reached Lv. ?.<br>
     *
     * @param type The type
     * @param level
     * @param charname
     * @return The "levelup" packet.
     */
    public static byte[] levelUpMessage(int type, int level, String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NOTIFY_LEVELUP.getValue());
        mplew.write(type);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(charname);

        return mplew.getPacket();
    }

    /**
     * Sends a "married" packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br> 0: <Guild ? is now married.
     * Please congratulate them.<br> 1: <Family ? is now married. Please
     * congratulate them.<br>
     *
     * @param type The type
     * @param charname
     * @return The "married" packet.
     */
    public static byte[] marriageMessage(int type, String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NOTIFY_MARRIAGE.getValue());
        mplew.write(type);
        mplew.writeMapleAsciiString("> " + charname); //To fix the stupid packet lol

        return mplew.getPacket();
    }

    /**
     * Sends a "job advance" packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br> 0: <Guild ? has advanced to
     * a(an) ?.<br> 1: <Family ? has advanced to a(an) ?.<br>
     *
     * @param type The type
     * @param job
     * @param charname
     * @return The "job advance" packet.
     */
    public static byte[] jobMessage(int type, int job, String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NOTIFY_JOB_CHANGE.getValue());
        mplew.write(type);
        mplew.writeInt(job); //Why fking int?
        mplew.writeMapleAsciiString("> " + charname); //To fix the stupid packet lol

        return mplew.getPacket();
    }

    /**
     *
     * @param type - (0:Light&Long 1:Heavy&Short)
     * @param delay - seconds
     * @return
     */
    public static byte[] trembleEffect(int type, int delay) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        return mplew.getPacket();
    }

    public static byte[] getEnergy(String info, int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SESSION_VALUE.getValue());
        mplew.writeMapleAsciiString(info);
        mplew.writeMapleAsciiString(Integer.toString(amount));
        return mplew.getPacket();
    }

    public static byte[] dojoWarpUp() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DOJO_WARP_UP.getValue());
        mplew.write(0);
        mplew.write(6);
        return mplew.getPacket();
    }

    public static byte[] itemExpired(int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(2);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static byte[] MobDamageMobFriendly(MapleMonster mob, int damage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.write(1); // direction ?
        mplew.writeInt(damage);
        int remainingHp = mob.getHp() - damage;
        if (remainingHp <= 0) {
            remainingHp = 0;
            mob.getMap().killMonster(mob.getObjectId());
        }
        mob.setHp(remainingHp);
        mplew.writeInt(remainingHp);
        mplew.writeInt(mob.getMaxHp());
        return mplew.getPacket();
    }

    public static byte[] shopErrorMessage(int error, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0A);
        mplew.write(type);
        mplew.write(error);
        return mplew.getPacket();
    }

    private static void addRingInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(chr.getCrushRings().size());
        for (MapleRing ring : chr.getCrushRings()) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 15);
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
        }
        mplew.writeShort(chr.getFriendshipRings().size());
        for (MapleRing ring : chr.getFriendshipRings()) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 15);
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getItemId());
        }
        mplew.writeShort(chr.getMarriageRing() != null ? 1 : 0);
        int marriageId = 30000;
        if (chr.getMarriageRing() != null) {
            mplew.writeInt(marriageId);
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing().getPartnerChrId());
            mplew.writeShort(3);
            mplew.writeInt(chr.getMarriageRing().getRingId());
            mplew.writeInt(chr.getMarriageRing().getPartnerRingId());
            mplew.writeAsciiString(chr.getName(), 15);
            mplew.writeAsciiString(chr.getMarriageRing().getPartnerName(), 15);
        }

    }

    public static byte[] finishedSort(int inv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.GATHER_ITEM_RESULT.getValue());
        mplew.write(0);
        mplew.write(inv);
        return mplew.getPacket();
    }

    public static byte[] finishedSort2(int inv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.SORT_ITEM_RESULT.getValue());
        mplew.write(0);
        mplew.write(inv);
        return mplew.getPacket();
    }

    public static byte[] bunnyPacket() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeAsciiString("Protect the Moon Bunny!!!");
        return mplew.getPacket();
    }

    public static byte[] hpqMessage(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_EFFECT.getValue()); // not 100% sure
        mplew.write(0);
        mplew.writeInt(5120016);
        mplew.writeAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] showEventInstructions() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] leftKnockBack() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendOpcode.LEFT_KNOCK_BACK.getValue());
        return mplew.getPacket();
    }

    public static byte[] rollSnowball(int type, MapleSnowball.MapleSnowballs ball1, MapleSnowball.MapleSnowballs ball2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.SNOWBALL_STATE.getValue());
        mplew.write(type);
        mplew.writeInt(ball1 == null ? 0 : ball1.getSnowmanHP() / 75);
        mplew.writeInt(ball2 == null ? 0 : ball2.getSnowmanHP() / 75);
        mplew.writeShort(ball1 == null ? 0 : ball1.getPosition());
        mplew.write(0);
        mplew.writeShort(ball2 == null ? 0 : ball2.getPosition());
        mplew.writeZeroBytes(11);

        return mplew.getPacket();
    }

    public static byte[] enterSnowBall() {
        return rollSnowball(0, null, null);
    }

   public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendOpcode.HIT_SNOWBALL.getValue());
        mplew.write(team);
        mplew.writeShort(damage);
        mplew.write(distance);
        mplew.write(delay);

        return mplew.getPacket();
    }


    /**
     * Sends a Snowball Message<br>
     *
     * Possible values for <code>message</code>:<br> 1: ... Team's snowball has
     * passed the stage 1.<br> 2: ... Team's snowball has passed the stage
     * 2.<br> 3: ... Team's snowball has passed the stage 3.<br> 4: ... Team is
     * attacking the snowman, stopping the progress<br> 5: ... Team is moving
     * again<br>
     *
     * @param message
     *
     */
    public static byte[] snowballMessage(int team, int message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SNOWBALL_MESSAGE.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeInt(message);
        return mplew.getPacket();
    }

    public static byte[] coconutScore(int[] teams) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.COCONUT_SCORE.getValue());
        mplew.writeShort(teams[2]);
        mplew.writeShort(teams[1]);
        return mplew.getPacket();
    }

    public static byte[] hitCoconut(boolean spawn, int id, int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.COCONUT_HIT.getValue());
        if (spawn) {
            mplew.writeShort(-1);
            mplew.writeShort(5000);
            mplew.write(0);
        } else {
            mplew.writeShort(id);
            mplew.writeShort(1000);//delay till you can attack again! 
            mplew.write(type); // What action to do for the coconut.
        }
        return mplew.getPacket();
    }

    public static byte[] customPacket(String packet) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(packet));
        return mplew.getPacket();
    }

    public static byte[] customPacket(byte[] packet) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(packet.length);
        mplew.write(packet);
        return mplew.getPacket();
    }

    public static byte[] spawnGuide(boolean spawn) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.SPAWN_GUIDE.getValue());
        if (spawn) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static byte[] talkGuide(String talk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(talk);
        mplew.write(new byte[]{(byte) 0xC8, 0, 0, 0, (byte) 0xA0, (byte) 0x0F, 0, 0});
        return mplew.getPacket();
    }

    public static byte[] guideHint(int hint) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
        mplew.write(1);
        mplew.writeInt(hint);
        mplew.writeInt(7000);
        return mplew.getPacket();
    }

    public static byte[] resetForcedStats() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendOpcode.FORCED_STAT_RESET.getValue());
        return mplew.getPacket();
    }

    public static byte[] showCombo(int count) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.SHOW_COMBO.getValue());
        mplew.writeInt(count);
        return mplew.getPacket();
    }

    public static byte[] earnTitleMessage(String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SCRIPT_PROGRESS_MESSAGE.getValue());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static byte[] startCPQ(MapleCharacter chr, MonsterCarnivalParty enemy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_START.getValue());
        mplew.write(chr.getTeam()); //team
        mplew.writeShort(chr.getCP()); //Obtained CP - Used CP
        mplew.writeShort(chr.getObtainedCP()); //Total Obtained CP
        mplew.writeShort(chr.getCarnivalParty().getAvailableCP()); //Obtained CP - Used CP of the team
        mplew.writeShort(chr.getCarnivalParty().getTotalCP()); //Total Obtained CP of the team
        mplew.writeShort(enemy.getAvailableCP()); //Obtained CP - Used CP of the team
        mplew.writeShort(enemy.getTotalCP()); //Total Obtained CP of the team
        mplew.writeShort(0); //Probably useless nexon shit
        mplew.writeLong(0); //Probably useless nexon shit
        return mplew.getPacket();
    }

    public static byte[] updateCP(int cp, int tcp) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
        mplew.writeShort(cp); //Obtained CP - Used CP
        mplew.writeShort(tcp); //Total Obtained CP
        return mplew.getPacket();
    }

    public static byte[] updatePartyCP(MonsterCarnivalParty party) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
        mplew.write(party.getTeam()); //Team where the points are given to.
        mplew.writeShort(party.getAvailableCP()); //Obtained CP - Used CP
        mplew.writeShort(party.getTotalCP()); //Total Obtained CP
        return mplew.getPacket();
    }

    public static byte[] CPQSummon(int tab, int number, String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(tab); //Tab
        mplew.writeShort(number); //Number of summon inside the tab
        mplew.writeMapleAsciiString(name); //Name of the player that summons
        return mplew.getPacket();
    }

    public static byte[] CPQDied(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(chr.getTeam()); //Team
        mplew.writeMapleAsciiString(chr.getName()); //Name of the player that died
        mplew.write(chr.getAndRemoveCP()); //Lost CP
        return mplew.getPacket();
    }

    /**
     * Sends a CPQ Message<br>
     *
     * Possible values for <code>message</code>:<br> 1: You don't have enough CP
     * to continue.<br> 2: You can no longer summon the Monster.<br> 3: You can
     * no longer summon the being.<br> 4: This being is already summoned.<br> 5:
     * This request has failed due to an unknown error.<br>
     *
     * @param message Displays a message inside Carnival PQ
     *
     */
    public static byte[] CPQMessage(byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_MESSAGE.getValue());
        mplew.write(message); //Message
        return mplew.getPacket();
    }

    public static byte[] leaveCPQ(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_LEAVE.getValue());
        mplew.write(0); //Something
        mplew.write(chr.getTeam()); //Team
        mplew.writeMapleAsciiString(chr.getName()); //Player name
        return mplew.getPacket();
    }

    public static byte[] sheepRanchInfo(byte wolf, byte sheep) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHEEP_RANCH_INFO.getValue());
        mplew.write(wolf);
        mplew.write(sheep);
        return mplew.getPacket();
    }
    //Know what this is? ?? >=)

    public static byte[] sheepRanchClothes(int id, byte clothes) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHEEP_RANCH_CLOTHES.getValue());
        mplew.writeInt(id); //Character id
        mplew.write(clothes); //0 = sheep, 1 = wolf, 2 = Spectator (wolf without wool)
        return mplew.getPacket();
    }

    public static byte[] incubatorResult() {//lol
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.INCUBATOR_RESULT.getValue());
        mplew.skip(6);
        return mplew.getPacket();
    }

    public static byte[] pyramidGauge(int gauge) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.PYRAMID_GAUGE.getValue());
        mplew.writeInt(gauge);
        return mplew.getPacket();
    }
    // f2

    public static byte[] pyramidScore(byte score, int exp) {//Type cannot be higher than 4 (Rank D), otherwise you'll crash
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.PYRAMID_SCORE.getValue());
        mplew.write(score);
        mplew.writeInt(exp);
        return mplew.getPacket();
    }
}
