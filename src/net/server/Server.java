/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2004 Patrick Huy <patrick.huy@frz.cc>
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
package net.server;

import client.MapleCharacter;
import client.SkillFactory;
import constants.ServerConstants;
import gm.GMPacketCreator;
import gm.server.GMServer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.MapleServerHandler;
import net.mina.MapleCodecFactory;
import net.server.channel.Channel;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.World;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import scripting.portal.PortalScriptManager;
import scripting.reactor.ReactorScriptManager;
import server.CashItemFactory;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.TimerManager;
import server.life.MapleMonsterInformationProvider;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.packets.MaplePacketCreator;
import tools.Pair;

public class Server implements Runnable {

    private IoAcceptor acceptor;
    private List<Map<Integer, String>> channels = new LinkedList<>();
    private List<World> worlds = new ArrayList<>();
    private Properties subnetInfo = new Properties();
    private static Server instance = null;
    private List<Pair<Integer, String>> worldRecommendedList = new LinkedList<>();
    private Map<Integer, MapleGuild> guilds = new LinkedHashMap<>();
    private PlayerBuffStorage buffStorage = new PlayerBuffStorage();
    private Map<Integer, MapleAlliance> alliances = new LinkedHashMap<>();
    private boolean online = false;
    private static boolean ranking = true;
    public static int channelMaxPlayer = 100;
    private static int expRate = 1;
    private static int mesoRate = 1;
    private static int dropRate = 1;
    private static int bossRate = 1;
    private static int qExpRate = 1;
    private static int qMesoRate = 1;
    private static boolean cashShopEnabled = false;
    private static TimerManager tMan;
    private boolean isShutdownWork = false;

    public boolean isIsShutdownWork() {
        return isShutdownWork;
    }

    public void setIsShutdownWork(boolean isShutdownWork) {
        this.isShutdownWork = isShutdownWork;
    }

    public static boolean getCashShopEnabled() {
        return cashShopEnabled;
    }

    public static boolean getRanking() {
        return ranking;
    }

    public static int getChannelMaxPlayer() {
        return channelMaxPlayer;
    }

    public static int getExpRate() {
        return expRate;
    }

    public static int getMesoRate() {
        return mesoRate;
    }

    public static int getBossRate() {
        return bossRate;
    }

    public static int getDropRate() {
        return dropRate;
    }

    public static int getQMesoRate() {
        return qMesoRate;
    }

    public static int getQExpRate() {
        return qExpRate;
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public boolean isOnline() {
        return online;
    }

    public List<Pair<Integer, String>> worldRecommendedList() {
        return worldRecommendedList;
    }

    public void removeChannel(int worldid, int channel) {
        channels.remove(channel);

        World world = worlds.get(worldid);
        if (world != null) {
            world.removeChannel(channel);
        }
    }

    public Channel getChannel(int world, int channel) {
        return worlds.get(world).getChannel(channel);
    }

    public List<Channel> getChannelsFromWorld(int world) {
        return worlds.get(world).getChannels();
    }

    public List<Channel> getAllChannels() {
        List<Channel> channelz = new ArrayList<>();
        for (World world : worlds) {
            for (Channel ch : world.getChannels()) {
                channelz.add(ch);
            }
        }

        return channelz;
    }

    public String getIP(int world, int channel) {
        return channels.get(world).get(channel);
    }

    private void resetLogin(Connection c) {

        try (PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0")) {
            ps.executeUpdate();

        } catch (SQLException ex) {
            FilePrinter.printError("Server.txt", ex);

        }
    }

    private void resetMerchant(Connection c) {

        try (PreparedStatement ps = c.prepareStatement("UPDATE characters SET HasMerchant = 0")) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            FilePrinter.printError("Server.txt", ex);

        }
    }

