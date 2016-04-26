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
package net.server.channel.handlers;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ItemConstants;
import constants.skills.Assassin;
import constants.skills.Bandit;
import constants.skills.Bishop;
import constants.skills.Bowmaster;
import constants.skills.Brawler;
import constants.skills.ChiefBandit;
import constants.skills.Cleric;
import constants.skills.Corsair;
import constants.skills.Crossbowman;
import constants.skills.DawnWarrior;
import constants.skills.FPArchMage;
import constants.skills.Fighter;
import constants.skills.Gunslinger;
import constants.skills.Hunter;
import constants.skills.ILArchMage;
import constants.skills.Marauder;
import constants.skills.Marksman;
import constants.skills.NightWalker;
import constants.skills.Outlaw;
import constants.skills.Page;
import constants.skills.Paladin;
import constants.skills.Rogue;
import constants.skills.Shadower;
import constants.skills.Spearman;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.partyquest.Pyramid;
import tools.FilePrinter;
import tools.packets.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.LittleEndianAccessor;
import tools.packets.CWvsContext;

public abstract class AbstractDealDamageHandler extends AbstractMaplePacketHandler {

    public static class AttackInfo {

        public int numAttacked,
                numDamage,
                numAttackedAndDamage,
                skill,
                skilllevel,
                stance,
                direction,
                rangedirection,
                charge,
                display,
                speed = 4,
                lastAttackTickCount = 0;
        public byte
                csstar,
                slot,
                AOE,
                unk;

        public Map<Integer, List<Integer>> allDamage;
        public boolean isHH = false;
        public Point position;

        public MapleStatEffect getAttackEffect(MapleCharacter chr, Skill theSkill) {

            Skill mySkill = theSkill;

            if (mySkill == null) {
                mySkill = SkillFactory.getSkill(GameConstants.getHiddenSkill(skill));
            }

            int skillLevel = chr.getSkillLevel(mySkill);

            if (mySkill.getId() % 10000000 == 1020) {
                if (chr.getPartyQuest() instanceof Pyramid) {
                    if (((Pyramid) chr.getPartyQuest()).useSkill()) {
                        skillLevel = 1;
                    }
                }
            }
            if (skillLevel == 0) {
                return null;
            }

            if (display > 80) { //Hmm
                if (!mySkill.getAction()) {
                    AutobanFactory.FAST_ATTACK.autoban(chr, "WZ 更改; adding action to a skill: " + display);
                    return null;
                }
            }
            return mySkill.getEffect(skillLevel);
        }
    }

