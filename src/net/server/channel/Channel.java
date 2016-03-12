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
package net.server.channel;

import client.MapleCharacter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.MapleServerHandler;
import net.mina.MapleCodecFactory;
import net.server.PlayerStorage;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import provider.MapleDataProviderFactory;
import scripting.event.EventScriptManager;
import server.TimerManager;
import server.events.MapleCoconut;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleFitness;
import server.events.MapleOla;
import server.events.MapleOxQuiz;
import server.events.MapleSnowball;
import server.events.MapleSurvival;
import server.expeditions.MapleExpedition;
import server.expeditions.MapleExpeditionType;
import server.maps.HiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.FilePrinter;
import tools.packets.MaplePacketCreator;

public final class Channel {

    private int port = 7575;
    private PlayerStorage players = new PlayerStorage();
    private int world, channel;
    private IoAcceptor acceptor;
    private String ip, serverMessage;
    private MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private Map<Integer, HiredMerchant> hiredMerchants = new HashMap<>();
    private ReentrantReadWriteLock merchant_lock = new ReentrantReadWriteLock(true);
    private static final Map<Integer, Channel> instances = new HashMap<>();
    private EnumMap<MapleExpeditionType, MapleExpedition> expeditions = new EnumMap<>(MapleExpeditionType.class);
    private int eventmap = -1;
    private final Map<MapleEventType, MapleEvent> events = new EnumMap<>(MapleEventType.class);
    public boolean eventOn = false;
    public int eventMap = 0;
    private boolean eventWarp;
    private String eventHost;
    private String eventName;
    private boolean finishedShutdown = false;