    @Override
    public void run() {

        Properties p = new Properties();
        try {
            p.load(new InputStreamReader(new FileInputStream("SyncMaple.ini"), "UTF-8"));
        } catch (Exception e) {
            System.out.println("Setting not found");
            System.exit(0);
        }

        System.out.println("Taiwan Maplestory v" + ServerConstants.VERSION + " 伺服器啟動中...");

        Runtime.getRuntime().addShutdownHook(new Thread(shutdownThread(false)));

        DatabaseConnection.getConnection();
        Connection c = DatabaseConnection.getConnection();

        this.resetLogin(c);
        this.resetMerchant(c);

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());

        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));

        tMan = TimerManager.getInstance();
        tMan.start();
        tMan.register(tMan.purge(), 300000);//Purging ftw...
        tMan.register(new RankingWorker(), Integer.parseInt(p.getProperty("Sync.RankingInterval", "3600000")));
        tMan.register(new RateWorker(), Integer.parseInt(p.getProperty("Sync.TimeWorkerInterval", "300000")));

        ranking = Boolean.parseBoolean(p.getProperty("Sync.EnableRanking", "true"));
        channelMaxPlayer = Integer.parseInt(p.getProperty("Sync.CMaxPlayer", "100"));
        expRate = Integer.parseInt(p.getProperty("Sync.ExpRate", "1"));
        mesoRate = Integer.parseInt(p.getProperty("Sync.MesoRate", "1"));
        dropRate = Integer.parseInt(p.getProperty("Sync.DropRate", "1"));
        bossRate = Integer.parseInt(p.getProperty("Sync.BossRate", "1"));
        qExpRate = Integer.parseInt(p.getProperty("Sync.QExpRate", "1"));
        qMesoRate = Integer.parseInt(p.getProperty("Sync.QMesoRate", "1"));
        cashShopEnabled = Boolean.parseBoolean(p.getProperty("Sync.EnableCashShop", "false"));

        long timeToTake = System.currentTimeMillis();
        System.out.println("讀取\t技能資料中...");
        SkillFactory.loadAllSkills();
        System.out.println("\t技能資料於 " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " 秒 讀取完畢");

        timeToTake = System.currentTimeMillis();
        System.out.println("讀取\t物品資料");
        MapleItemInformationProvider.getInstance().getAllItems();
        CashItemFactory.getInstance().initialize();

        System.out.println("\t物品資料於 " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " 秒 讀取完畢");

        try {
            for (int i = 0; i < 1; i++) {
                //int i = 1;
                System.out.println("建立\t世界伺服器");
                World world = new World(i,
                        Integer.parseInt(p.getProperty("Sync.Flag")),
                        p.getProperty("Sync.EventMessage"),
                        getExpRate(),
                        getDropRate(),
                        getMesoRate(),
                        getBossRate());//ohlol

                worldRecommendedList.add(new Pair<>(i, p.getProperty("Sync.Whyamirecommended", "")));
                worlds.add(world);
                channels.add(new LinkedHashMap<Integer, String>());
                int channelCount = Integer.parseInt(p.getProperty("Sync.Channels", "1"));
                for (int j = 0; j < channelCount; j++) {
                    int channelid = j + 1;
                    Channel channel = new Channel(i, channelid);
                    world.addChannel(channel);
                    channels.get(i).put(channelid, channel.getIP());
                }

                world.setServerMessage(p.getProperty("Sync.ServerMessage", ""));
                System.out.println("世界伺服器讀取完畢\n");
            }
            Collections.reverse(worlds);
            System.out.println("");
        } catch (Exception e) {
            System.err.println(e);
            System.out.println("Setting not found");
            System.exit(0);
        }

        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        acceptor.setHandler(new MapleServerHandler());
        Integer port = ServerConstants.HOST_PORT;
        try {
            acceptor.bind(new InetSocketAddress(port));
        } catch (IOException ex) {
            System.err.println(ex + ":" + port.toString());
            System.exit(0);
        }

        System.out.println("登入伺服器端口: 8484 \n\n");
        System.out.println("伺服器已啟動完畢上線.");
        online = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (this.isOnline()) {
            String command;
            try {
                System.out.print(">>");
                command = br.readLine();

                if (command.equalsIgnoreCase("shutdown")) {
                    this.shutdownThread(false).run();
                } else if (command.equalsIgnoreCase("saveAll")) {
                    int channelCount = Integer.parseInt(p.getProperty("Sync.Channels", "1"));
                    for (int j = 0; j < channelCount; j++) {
                        worlds.get(0).getChannel(j + 1).saveAll();
                    }
                } else if (command.equalsIgnoreCase("reload")) {
                    PortalScriptManager.getInstance().clearScripts(); // 傳點腳本
                    MapleMonsterInformationProvider.getInstance().clearDrops(); // 怪物掉落
                    ReactorScriptManager.getInstance().clearDrops(); // 反應堆腳本
                    MapleShopFactory.getInstance().reloadShops(); // 商店腳本
                    for (Channel instance : Channel.getAllInstances()) {
                        instance.reloadEvents(); // 事件腳本
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void main(String args[]) {
        Server.getInstance().run();
    }

    public Properties getSubnetInfo() {
        return subnetInfo;
    }

    public MapleAlliance getAlliance(int id) {
        synchronized (alliances) {
            if (alliances.containsKey(id)) {
                return alliances.get(id);
            }
            return null;
        }
    }

    public void addAlliance(int id, MapleAlliance alliance) {
        synchronized (alliances) {
            if (!alliances.containsKey(id)) {
                alliances.put(id, alliance);
            }
        }
    }

    public void disbandAlliance(int id) {
        synchronized (alliances) {
            MapleAlliance alliance = alliances.get(id);
            if (alliance != null) {
                for (Integer gid : alliance.getGuilds()) {
                    guilds.get(gid).setAllianceId(0);
                }
                alliances.remove(id);
            }
        }
    }

    public void allianceMessage(int id, final byte[] packet, int exception, int guildex) {
        MapleAlliance alliance = alliances.get(id);
        if (alliance != null) {
            for (Integer gid : alliance.getGuilds()) {
                if (guildex == gid) {
                    continue;
                }
                MapleGuild guild = guilds.get(gid);
                if (guild != null) {
                    guild.broadcast(packet, exception);
                }
            }
        }
    }

    public boolean addGuildtoAlliance(int aId, int guildId) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.addGuild(guildId);
            return true;
        }
        return false;
    }

    public boolean removeGuildFromAlliance(int aId, int guildId) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.removeGuild(guildId);
            return true;
        }
        return false;
    }

    public boolean setAllianceRanks(int aId, String[] ranks) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setRankTitle(ranks);
            return true;
        }
        return false;
    }

    public boolean setAllianceNotice(int aId, String notice) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setNotice(notice);
            return true;
        }
        return false;
    }

    public boolean increaseAllianceCapacity(int aId, int inc) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.increaseCapacity(inc);
            return true;
        }
        return false;
    }

    public Set<Integer> getChannelServer(int world) {
        return new HashSet<>(channels.get(world).keySet());
    }

    public byte getHighestChannelId() {
        byte highest = 0;
        for (Iterator<Integer> it = channels.get(0).keySet().iterator(); it.hasNext();) {
            Integer channel = it.next();
            if (channel != null && channel.intValue() > highest) {
                highest = channel.byteValue();
            }
        }
        return highest;
    }

    public int createGuild(int leaderId, String name) {
        return MapleGuild.createGuild(leaderId, name);
    }

    public MapleGuild getGuild(int id, MapleGuildCharacter mgc) {
        synchronized (guilds) {
            if (guilds.get(id) != null) {
                return guilds.get(id);
            }
            if (mgc == null) {
                return null;
            }
            MapleGuild g = new MapleGuild(mgc);
            if (g.getId() == -1) {
                return null;
            }
            guilds.put(id, g);
            return g;
        }
    }

    public void clearGuilds() {//remake
        synchronized (guilds) {
            guilds.clear();
        }
        //for (List<Channel> world : worlds.values()) {
        //reloadGuildCharacters();

    }

    public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) {
        MapleGuild g = getGuild(mgc.getGuildId(), mgc);
        g.setOnline(mgc.getId(), bOnline, channel);
    }

    public int addGuildMember(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            return g.addGuildMember(mgc);
        }
        return 0;
    }

    public boolean setGuildAllianceId(int gId, int aId) {
        MapleGuild guild = guilds.get(gId);
        if (guild != null) {
            guild.setAllianceId(aId);
            return true;
        }
        return false;
    }

    public void leaveGuild(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.leaveGuild(mgc);
        }
    }

    public void guildChat(int gid, String name, int cid, String msg) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.guildChat(name, cid, msg);
        }
    }

    public void changeRank(int gid, int cid, int newRank) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.changeRank(cid, newRank);
        }
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) {
        MapleGuild g = guilds.get(initiator.getGuildId());
        if (g != null) {
            g.expelMember(initiator, name, cid);
        }
    }

    public void setGuildNotice(int gid, String notice) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.setGuildNotice(notice);
        }
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.memberLevelJobUpdate(mgc);
        }
    }

    public void changeRankTitle(int gid, String[] ranks) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.changeRankTitle(ranks);
        }
    }

    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.setGuildEmblem(bg, bgcolor, logo, logocolor);
        }
    }

    public void disbandGuild(int gid) {
        synchronized (guilds) {
            MapleGuild g = guilds.get(gid);
            g.disbandGuild();
            guilds.remove(gid);
        }
    }

    public boolean increaseGuildCapacity(int gid) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            return g.increaseCapacity();
        }
        return false;
    }

    public void gainGP(int gid, int amount) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.gainGP(amount);
        }
    }

    public PlayerBuffStorage getPlayerBuffStorage() {
        return buffStorage;
    }

    public void deleteGuildCharacter(MapleGuildCharacter mgc) {
        setGuildMemberOnline(mgc, false, (byte) -1);
        if (mgc.getGuildRank() > 1) {
            leaveGuild(mgc);
        } else {
            disbandGuild(mgc.getGuildId());
        }
    }

    public void reloadGuildCharacters(int world) {
        World worlda = getWorld(world);
        for (MapleCharacter mc : worlda.getPlayerStorage().getAllCharacters()) {
            if (mc.getGuildId() > 0) {
                setGuildMemberOnline(mc.getMGC(), true, worlda.getId());
                memberLevelJobUpdate(mc.getMGC());
            }
        }
        worlda.reloadGuildSummary();
    }

    public void broadcastMessage(int world, final byte[] packet) {
        for (Channel ch : getChannelsFromWorld(world)) {
            ch.broadcastPacket(packet);
        }
    }

    public World getWorld(int id) {
        World world = null;
        for (World w : worlds) {
            if (w.getId() == id) {
                world = w;
                break;
            }
        }

        return world;
    }

    public List<World> getWorlds() {
        return worlds;
    }

    public void gmChat(String message, String exclude) {
        GMServer.broadcastInGame(MaplePacketCreator.broadcastMsg(6, message));
        GMServer.broadcastOutGame(GMPacketCreator.chat(message), exclude);
    }

    public final Runnable shutdownThread(final boolean restart) {//only once :D
        return new Runnable() {
            @Override
            public void run() {
                Server.getInstance().setIsShutdownWork(true);
                System.out.println("伺服器 " + (restart ? "重新開機中" : "關閉中") + "\n");

                if (getWorlds() == null) {
                    return;//already shutdown
                }

                for (World w : getWorlds()) {
                    w.shutdown();
                }

                long ticks = System.currentTimeMillis();

                for (World w : getWorlds()) {
                    while (w.getPlayerStorage().getAllCharacters().size() > 0) {
                        try {
                            System.out.println("等待世界" + w.getId() + "關閉中\n");
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            System.err.println("FUCK MY LIFE");
                        }
                    }
                }
                for (Channel ch : getAllChannels()) {
                    while (ch.getConnectedClients() > 0) {
                        try {
                            System.out.println("等待世界" + ch.getWorld() + " 頻道" + ch.getId() + "關閉中\n");
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            System.err.println("FUCK MY LIFE");
                        }
                    }
                }

                tMan.stop();
                TimerManager.getInstance().stop();

                worlds.clear();
                worlds = null;
                channels.clear();
                channels = null;
                worldRecommendedList.clear();
                worldRecommendedList = null;

                System.out.println("所有伺服器以及頻道關閉完成。");
                acceptor.setCloseOnDeactivation(true);
                for (IoSession ss : acceptor.getManagedSessions().values()) {
                    ss.close(true);
                }
                acceptor.unbind();
                acceptor.dispose();
                acceptor = null;
                online = false;

                if (!restart) {
                    System.exit(0);
                } else {
                    System.out.println("\r\n重新啟動伺服器....\r\n");
                    Server.getInstance().setIsShutdownWork(false);
                    try {
                        instance.finalize();//FUU I CAN AND IT'S FREE
                    } catch (Throwable ex) {

                    }
                    instance = null;
                    System.gc();
                    getInstance().run();//DID I DO EVERYTHING?! D:
                }
            }
        };
    }
}
