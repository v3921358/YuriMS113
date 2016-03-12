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
package net.server.guild;

import client.MapleCharacter;
import client.MapleClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.server.Server;
import net.server.channel.Channel;
import net.server.guild.MapleBBSThread.MapleBBSReply;
import tools.DatabaseConnection;
import tools.packets.GuildPacket;
import tools.packets.MaplePacketCreator;

public class MapleGuild {

    public final static int CREATE_GUILD_COST = 1500000;
    public final static int CHANGE_EMBLEM_COST = 5000000;

    private enum BCOp {

        NONE, DISBAND, EMBELMCHANGE
    }
    private List<MapleGuildCharacter> members;
    private final Map<Integer, MapleBBSThread> bbs = new LinkedHashMap<Integer, MapleBBSThread>();
    private Map<Integer, List<Integer>> notifications = new LinkedHashMap<>();

    private String rankTitles[] = new String[5];
    // 1 = master, 2 = jr, 5 = lowest member
    private String name, notice;
    private int id, gp, logo, logoColor, leader, capacity, logoBG, logoBGColor, signature, allianceId;
    private int world;
    private boolean bDirty = true;

    public MapleGuild(MapleGuildCharacter initiator) {
        int guildid = initiator.getGuildId();
        world = initiator.getWorld();
        members = new ArrayList<>();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = " + guildid);
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                id = -1;
                ps.close();
                rs.close();
                return;
            }
            id = guildid;
            name = rs.getString("name");
            gp = rs.getInt("GP");
            logo = rs.getInt("logo");
            logoColor = rs.getInt("logoColor");
            logoBG = rs.getInt("logoBG");
            logoBGColor = rs.getInt("logoBGColor");
            capacity = rs.getInt("capacity");
            for (int i = 1; i <= 5; i++) {
                rankTitles[i - 1] = rs.getString("rank" + i + "title");
            }
            leader = rs.getInt("leader");
            notice = rs.getString("notice");
            signature = rs.getInt("signature");
            allianceId = rs.getInt("allianceId");
            ps.close();
            rs.close();
            ps = con.prepareStatement("SELECT id, name, level, job, guildrank, allianceRank FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            if (!rs.first()) {
                rs.close();
                ps.close();
                return;
            }
            do {
                members.add(new MapleGuildCharacter(rs.getInt("id"), rs.getInt("level"), rs.getString("name"), (byte) -1, world, rs.getInt("job"), rs.getInt("guildrank"), guildid, false, rs.getInt("allianceRank")));
            } while (rs.next());
            setOnline(initiator.getId(), true, initiator.getChannel());
            ps.close();
            rs.close();

            ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid DESC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            while (rs.next()) {
                final MapleBBSThread thread = new MapleBBSThread(rs.getInt("localthreadid"), rs.getString("name"), rs.getString("startpost"), rs.getLong("timestamp"),
                        guildid, rs.getInt("postercid"), rs.getInt("icon"));
                final PreparedStatement pse = con.prepareStatement("SELECT * FROM bbs_replies WHERE threadid = ?");
                pse.setInt(1, rs.getInt("threadid"));
                final ResultSet rse = pse.executeQuery();
                while (rse.next()) {
                    thread.replies.put(thread.replies.size(), new MapleBBSReply(thread.replies.size(), rse.getInt("postercid"), rse.getString("content"), rse.getLong("timestamp")));
                }
                rse.close();
                pse.close();
                bbs.put(rs.getInt("localthreadid"), thread);
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            System.out.println("unable to read guild information from sql" + se);
        }
    }

     public void buildNotifications() {
        if (!bDirty) {
            return;
        }
        Set<Integer> chs = Server.getInstance().getChannelServer(world);
        if (notifications.keySet().size() != chs.size()) {
            notifications.clear();
            for (Integer ch : chs) {
                notifications.put(ch, new LinkedList<Integer>());
            }
        } else {
            for (List<Integer> l : notifications.values()) {
                l.clear();
            }
        }
        synchronized (members) {
            for (MapleGuildCharacter mgc : members) {
                if (!mgc.isOnline()) {
                    continue;
                }
                List<Integer> ch = notifications.get(mgc.getChannel());
                if (ch != null) ch.add(mgc.getId());
                //Unable to connect to Channel... error was here
            }
        }
        bDirty = false;
    }

    public void writeToDB(boolean bDisband) {
        try {
            Connection con = DatabaseConnection.getConnection();
            if (!bDisband) {
                StringBuilder builder = new StringBuilder();
                builder.append("UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");
                for (int i = 0; i < 5; i++) {
                    builder.append("rank").append(i + 1).append("title = ?, ");
                }
                builder.append("capacity = ?, notice = ? WHERE guildid = ?");
                PreparedStatement ps = con.prepareStatement(builder.toString());

                ps.setInt(1, gp);
                ps.setInt(2, logo);
                ps.setInt(3, logoColor);
                ps.setInt(4, logoBG);
                ps.setInt(5, logoBGColor);
                for (int i = 6; i < 11; i++) {
                    ps.setString(i, rankTitles[i - 6]);
                }
                ps.setInt(11, capacity);
                ps.setString(12, notice);
                ps.setInt(13, this.id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("INSERT INTO bbs_threads(`postercid`, `name`, `timestamp`, `icon`, `startpost`, `guildid`, `localthreadid`) VALUES(?, ?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS);
                ps.setInt(6, id);
                for (MapleBBSThread bb : bbs.values()) {
                    ps.setInt(1, bb.ownerID);
                    ps.setString(2, bb.name);
                    ps.setLong(3, bb.timestamp);
                    ps.setInt(4, bb.icon);
                    ps.setString(5, bb.text);
                    ps.setInt(7, bb.localthreadID);
                    ps.executeUpdate();
                    final ResultSet rs = ps.getGeneratedKeys();
                    if (!rs.next()) {
                        rs.close();
                        continue;
                    }
                    final PreparedStatement pse = con.prepareStatement("INSERT INTO bbs_replies (`threadid`, `postercid`, `timestamp`, `content`, `guildid`) VALUES (?, ?, ?, ?, ?)");
                    pse.setInt(5, id);
                    for (MapleBBSReply r : bb.replies.values()) {
                        pse.setInt(1, rs.getInt(1));
                        pse.setInt(2, r.ownerID);
                        pse.setLong(3, r.timestamp);
                        pse.setString(4, r.content);
                        pse.execute();
                    }
                    pse.close();
                    rs.close();
                }
                ps.close();

            } else {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?");
                ps.setInt(1, this.id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                if (allianceId > 0) {
                    final MapleAlliance alliance = MapleAlliance.loadAlliance(allianceId);
                    if (alliance != null) {
                        alliance.removeGuild(id);
                    }
                }
                this.broadcast(GuildPacket.guildDisband(this.id));
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getLeaderId() {
        return leader;
    }

    public int getGP() {
        return gp;
    }

    public int getLogo() {
        return logo;
    }

    public void setLogo(int l) {
        logo = l;
    }

    public int getLogoColor() {
        return logoColor;
    }

    public void setLogoColor(int c) {
        logoColor = c;
    }

    public int getLogoBG() {
        return logoBG;
    }

    public void setLogoBG(int bg) {
        logoBG = bg;
    }

    public int getLogoBGColor() {
        return logoBGColor;
    }

    public void setLogoBGColor(int c) {
        logoBGColor = c;
    }

    public String getNotice() {
        if (notice == null) {
            return "";
        }
        return notice;
    }

    public String getName() {
        return name;
    }

    public Collection<MapleGuildCharacter> getMembers() {
        return Collections.unmodifiableCollection(members);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSignature() {
        return signature;
    }

    public void broadcast(final byte[] packet) {
        broadcast(packet, -1, BCOp.NONE);
    }

    public void broadcast(final byte[] packet, int exception) {
        broadcast(packet, exception, BCOp.NONE);
    }

    public void broadcast(final byte[] packet, int exceptionId, BCOp bcop) {
        synchronized (notifications) {
            if (bDirty) {
                buildNotifications();
            }
            try {
                for (Integer b : Server.getInstance().getChannelServer(world)) {
                    if (notifications.get(b).size() > 0) {
                        if (bcop == BCOp.DISBAND) {
                            Server.getInstance().getWorld(world).setGuildAndRank(notifications.get(b), 0, 5, exceptionId);
                        } else if (bcop == BCOp.EMBELMCHANGE) {
                            Server.getInstance().getWorld(world).changeEmblem(this.id, notifications.get(b), new MapleGuildSummary(this));
                        } else {
                            Server.getInstance().getWorld(world).sendPacket(notifications.get(b), packet, exceptionId);
                        }
                    }
                }
            } catch (Exception re) {
                System.out.println("Failed to contact channel(s) for broadcast.");//fu?
            }
        }
    }

    public void guildMessage(final byte[] serverNotice) {
        for (MapleGuildCharacter mgc : members) {
            for (Channel cs : Server.getInstance().getChannelsFromWorld(world)) {
                if (cs.getPlayerStorage().getCharacterById(mgc.getId()) != null) {
                    cs.getPlayerStorage().getCharacterById(mgc.getId()).getClient().announce(serverNotice);
                    break;
                }
            }
        }
    }

    public final void setOnline(int cid, boolean online, int channel) {
        boolean bBroadcast = true;
        for (MapleGuildCharacter mgc : members) {
            if (mgc.getId() == cid) {
                if (mgc.isOnline() && online) {
                    bBroadcast = false;
                }
                mgc.setOnline(online);
                mgc.setChannel(channel);
                break;
            }
        }
        if (bBroadcast) {
            this.broadcast(GuildPacket.guildMemberOnline(id, cid, online), cid);
        }
        bDirty = true;
    }
    
    

    public void guildChat(String name, int cid, String msg) {
        this.broadcast(MaplePacketCreator.multiChat(name, msg, 2), cid);
    }

    public final void allianceChat(final String name, final int cid, final String msg) {
        broadcast(MaplePacketCreator.multiChat(name, msg, 3), cid);
    }

    public String getRankTitle(int rank) {
        return rankTitles[rank - 1];
    }

    public static int createGuild(int leaderId, String name) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                ps.close();
                rs.close();
                return 0;
            }
            ps.close();
            rs.close();
            ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `signature`) VALUES (?, ?, ?)");
            ps.setInt(1, leaderId);
            ps.setString(2, name);
            ps.setInt(3, (int) System.currentTimeMillis());
            ps.execute();
            ps.close();
            ps = con.prepareStatement("SELECT guildid FROM guilds WHERE leader = ?");
            ps.setInt(1, leaderId);
            rs = ps.executeQuery();
            rs.first();
            int guildid = rs.getInt("guildid");
            rs.close();
            ps.close();
            return guildid;
        } catch (Exception e) {
            return 0;
        }
    }

    public int addGuildMember(MapleGuildCharacter mgc) {
        synchronized (members) {
            if (members.size() >= capacity) {
                return 0;
            }
            for (int i = members.size() - 1; i >= 0; i--) {
                if (members.get(i).getGuildRank() < 5 || members.get(i).getName().compareTo(mgc.getName()) < 0) {
                    members.add(i + 1, mgc);
                    bDirty = true;
                    break;
                }
            }
        }
        this.broadcast(GuildPacket.newGuildMember(mgc));
        return 1;
    }

    public void leaveGuild(MapleGuildCharacter mgc) {
        this.broadcast(GuildPacket.memberLeft(mgc, false));
        synchronized (members) {
            members.remove(mgc);
            bDirty = true;
        }
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) {
        synchronized (members) {
            java.util.Iterator<MapleGuildCharacter> itr = members.iterator();
            MapleGuildCharacter mgc;
            while (itr.hasNext()) {
                mgc = itr.next();
                if (mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()) {
                    this.broadcast(GuildPacket.memberLeft(mgc, true));
                    itr.remove();
                    bDirty = true;
                    try {
                        if (mgc.isOnline()) {
                            Server.getInstance().getWorld(mgc.getWorld()).setGuildAndRank(cid, 0, 5);
                        } else {
                            try {
                                try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)")) {
                                    ps.setString(1, mgc.getName());
                                    ps.setString(2, initiator.getName());
                                    ps.setString(3, "You have been expelled from the guild.");
                                    ps.setLong(4, System.currentTimeMillis());
                                    ps.executeUpdate();
                                }
                            } catch (SQLException e) {
                                System.out.println("expelMember - MapleGuild " + e);
                            }
                            Server.getInstance().getWorld(mgc.getWorld()).setOfflineGuildStatus((short) 0, (byte) 5, cid);
                        }
                    } catch (Exception re) {
                        re.printStackTrace();
                        return;
                    }
                    return;
                }
            }
            System.out.println("Unable to find member with name " + name + " and id " + cid);
        }
    }

    public void changeRank(int cid, int newRank) {
        for (MapleGuildCharacter mgc : members) {
            if (cid == mgc.getId()) {
                try {
                    if (mgc.isOnline()) {
                        Server.getInstance().getWorld(mgc.getWorld()).setGuildAndRank(cid, this.id, newRank);
                    } else {
                        Server.getInstance().getWorld(mgc.getWorld()).setOfflineGuildStatus((short) this.id, (byte) newRank, cid);
                    }
                } catch (Exception re) {
                    re.printStackTrace();
                    return;
                }
                mgc.setGuildRank(newRank);
                this.broadcast(GuildPacket.changeRank(mgc));
                return;
            }
        }
    }

    public void setGuildNotice(String notice) {
        this.notice = notice;
        writeToDB(false);
        this.broadcast(GuildPacket.guildNotice(this.id, notice));
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        for (MapleGuildCharacter member : members) {
            if (mgc.equals(member)) {
                member.setJobId(mgc.getJobId());
                member.setLevel(mgc.getLevel());
                this.broadcast(GuildPacket.guildMemberLevelJobUpdate(mgc));
                break;
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MapleGuildCharacter)) {
            return false;
        }
        MapleGuildCharacter o = (MapleGuildCharacter) other;
        return (o.getId() == id && o.getName().equals(name));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 89 * hash + this.id;
        return hash;
    }

    public void changeRankTitle(String[] ranks) {
        System.arraycopy(ranks, 0, rankTitles, 0, 5);
        this.broadcast(GuildPacket.rankTitleChange(this.id, ranks));
        this.writeToDB(false);
    }

    public void disbandGuild() {
        this.writeToDB(true);
        this.broadcast(null, -1, BCOp.DISBAND);
    }

    public void setGuildEmblem(short bg, byte bgcolor, short logo, byte logocolor) {
        this.logoBG = bg;
        this.logoBGColor = bgcolor;
        this.logo = logo;
        this.logoColor = logocolor;
        this.writeToDB(false);
        this.broadcast(null, -1, BCOp.EMBELMCHANGE);
    }

    public MapleGuildCharacter getMGC(int cid) {
        for (MapleGuildCharacter mgc : members) {
            if (mgc.getId() == cid) {
                return mgc;
            }
        }
        return null;
    }

    public boolean increaseCapacity() {
        if (capacity > 99) {
            return false;
        }
        capacity += 5;
        this.writeToDB(false);
        this.broadcast(GuildPacket.guildCapacityChange(this.id, this.capacity));
        return true;
    }

    public void gainGP(int amount) {
        this.gp += amount;
        this.writeToDB(false);
        this.guildMessage(MaplePacketCreator.updateGP(this.id, this.gp));
    }

    public static MapleGuildResponse sendInvite(MapleClient c, String targetName) {
        MapleCharacter mc = c.getChannelServer().getPlayerStorage().getCharacterByName(targetName);
        if (mc == null) {
            return MapleGuildResponse.NOT_IN_CHANNEL;
        }
        if (mc.getGuildId() > 0) {
            return MapleGuildResponse.ALREADY_IN_GUILD;
        }
        mc.getClient().announce(GuildPacket.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getName()));
        return null;
    }

    public static void displayGuildRanks(MapleClient c, int npcid) {
        try {
            ResultSet rs;
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `name`, `GP`, `logoBG`, `logoBGColor`, " + "`logo`, `logoColor` FROM guilds ORDER BY `GP` DESC LIMIT 50")) {
                rs = ps.executeQuery();
                c.announce(MaplePacketCreator.showGuildRanks(npcid, rs));
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("failed to display guild ranks. " + e);
        }
    }

    public int getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(int aid) {
        this.allianceId = aid;
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE guilds SET allianceId = ? WHERE guildid = ?")) {
                ps.setInt(1, aid);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
        }
    }

    public int getIncreaseGuildCost(int size) {
        return 500000;
    }

    public final List<MapleBBSThread> getBBS() {
        final List<MapleBBSThread> ret = new ArrayList<MapleBBSThread>(bbs.values());
        Collections.sort(ret, new MapleBBSThread.ThreadComparator());
        return ret;
    }

    public final int addBBSThread(final String title, final String text, final int icon, final boolean bNotice, final int posterID) {
        final int add = bbs.get(0) == null ? 1 : 0; //add 1 if no notice
        final int ret = bNotice ? 0 : Math.max(1, bbs.size() + add);
        bbs.put(ret, new MapleBBSThread(ret, title, text, System.currentTimeMillis(), this.id, posterID, icon));
        return ret;
    }

    public final void editBBSThread(final int localthreadid, final String title, final String text, final int icon, final int posterID, final int guildRank) {
        final MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            bbs.put(localthreadid, new MapleBBSThread(localthreadid, title, text, System.currentTimeMillis(), this.id, thread.ownerID, icon));
        }
    }

    public final void deleteBBSThread(final int localthreadid, final int posterID, final int guildRank) {
        final MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            bbs.remove(localthreadid);
        }
    }

    public final void addBBSReply(final int localthreadid, final String text, final int posterID) {
        final MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null) {
            thread.replies.put(thread.replies.size(), new MapleBBSReply(thread.replies.size(), posterID, text, System.currentTimeMillis()));
        }
    }

    public final void deleteBBSReply(final int localthreadid, final int replyid, final int posterID, final int guildRank) {
        final MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null) {
            final MapleBBSReply reply = thread.replies.get(replyid);
            if (reply != null && (reply.ownerID == posterID || guildRank <= 2)) {
                thread.replies.remove(replyid);
            }
        }
    }
}