    public Channel(final int world, final int channel) {
        this.world = world;
        this.channel = channel;
        this.mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")), world, channel);

        Properties p = new Properties();
        try {
            p.load(new InputStreamReader(new FileInputStream("SyncMaple.ini"), "UTF-8"));
        } catch (Exception e) {
            System.out.println("Setting not found");
            System.exit(0);
        }
        //p.getProperty("Sync.EventMessage")
        //Integer.parseInt(p.getProperty("Sync.ExpRate"))

        try {
            eventSM = new EventScriptManager(this, p.getProperty("Sync.Events").split(","));
            port = 7575 + this.channel - 1;
            port += (world * 100);
            ip = p.getProperty("Sync.Host") + ":" + port;
            IoBuffer.setUseDirectBuffer(false);
            IoBuffer.setAllocator(new SimpleBufferAllocator());
            acceptor = new NioSocketAcceptor();
            TimerManager.getInstance().register(new respawnMaps(), 10000);
            acceptor.setHandler(new MapleServerHandler(world, channel));
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
            acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
            acceptor.bind(new InetSocketAddress(port));
            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);

            eventSM.init();
            System.out.println("\t頻道 " + getId() + ": 使用端口 " + port);
        } catch (Exception e) {
            FilePrinter.printError("Server.txt", e);
        }
    }

    public final void shutdown() {
        try {
            System.out.println("即將關閉 世界" + world + " 頻道 " + channel);
            closeAllMerchants();
            System.out.println("\t世界" + world + " 頻道 " + channel + " 儲存個人商店完成");
            players.disconnectAll();
            System.out.println("\t世界" + world + " 頻道 " + channel + " 儲存角色完成");
            acceptor.setCloseOnDeactivation(true);
            finishedShutdown = true;
            System.out.println("關閉 世界" + world + " 頻道 " + channel + "成功");
        } catch (Exception e) {
            System.err.println("Error while shutting down Channel " + channel + " on World " + world + "\r\n" + e);
        }
    }

    public void closeAllMerchants() {
        WriteLock wlock = merchant_lock.writeLock();
        wlock.lock();
        try {
            final Iterator<HiredMerchant> hmit = hiredMerchants.values().iterator();
            while (hmit.hasNext()) {
                hmit.next().forceClose();
                hmit.remove();
            }
        } catch (Exception e) {
        } finally {
            wlock.unlock();
        }
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public static final Channel getInstance(final int channel) {
        return instances.get(channel);
    }

    public int getWorld() {
        return world;
    }

    public void addPlayer(MapleCharacter chr) {
        players.addPlayer(chr);
        chr.announce(MaplePacketCreator.broadcastMsg(serverMessage));
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public void removePlayer(MapleCharacter chr) {
        players.removePlayer(chr.getId());
    }

    public int getConnectedClients() {
        return players.getAllCharacters().size();
    }

    public void broadcastPacket(final byte[] data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            chr.announce(data);
        }
    }

    public final int getId() {
        return channel;
    }

    public String getIP() {
        return ip;
    }

    public int getEvent() {
        return eventmap;
    }

    public final void setEvent(final int ze) {
        this.eventmap = ze;
    }

    public MapleEvent getEvent(final MapleEventType t) {
        return events.get(t);
    }
    
    public static final Collection<Channel> getAllInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }
    
    public final void reloadEvents() {
        Properties p = new Properties();
        eventSM.cancel();
        eventSM = new EventScriptManager(this, p.getProperty("Sync.Events").split(","));
        eventSM.init();
    }
    
   public void loadEvents() {
       if (!events.isEmpty()) {
            return;
        }
        events.put(MapleEventType.CokePlay, new MapleCoconut(channel, MapleEventType.CokePlay)); //yep, coconut. same shit
        events.put(MapleEventType.Coconut, new MapleCoconut(channel, MapleEventType.Coconut));
        events.put(MapleEventType.Fitness, new MapleFitness(channel, MapleEventType.Fitness));
        events.put(MapleEventType.OlaOla, new MapleOla(channel, MapleEventType.OlaOla));
        events.put(MapleEventType.OxQuiz, new MapleOxQuiz(channel, MapleEventType.OxQuiz));
        events.put(MapleEventType.Snowball, new MapleSnowball(channel, MapleEventType.Snowball));
        events.put(MapleEventType.Survival, new MapleSurvival(channel, MapleEventType.Survival));
   } 
   
    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public void broadcastGMPacket(final byte[] data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr.isGM()) {
                chr.announce(data);
            }
        }
    }

    public void broadcastGMPacket(final byte[] data, String exclude) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr.isGM() && !chr.getName().equals(exclude)) {
                chr.announce(data);
            }
        }
    }

    public void yellowWorldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.announce(MaplePacketCreator.sendYellowTip(msg));
        }
    }

    public void worldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.dropMessage(msg);
        }
    }

    public List<MapleCharacter> getPartyMembers(MapleParty party) {
        List<MapleCharacter> partym = new ArrayList<>(8);
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar.getChannel() == getId()) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    partym.add(chr);
                }
            }
        }
        return partym;

    }

    public class respawnMaps implements Runnable {

        @Override
        public void run() {
            for (Entry<Integer, MapleMap> map : mapFactory.getMaps().entrySet()) {
                map.getValue().respawn();
            }
        }
    }

    public Map<Integer, HiredMerchant> getHiredMerchants() {
        return hiredMerchants;
    }

    public void addHiredMerchant(int chrid, HiredMerchant hm) {
        WriteLock wlock = merchant_lock.writeLock();
        wlock.lock();
        try {
            hiredMerchants.put(chrid, hm);
        } finally {
            wlock.unlock();
        }
    }

    public void removeHiredMerchant(int chrid) {
        WriteLock wlock = merchant_lock.writeLock();
        wlock.lock();
        try {
            hiredMerchants.remove(chrid);
        } finally {
            wlock.unlock();
        }
    }

    public int[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<Integer> ret = new ArrayList<>(characterIds.length);
        PlayerStorage playerStorage = getPlayerStorage();
        for (int characterId : characterIds) {
            MapleCharacter chr = playerStorage.getCharacterById(characterId);
            if (chr != null) {
                if (chr.getBuddylist().containsVisible(charIdFrom)) {
                    ret.add(characterId);
                }
            }
        }
        int[] retArr = new int[ret.size()];
        int pos = 0;
        for (Integer i : ret) {
            retArr[pos++] = i;
        }
        return retArr;
    }

    public boolean hasExpedition(MapleExpeditionType type) {
        return expeditions.containsKey(type);
    }

    public void addExpedition(MapleExpeditionType type, MapleExpedition exped) {
        expeditions.put(type, exped);
    }

    public MapleExpedition getExpedition(MapleExpeditionType type) {
        return expeditions.get(type);
    }

    public boolean isConnected(String name) {
        return getPlayerStorage().getCharacterByName(name) != null;
    }

    public boolean finishedShutdown() {
        return finishedShutdown;
    }

    public void setServerMessage(String message) {
        this.serverMessage = message;
        broadcastPacket(MaplePacketCreator.broadcastMsg(message));
    }

    public void saveAll() {
        
        int ppl = 0;
        for (MapleCharacter chr : this.players.getAllCharacters()) {
            ++ppl;
            chr.saveToDB();
        }
        System.out.println("[自動存檔] 已經將頻道 " + this.channel + " 的 " + ppl + " 個玩家保存到數據中.");
    }
}
