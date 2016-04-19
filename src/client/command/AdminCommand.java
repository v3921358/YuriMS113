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
package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import net.server.channel.Channel;
import server.MaplePortal;
import server.life.MapleLifeFactory;
import server.maps.MapleMap;
import java.awt.Point;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.server.PlayerStorage;
import net.server.Server;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.MapleTrade;
import tools.packets.MaplePacketCreator;
import tools.StringUtil;
import net.server.world.WorldLocation;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import scripting.portal.PortalScriptManager;
import server.life.MapleMonsterInformationProvider;
import scripting.reactor.ReactorScriptManager;
import java.sql.PreparedStatement;
import tools.DatabaseConnection;

public class AdminCommand {

    static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        Channel cserv = c.getChannelServer();
        try {

            if (splitted[0].equalsIgnoreCase("speakall")) {
                String text = StringUtil.joinStringFrom(splitted, 1);
                for (MapleCharacter mch : player.getMap().getCharacters()) {
                    mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
                }
            } else if (splitted[0].equalsIgnoreCase("speak")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    String text = StringUtil.joinStringFrom(splitted, 2);
                    victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), text, false, 0));
                } else {
                    player.dropMessage(5, "找不到玩家");
                }
            } else if (splitted[0].equalsIgnoreCase("map")) {
                try {
                    MapleMap target = cserv.getMapFactory().getMap(Integer.parseInt(splitted[1]));
                    MaplePortal targetPortal = null;
                    if (splitted.length > 2) {
                        try {
                            targetPortal = target.getPortal(Integer.parseInt(splitted[2]));
                        } catch (IndexOutOfBoundsException e) {
                            player.dropMessage(5, "無效的光點位置！");
                        } catch (NumberFormatException a) {
                        }
                    }
                    if (targetPortal == null) {
                        targetPortal = target.getPortal(0);
                    }
                    player.changeMap(target);
                } catch (Exception e) {
                    player.dropMessage(5, "找不到此地圖代碼！");
                }
            } else if (splitted[0].equalsIgnoreCase("sp")) {
                player.setRemainingSp(Integer.parseInt(splitted[1]));
                player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
            } else if (splitted[0].equalsIgnoreCase("ap")) {
                player.setRemainingAp(Integer.parseInt(splitted[1]));
                player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
            } else if (splitted[0].equalsIgnoreCase("job")) {
                player.changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
            } else if (splitted[0].equalsIgnoreCase("level")) {
                player.setLevel(Integer.parseInt(splitted[1]));
                player.gainExp(-player.getExp(), false, false);
                player.updateSingleStat(MapleStat.LEVEL, player.getLevel());
            } else if (splitted[0].equalsIgnoreCase("mesos")) {
                player.gainMeso(Integer.parseInt(splitted[1]), true);
            } else if (splitted[0].equalsIgnoreCase("warp")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                player.changeMap(victim.getMapId(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
                player.dropMessage(5, "跟蹤到了！");
            } else if (splitted[0].equalsIgnoreCase("warphere")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));
                player.dropMessage(5, "已經把玩家傳送到身旁了！");
            } else if (splitted[0].equalsIgnoreCase("warpallhere")) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (mch.getMapId() != player.getMapId()) {
                        mch.changeMap(player.getMap(), player.getPosition());
                        player.dropMessage(5, "已經把頻道上所有玩家傳送到身旁了！");
                    } else {
                        player.dropMessage(5, "找不到玩家傳送到身旁！");
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("ban")) {
                try {
                    try (PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 1 WHERE id = " + MapleCharacter.getIdByName(splitted[1]))) {
                        p.executeUpdate();
                    }
                } catch (Exception e) {
                    player.message("封鎖失敗 " + splitted[1]);
                    return true;
                }
                player.message("封鎖成功 " + splitted[1]);
            } else if (splitted[0].equalsIgnoreCase("unban")) {
                try {
                    try (PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 0 WHERE id = " + MapleCharacter.getIdByName(splitted[1]))) {
                        p.executeUpdate();
                    }
                } catch (Exception e) {
                    player.message("解鎖失敗 " + splitted[1]);
                    return true;
                }
                player.message("解鎖成功 " + splitted[1]);
            } else if (splitted[0].equalsIgnoreCase("npc")) {
                int npcId = Integer.parseInt(splitted[1], 10);
                MapleNPC npc = MapleLifeFactory.getNPC(npcId);
                if (npc != null && !npc.getName().equalsIgnoreCase("MISSINGNO")) {
                    npc.setPosition(player.getPosition());
                    npc.setCy(player.getPosition().y);
                    npc.setRx0(player.getPosition().x + 50);
                    npc.setRx1(player.getPosition().x - 50);
                    npc.setFh(player.getMap().getFootholds().findBelow(player.getPosition()).getId());
                    player.getMap().addMapObject(npc);
                    player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                } else {
                    player.dropMessage(5, "npc 不存在");
                }
            } else if (splitted[0].equalsIgnoreCase("saveall")) {
                int p = 0;
                List<Channel> channels = c.getWorldServer().getChannels();
                for (Channel channel : channels) {
                    for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                        p++;
                        c.getChannelServer().saveAll();
                        player.dropMessage(5, "[全服存檔] " + p + "個玩家數據保存到數據中.");
                    }
                }
            } else if (splitted[0].equals("dc")) {
                int level = 0;
                MapleCharacter victim;
                if (splitted[1].charAt(0) == '-') {
                    level = StringUtil.countCharacters(splitted[1], 'f');
                    victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
                } else {
                    victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                }
                victim.getClient().getSession().close();
                if (level >= 1) {
                    victim.getClient().disconnect(true, false);
                }
                if (level >= 2) {
                    victim.saveToDB();
                    cserv.removePlayer(victim);
                }
                victim.getClient().disconnect(false, false);
            } else if (splitted[0].equalsIgnoreCase("online")) {
                int total = 0;
                int curConnected = c.getChannelServer().getConnectedClients();
                List<Channel> channels = c.getWorldServer().getChannels();
                total += curConnected;
                for (Channel channel : channels) {
                    player.dropMessage(5, "頻道 " + String.valueOf(channel.getId()));
                    PlayerStorage all = channel.getPlayerStorage();
                    for (MapleCharacter chr : all.getAllCharacters()) {
                        if (chr != null && c.getPlayer().getGMLevel() >= chr.getGMLevel()) {
                            if (chr.getMap() != null) {
                                player.dropMessage(5, "名稱: " + chr.getName()
                                        + " 職業: " + chr.getJob()
                                        + " 等級: " + String.valueOf(chr.getLevel())
                                        + " 地圖: " + chr.getMapId() + " - " + chr.getMap().getMapName().toString()
                                        + " 財產: " + String.valueOf(chr.getMeso()));
                                c.getPlayer().dropMessage(6, new StringBuilder().append("當前伺服器總計線上人數: ").append(total).toString());
                            }
                        }
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("item") || splitted[0].equalsIgnoreCase("drop")) {
                int itemId = Integer.parseInt(splitted[1]);
                short quantity = 1;
                try {
                    quantity = Short.parseShort(splitted[2]);
                } catch (Exception e) {
                }
                if (splitted[0].equalsIgnoreCase("item")) {
                    int petid = -1;
                    if (ItemConstants.isPet(itemId)) {
                        petid = MaplePet.createPet(itemId);
                    }
                    MapleInventoryManipulator.addById(c, itemId, quantity, player.getName(), petid, -1);
                } else {
                    Item toDrop;
                    if (MapleItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                        toDrop = MapleItemInformationProvider.getInstance().getEquipById(itemId);
                    } else {
                        toDrop = new Item(itemId, (byte) 0, quantity);
                    }
                    player.getMap().spawnItemDrop(player, player, toDrop, player.getPosition(), true, true);
                }
            } else if (splitted[0].equalsIgnoreCase("killall")) {
                List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
                MapleMap map = player.getMap();
                for (MapleMapObject monstermo : monsters) {
                    MapleMonster monster = (MapleMonster) monstermo;
                    map.killMonster(monster, player, true);
                    monster.giveExpToCharacter(player, monster.getExp() * player.getExpRate(), true, 1);
                }
                player.dropMessage("殺死了 " + monsters.size() + " 隻怪物.");
            } else if (splitted[0].equalsIgnoreCase("monsterdebug")) {
                List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
                for (MapleMapObject monstermo : monsters) {
                    MapleMonster monster = (MapleMonster) monstermo;
                    player.message("怪物 ID: " + monster.getId());
                }
            } else if (splitted[0].equalsIgnoreCase("shutdown")) {
                if (splitted.length > 2 && Integer.parseInt(splitted[1]) >= 0) {
                    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    executor.schedule(Server.getInstance().shutdownThread(false), Integer.parseInt(splitted[1]) * 60, TimeUnit.SECONDS);
                }

            } else if (splitted[0].equals("reload")) {
                PortalScriptManager.getInstance().clearScripts(); // 傳點腳本
                MapleMonsterInformationProvider.getInstance().clearDrops(); // 怪物掉落
                ReactorScriptManager.getInstance().clearDrops(); // 反應堆腳本
                MapleShopFactory.getInstance().reloadShops(); // 商店腳本
                for (Channel instance : Channel.getAllInstances()) {
                    instance.reloadEvents(); // 事件腳本
                }

            } else if (splitted[0].equals("pos")) {
                Point pos = c.getPlayer().getPosition();
                c.getPlayer().dropMessage(6, "X: " + pos.x + " | Y: " + pos.y + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getFH() + "| CY:" + pos.y);

            } else if (splitted[0].equalsIgnoreCase("spawn")) {
                final int mid = Integer.parseInt(splitted[1]);
                int num = 1;

                if (splitted.length > 2) {
                    num = Integer.parseInt(splitted[2]);
                }
                num = (num > 500 ? 500 : num);

                MapleMonster onemob;

                onemob = MapleLifeFactory.getMonster(mid);

                if (onemob != null) {
                    for (int i = 0; i < num; i++) {
                        MapleMonster mob = MapleLifeFactory.getMonster(mid);
                        player.getMap().spawnMonsterOnGroundBelow(mob, player.getPosition());
                    }
                } else {
                    player.dropMessage(5, "怪物不存在");
                }

            } else if (splitted[0].equalsIgnoreCase("notice")) {
                String text = StringUtil.joinStringFrom(splitted, 1);
                c.getWorldServer().broadcastPacket(MaplePacketCreator.broadcastMsg(0, text));
            } else if (splitted[0].equalsIgnoreCase("管理員幫助") || splitted[0].equalsIgnoreCase("adminhelp")) {
                player.dropMessage(5, "SyncMs GM 指令");
                player.dropMessage(5, "!online - 上線人數");
                player.dropMessage(5, "!notice 訊息 - 公告");
                player.dropMessage(5, "!speakall <訊息> - 讓每個人都說同樣的話");
                player.dropMessage(5, "!speak <玩家名字> <訊息> - 單獨讓人說同樣的話 ");
                player.dropMessage(5, "!mesos <數量> - 爽得錢");
                player.dropMessage(5, "!item <物品代碼> - 爽製造東西");
                player.dropMessage(5, "!drop <物品代碼> - 爽製造東西到地板");
                player.dropMessage(5, "!job <職業代碼> - 轉職");
                player.dropMessage(5, "!map <地圖代碼> - 傳送到那地圖");
                player.dropMessage(5, "!level <等級> - 爽升等");
                player.dropMessage(5, "!warp <玩家名字> - 飛到身旁");
                player.dropMessage(5, "!warphere - 把玩家傳到身旁");
                player.dropMessage(5, "!warpallhere - 把頻道所有玩家傳到身旁");
                player.dropMessage(5, "!killall - 殺死地圖所有怪物");
                player.dropMessage(5, "!monsterdebug - 怪物除錯");
                player.dropMessage(5, "!sp <數量> - 獲得技能點");
                player.dropMessage(5, "!ap <數量> - 獲得能力值");
                player.dropMessage(5, "!spawn <怪物代碼> - 生怪");
                player.dropMessage(5, "!shotdown <時間> - 關機");
                player.dropMessage(5, "!reload - 重新讀取所有腳本");
                player.dropMessage(5, "!pos - 目前的座標軸");
                player.dropMessage(5, "!saveall - 全服存檔");
                player.dropMessage(5, "!ban <名字> - 封鎖");
                player.dropMessage(5, "!unban <名字> - 解鎖");
                player.dropMessage(5, "!dc <名字> - 斷線");

            } else if (player.gmLevel() == 5) {
                player.message("管理員指令 " + heading + splitted[0] + " 不存在. 可以使用 !管理員幫助/!adminHelp 來查看所有管理員指令.");
                return false;
            }
        } catch (Exception e) {
            if (player.gmLevel() == 5) {
                player.message("管理員指令 " + heading + splitted[0] + "使用錯誤，可以使用 !管理員幫助/!adminHelp 來查看所有管理員指令.");
                return false;
            }
        }
        return true;
    }
}