    protected synchronized void applyAttack(AttackInfo attack, final MapleCharacter player, int attackCount) {

        Skill theSkill = null;
        MapleStatEffect attackEffect = null;

        try {
            if (player.isBanned()) {
                return;
            }

            if (attack.skill != 0) {
                theSkill = SkillFactory.getSkill(attack.skill);
                attackEffect = attack.getAttackEffect(player, theSkill);
                if (attackEffect == null) {
                    player.getClient().announce(CWvsContext.enableActions());
                    return;
                }

                if (player.getMp() < attackEffect.getMpCon()) {
                    AutobanFactory.MPCON.addPoint(player.getAutobanManager(), "技能: " + attack.skill + "; 角色MP: " + player.getMp() + "; MP需消耗: " + attackEffect.getMpCon());
                }

                if (attack.skill != Cleric.HEAL) {
                    if (player.isAlive()) {
                        attackEffect.applyTo(player);
                    } else {
                        player.getClient().announce(CWvsContext.enableActions());
                    }
                }
                int mobCount = attackEffect.getMobCount();
                if (attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Page.FINAL_ATTACK_BW || attack.skill == Page.FINAL_ATTACK_SWORD || attack.skill == Fighter.FINAL_ATTACK_SWORD
                        || attack.skill == Fighter.FINAL_ATTACK_AXE || attack.skill == Spearman.FINAL_ATTACK_SPEAR || attack.skill == Spearman.FINAL_ATTACK_POLEARM || attack.skill == WindArcher.FINAL_ATTACK
                        || attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Hunter.FINAL_ATTACK || attack.skill == Crossbowman.FINAL_ATTACK) {
                    mobCount = 15;//:(
                }
                if (attack.numAttacked > mobCount) {
                    AutobanFactory.MOB_COUNT.autoban(player, "技能: " + attack.skill + "; 攻擊怪物數量: " + attack.numAttacked + " 正常最大值: " + attackEffect.getMobCount());
                    return;
                }
            }
            if (!player.isAlive()) {
                return;
            }

            //WTF IS THIS F3,1
        /*if (attackCount != attack.numDamage && attack.skill != ChiefBandit.MESO_EXPLOSION && attack.skill != NightWalker.VAMPIRE && attack.skill != WindArcher.WIND_SHOT && attack.skill != Aran.COMBO_SMASH && attack.skill != Aran.COMBO_PENRIL && attack.skill != Aran.COMBO_TEMPEST && attack.skill != NightLord.NINJA_AMBUSH && attack.skill != Shadower.NINJA_AMBUSH) {
             return;
             }*/
            int totDamage = 0;
            final MapleMap map = player.getMap();

            if (attack.skill == ChiefBandit.MESO_EXPLOSION) {
                int delay = 0;
                for (Integer oned : attack.allDamage.keySet()) {
                    MapleMapObject mapobject = map.getMapObject(oned);
                    if (mapobject != null && mapobject.getType() == MapleMapObjectType.ITEM) {
                        final MapleMapItem mapitem = (MapleMapItem) mapobject;
                        if (mapitem.getMeso() > 9) {
                            synchronized (mapitem) {
                                if (mapitem.isPickedUp()) {
                                    return;
                                }
                                TimerManager.getInstance().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        map.removeMapObject(mapitem);
                                        map.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 4, 0), mapitem.getPosition());
                                        mapitem.setPickedUp(true);
                                    }
                                }, delay);
                                delay += 100;
                            }
                        } else if (mapitem.getMeso() == 0) {
                            return;
                        }
                    } else if (mapobject != null && mapobject.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                }
            }
            for (Integer oned : attack.allDamage.keySet()) {
                final MapleMonster monster = map.getMonsterByOid(oned);
                if (monster != null) {
                    int totDamageToOneMonster = 0;
                    List<Integer> onedList = attack.allDamage.get(oned);
                    for (Integer eachd : onedList) {
                        totDamageToOneMonster += eachd;
                    }
                    totDamage += totDamageToOneMonster;
                    player.checkMonsterAggro(monster);
                    if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null && (attack.skill == 0 || attack.skill == Rogue.DOUBLE_STAB || attack.skill == Bandit.SAVAGE_BLOW || attack.skill == ChiefBandit.ASSAULTER || attack.skill == ChiefBandit.BAND_OF_THIEVES || attack.skill == Shadower.ASSASSINATE || attack.skill == Shadower.TAUNT || attack.skill == Shadower.BOOMERANG_STEP)) {
                        Skill pickpocket = SkillFactory.getSkill(ChiefBandit.PICKPOCKET);
                        int delay = 0;
                        final int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET);
                        for (final Integer eachd : onedList) {
                            if (pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()) {
                                TimerManager.getInstance().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        player.getMap().spawnMesoDrop(Math.min((int) Math.max(((double) eachd / (double) 20000) * (double) maxmeso, (double) 1), maxmeso), new Point((int) (monster.getPosition().getX() + Randomizer.nextInt(100) - 50), (int) (monster.getPosition().getY())), monster, player, true, (byte) 0);
                                    }
                                }, delay);
                                delay += 100;
                            }
                        }
                    } else if (attack.skill == Marksman.SNIPE) {
                        totDamageToOneMonster = 195000 + Randomizer.nextInt(5000);
                    } else if (attack.skill == Marauder.ENERGY_DRAIN || attack.skill == ThunderBreaker.ENERGY_DRAIN || attack.skill == NightWalker.VAMPIRE || attack.skill == Assassin.DRAIN) {
                        player.addHP(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skill))).getX() / 100.0), player.getMaxHp() / 2)));
                    } else if (attack.skill == Bandit.STEAL) {
                        Skill steal = SkillFactory.getSkill(Bandit.STEAL);
                        if (Math.random() < 0.3 && steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()) { //Else it drops too many cool stuff :(
                            List<MonsterDropEntry> toSteals = MapleMonsterInformationProvider.getInstance().retrieveDrop(monster.getId());
                            Collections.shuffle(toSteals);
                            int toSteal = toSteals.get(rand(0, (toSteals.size() - 1))).itemId;
                            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                            Item item;
                            if (ItemConstants.getInventoryType(toSteal).equals(MapleInventoryType.EQUIP)) {
                                item = ii.randomizeStats((Equip) ii.getEquipById(toSteal));
                            } else {
                                item = new Item(toSteal, (byte) 0, (short) 1, -1);
                            }
                            player.getMap().spawnItemDrop(monster, player, item, monster.getPosition(), false, false);
                            monster.addStolen(toSteal);
                        }
                    } else if (attack.skill == FPArchMage.FIRE_DEMON) {
                        monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(FPArchMage.FIRE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(FPArchMage.FIRE_DEMON))).getDuration() * 1000);
                    } else if (attack.skill == ILArchMage.ICE_DEMON) {
                        monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(ILArchMage.ICE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(ILArchMage.ICE_DEMON))).getDuration() * 1000);
                    } else if (attack.skill == Outlaw.HOMING_BEACON || attack.skill == Corsair.BULLSEYE) {
                        player.setMarkedMonster(monster.getObjectId());
                        player.announce(MaplePacketCreator.giveBuff(1, attack.skill, Collections.singletonList(new Pair<>(MapleBuffStat.HOMING_BEACON, monster.getObjectId()))));
                    }
                    if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
                        Skill hamstring = SkillFactory.getSkill(Bowmaster.HAMSTRING);
                        if (hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()) {
                            MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
                        }
                    }
                    if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
                        Skill blind = SkillFactory.getSkill(Marksman.BLIND);
                        if (blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()) {
                            MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
                        }
                    }
                    final int id = player.getJob().getId();
                    if (id == 121 || id == 122) {
                        for (int charge = 1211005; charge < 1211007; charge++) {
                            Skill chargeSkill = SkillFactory.getSkill(charge);
                            if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)) {
                                final ElementalEffectiveness iceEffectiveness = monster.getEffectiveness(Element.ICE);
                                if (totDamageToOneMonster > 0 && iceEffectiveness == ElementalEffectiveness.NORMAL || iceEffectiveness == ElementalEffectiveness.WEAK) {
                                    monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.FREEZE, 1), chargeSkill, null, false), false, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 2000);
                                }
                                break;
                            }
                        }
                    } else if (player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null || player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                        Skill skill;
                        if (player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null) {
                            skill = SkillFactory.getSkill(21101003);
                            final MapleStatEffect eff = skill.getEffect(player.getSkillLevel(skill));

                            if (eff.makeChanceResult()) {
                                monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.NEUTRALISE, 1), skill, null, false), false, eff.getX() * 1000, false);
                            }
                        }
                        if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                            skill = SkillFactory.getSkill(21100005);
                            player.setHp(player.getHp() + ((totDamage * skill.getEffect(player.getSkillLevel(skill)).getX()) / 100), true);
                            player.updateSingleStat(MapleStat.HP, player.getHp());
                        }
                    } else if (id == 412 || id == 422 || id == 1411) {
                        Skill type = SkillFactory.getSkill(player.getJob().getId() == 412 ? 4120005 : (player.getJob().getId() == 1411 ? 14110004 : 4220005));
                        if (player.getSkillLevel(type) > 0) {
                            MapleStatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
                            for (int i = 0; i < attackCount; i++) {
                                if (venomEffect.makeChanceResult()) {
                                    if (monster.getVenomMulti() < 3) {
                                        monster.setVenomMulti((monster.getVenomMulti() + 1));
                                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, null, false);
                                        monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                                    }
                                }
                            }
                        }
                    }
                    double d = player.getPosition().distanceSq(monster.getPosition());
                    if (d > 700000.0) { // 600^2, 550 is approximatly the range of ultis
                        int x = 1;
                        x = 4;
                    }
                    if ( attackEffect != null && attack.skill != 0 ) {
                        if (attackEffect.getFixDamage() != -1) {
                            if (totDamageToOneMonster != attackEffect.getFixDamage() && totDamageToOneMonster != 0) {
                                AutobanFactory.FIX_DAMAGE.autoban(player, String.valueOf(totDamageToOneMonster) + " damage");
                            }
                        }
                    }
                    if (totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0) {
                        if (attackEffect.makeChanceResult()) {
                            monster.applyStatus(player, new MonsterStatusEffect(attackEffect.getMonsterStati(), theSkill, null, false), attackEffect.isPoison(), attackEffect.getDuration());
                        }
                    }
                    if (attack.isHH && !monster.isBoss()) {
                        map.damageMonster(player, monster, monster.getHp() - 1);
                    } else if (attack.isHH) {
                        int HHDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Paladin.HEAVENS_HAMMER).getEffect(player.getSkillLevel(SkillFactory.getSkill(Paladin.HEAVENS_HAMMER))).getDamage() / 100));
                        map.damageMonster(player, monster, (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8)));
                    } else {
                        map.damageMonster(player, monster, totDamageToOneMonster);
                    }
                }
            }
        } catch (Exception e) {
             FilePrinter.printError("AbstractDealDamageHandler.txt", e);
        }
    }

    protected AttackInfo parseDamage(LittleEndianAccessor lea, MapleCharacter chr, boolean ranged) {

        AttackInfo ret = new AttackInfo();
        lea.skip(1);
        lea.skip(8);
        ret.numAttackedAndDamage = lea.readByte();
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.allDamage = new HashMap<>();
        lea.skip(8);
        ret.skill = lea.readInt();
        lea.skip(12); // ORDER [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82
        if (ret.skill > 0) {
            ret.skilllevel = chr.getSkillLevel(ret.skill);
        }
        if (ret.skill == FPArchMage.BIG_BANG
                || ret.skill == ILArchMage.BIG_BANG
                || ret.skill == Bishop.BIG_BANG
                || ret.skill == Gunslinger.GRENADE
                || ret.skill == Brawler.CORKSCREW_BLOW
                || ret.skill == ThunderBreaker.CORKSCREW_BLOW
                || ret.skill == NightWalker.POISON_BOMB) {
            ret.charge = lea.readInt();
        } else {
            ret.charge = 0;
        }
        if (ret.skill == Paladin.HEAVENS_HAMMER) {
            ret.isHH = true;
        }
        lea.skip(1);
        ret.display = lea.readByte();
        ret.direction = lea.readByte();
        ret.stance = lea.readByte();
        ret.speed = lea.readByte();
        ret.lastAttackTickCount = lea.readInt(); // Ticks
        if (ret.skill == ChiefBandit.MESO_EXPLOSION) {
            if (ret.numAttackedAndDamage == 0) {
                lea.skip(4);
                int bullets = lea.readByte();
                for (int j = 0; j < bullets; j++) {
                    int mesoid = lea.readInt();
                    lea.skip(1);
                    ret.allDamage.put(mesoid, null);
                }
                return ret;
            }
            for (int i = 0; i < ret.numAttacked + 1; i++) {
                int oid = lea.readInt();
                if (i < ret.numAttacked) {
                    lea.skip(12);
                    int bullets = lea.readByte();
                    List<Integer> allDamageNumbers = new ArrayList<>();
                    for (int j = 0; j < bullets; j++) {
                        int damage = lea.readInt();
                        allDamageNumbers.add(damage);
                    }
                    ret.allDamage.put(oid, allDamageNumbers);
                    lea.skip(4);
                } else {
                    lea.skip(4);
                    int bullets = lea.readByte();
                    for (int j = 0; j < bullets; j++) {
                        int mesoid = lea.readInt();
                        lea.skip(1);
                        ret.allDamage.put(mesoid, null);
                    }
                }
            }
            return ret;
        }

        /*
         26 00 
         01 
         FF FF FF FF FF FF FF FF 
         11 
         FF FF FF FF FF FF FF FF 
         AC CA 2D 00 
         48 98 E7 95 2F EE 12 5F 58 75 75 77 
         00 - skip
         16 - displ
         80 - ani
         03 - wap
         06 - speed
         6F 27 46 04 
         04 00 
         00 00 
         1E 
         72 00 00 00 06 81 02 05 E4 00 4B 08 E4 00 4B 08 38 02 F6 08 00 00 59 EA 02 4A 4F 01 41 08 31 01 25 08

         */
        /*
        
         * 雙子星 :
         26 00 header
         02 skip
         FF FF FF FF FF FF FF FF skip
         12 // att dmg
         FF FF FF FF FF FF FF FF  skip
         2B 4F 4C 00 
         23 3C E2 19 47 DB B4 1A 42 B1 91 90 
         00 skip
         5B displ
         80 ani
         09 wpaC
         05 speed
         D9 62 26 00 
        
         0E 00 
         00 00
         3F 
        
         64 00 00 00 
         06 00 02 01 12 FF 87 00 0F FF 87 00 EA 00 
         15 00 00 00 
         15 00 00 00 
        
         47 85 A9 A1 
         B1 FF 87 00 
        
        
         72 FF 6B 00
         */
        if (ranged) {

            ret.slot = (byte) lea.readShort();
            ret.csstar = (byte) lea.readShort();
            ret.AOE = lea.readByte(); // is AOE or not, TT/ Avenger = 41, Showdown = 0
            if (ret.skill == Bowmaster.HURRICANE || ret.skill == Marksman.PIERCING_ARROW || ret.skill == Corsair.RAPID_FIRE || ret.skill == WindArcher.HURRICANE) {
                lea.skip(4);
            }
        }

        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(14);
            List<Integer> allDamageNumbers = new ArrayList<>();
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = lea.readInt();
                if (ret.skill == Marksman.SNIPE) {
                    damage += 0x80000000; //Critical
                }
                allDamageNumbers.add(damage);
            }
            if (ret.skill != 5221004) {
                lea.skip(4);
            }
            ret.allDamage.put(oid, allDamageNumbers);
        }

        if (ranged) {
            lea.skip(4);
        }
        ret.position = lea.readPos();

        return ret;
    }

    private static int rand(int l, int u) {
        return (int) ((Math.random() * (u - l + 1)) + l);
    }
}
