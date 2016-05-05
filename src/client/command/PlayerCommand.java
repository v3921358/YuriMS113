package client.command;

import client.inventory.IItem;
import client.MapleCharacter;
import client.MapleClient;
import server.TimerManager;
import client.inventory.MapleInventoryType;
import client.MapleStat;
import java.rmi.RemoteException;
import java.sql.Connection;
import client.SkillFactory;
import server.life.MapleLifeFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.maps.MapleMap;
import net.server.Server;
import net.server.channel.Channel;
import net.server.world.World;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleShopFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.packets.MaplePacketCreator;
import tools.DatabaseConnection;
import tools.StringUtil;
import tools.packets.CWvsContext;

public class PlayerCommand {

    private static ResultSet ranking(boolean gm) {
        try {
            Connection con = (Connection) DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = (PreparedStatement) con.prepareStatement("SELECT reborns, level, name, job FROM characters WHERE gm < 3 ORDER BY reborns desc LIMIT 10");
            } else {
                ps = (PreparedStatement) con.prepareStatement("SELECT name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    static void execute(MapleClient c, String[] splitted, char heading) throws SQLException {
        Channel cserv = c.getChannelServer();
        MapleCharacter player = c.getPlayer();
        if (splitted[0].equals("expfix") || splitted[0].equalsIgnoreCase("修復")) {
            player.setExp(0);
            player.updateSingleStat(MapleStat.EXP, player.getExp());
            c.getPlayer().dropMessage(5, "經驗修復完成");

        } else if (splitted[0].equalsIgnoreCase("ea") || splitted[0].equalsIgnoreCase("解卡")) {
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(CWvsContext.enableActions());
            c.getPlayer().dropMessage(5, "解卡完成");

        } else if (splitted[0].equalsIgnoreCase("save") || splitted[0].equalsIgnoreCase("存檔")) {
            c.getPlayer().saveToDB();
            c.getPlayer().dropMessage("保存成功");

        } else if (splitted[0].equalsIgnoreCase("fm") || splitted[0].equalsIgnoreCase("自由")) {
            if (player.haveItem(2030000)) { //是否有回家卷軸
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, 2030000, 1, true, false);
                c.getPlayer().saveLocation("FREE_MARKET");
                c.getPlayer().changeMap(910000000);
                c.getPlayer().dropMessage(5, "回到自由了");
            } else {
                player.dropMessage("沒有回家卷軸,所以不能使用回自由指令!.");
            }

        } else if (splitted[0].equalsIgnoreCase("getPos")) {
            c.getPlayer().dropMessage(5, String.format("x is %d, y is %d", c.getPlayer().getPosition().x, c.getPlayer().getPosition().y));

        } else if (splitted[0].equalsIgnoreCase("mob") || splitted[0].equalsIgnoreCase("怪物")) {
            MapleMonster mob = null;
            for (final MapleMapObject monstermo : c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 100000, Arrays.asList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (mob.isAlive()) {
                    c.getPlayer().dropMessage(6, "怪物 " + mob.toString());
                    break;
                }
            }
            if (mob == null) {
                c.getPlayer().dropMessage(6, "找不到地圖上的怪物");
            }

        } else if (splitted[0].equalsIgnoreCase("mapid") || splitted[0].equalsIgnoreCase("地圖編號")) {
            c.getPlayer().dropMessage(6, "地圖編號 " + Integer.toString(c.getPlayer().getMapId()));
        } else if (splitted[0].equalsIgnoreCase("allnpc")) {
            for (final MapleMapObject npcobject : c.getPlayer().getMap().getMapObjects()) {
                MapleNPC npc = (MapleNPC) npcobject;
                System.err.println(npc.getId());

            }
        } else if (splitted[0].equalsIgnoreCase("gms")) {
            for (Channel w : Server.getInstance().getAllChannels()) {
                for (MapleCharacter chr : w.getPlayerStorage().getAllCharacters()) {
                    if (chr.isGM()) {
                        c.getPlayer().dropMessage(6, "己上線的GM " + chr.getName());
                    }
                }
            }
        }else if (splitted[0].equalsIgnoreCase("jobid")) {
            c.getPlayer().dropMessage(6, String.format("JobID:%d", c.getPlayer().getJob().getId()));
        } else if (splitted[0].equalsIgnoreCase("幫助") || splitted[0].equalsIgnoreCase("help")) {
            c.getPlayer().dropMessage(5, "SyncMs 玩家指令");
            c.getPlayer().dropMessage(5, "@解卡/@ea <解除異常>");
            c.getPlayer().dropMessage(5, "@修復/@expfix <修復經驗假死>");
            c.getPlayer().dropMessage(5, "@存檔/@save <保存資料>");
            c.getPlayer().dropMessage(5, "@自由/@fm <回自由-需要1張回家卷軸>");
            c.getPlayer().dropMessage(5, "@怪物/@mob <查看當前地圖怪物狀態>");
        } else {
            player.dropMessage("指令: " + heading + splitted[0] + " 不存在. 可以使用 @幫助/@help 來查看所有玩家指令.");
        }
    }

    private static void compareTime(StringBuilder sb, long timeDiff) {
        double secondsAway = timeDiff / 1000;
        double minutesAway = 0;
        double hoursAway = 0;

        while (secondsAway > 60) {
            minutesAway++;
            secondsAway -= 60;
        }
        while (minutesAway > 60) {
            hoursAway++;
            minutesAway -= 60;
        }
        boolean hours = false;
        boolean minutes = false;
        if (hoursAway > 0) {
            sb.append(" ");
            sb.append((int) hoursAway);
            sb.append(" hours");
            hours = true;
        }
        if (minutesAway > 0) {
            if (hours) {
                sb.append(" -");
            }
            sb.append(" ");
            sb.append((int) minutesAway);
            sb.append(" minutes");
            minutes = true;
        }
        if (secondsAway > 0) {
            if (minutes) {
                sb.append(" and");
            }
            sb.append(" ");
            sb.append((int) secondsAway);
            sb.append(" seconds.");
        }
    }
}
