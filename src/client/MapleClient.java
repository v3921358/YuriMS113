package client;

import gm.server.GMServer;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import net.server.Server;
import net.server.channel.Channel;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.MapleMessengerCharacter;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;
import org.apache.mina.core.session.IoSession;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestActionManager;
import scripting.quest.QuestScriptManager;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MapleTrade;
import server.TimerManager;
import server.maps.HiredMerchant;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.HexTool;
import tools.MapleAESOFB;
import tools.packets.LoginPacket;
import tools.packets.MaplePacketCreator;
import server.quest.MapleQuest;
import tools.encrypt;


public class MapleClient {

    public static final int LOGIN_NOTLOGGEDIN = 0;
    public static final int LOGIN_SERVER_TRANSITION = 1;
    public static final int LOGIN_LOGGEDIN = 2;
    public static final String CLIENT_KEY = "CLIENT";
    private MapleAESOFB send;
    private MapleAESOFB receive;
    private final IoSession session;
    private MapleCharacter player;
    private int channel = 1;
    private int accId = 1;
    private boolean loggedIn = false;
    private boolean serverTransition = false;
    private Calendar birthday = null;
    private String accountName = null;
    private int world;
    private long lastPong;
    private int gmlevel;
    private Set<String> macs = new HashSet<>();
    private final Map<String, ScriptEngine> engines = new HashMap<>();
    private ScheduledFuture<?> idleTask = null;
    private byte characterSlots = 3;
    private byte loginattempt = 0;
    private String secondPassword;
    private final int pinattempt = 0;
    private final int picattempt = 0;
    private byte gender = -1;
    private boolean disconnecting = false;
    private final Lock mutex = new ReentrantLock(true);
    private long lastNpcClick;

    public MapleClient(MapleAESOFB send, MapleAESOFB receive, IoSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }

    public synchronized MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public synchronized MapleAESOFB getSendCrypto() {
        return send;
    }

    public synchronized IoSession getSession() {
        return session;
    }

    public MapleCharacter getPlayer() {
        return player;
    }

    public void setPlayer(MapleCharacter player) {
        this.player = player;
    }

    public void sendCharList(int server) {
        this.session.write(LoginPacket.getCharList(this, server));
    }

    public List<MapleCharacter> loadCharacters(int serverId) {
        List<MapleCharacter> chars = new ArrayList<>(15);
        try {
            for (CharNameAndId cni : loadCharactersInternal(serverId)) {
                chars.add(MapleCharacter.loadCharFromDB(cni.id, this, false));
            }
        } catch (Exception e) {
        }
        return chars;
    }

    public List<String> loadCharacterNames(int serverId) {
        List<String> chars = new ArrayList<>(15);
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            chars.add(cni.name);
        }
        return chars;
    }

    private List<CharNameAndId> loadCharactersInternal(int serverId) {
        PreparedStatement ps;
        List<CharNameAndId> chars = new ArrayList<>(15);
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT id, name FROM characters WHERE accountid = ? AND world = ?");
            ps.setInt(1, this.getAccID());
            ps.setInt(2, serverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
                }
            }
            ps.close();
        } catch (SQLException e) {
            FilePrinter.printError("MapleClient.txt", e);
        }
        return chars;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean hasBannedIP() {
        boolean ret = false;
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')")) {
                ps.setString(1, session.getRemoteAddress().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ret = true;
                    }
                }
            }
        } catch (SQLException e) {
        }
        return ret;
    }

    public boolean hasBannedMac() {
        if (macs.isEmpty()) {
            return false;
        }
        boolean ret = false;
        int i;
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");
            for (i = 0; i < macs.size(); i++) {
                sql.append("?");
                if (i != macs.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
                i = 0;
                for (String mac : macs) {
                    i++;
                    ps.setString(i, mac);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ret = true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    private void loadMacsIfNescessary() throws SQLException {
        if (macs.isEmpty()) {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT macs FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        for (String mac : rs.getString("macs").split(", ")) {
                            if (!mac.equals("")) {
                                macs.add(mac);
                            }
                        }
                    }
                }
            }
        }
    }

    public void banMacs() {
        Connection con = DatabaseConnection.getConnection();
        try {
            loadMacsIfNescessary();
            List<String> filtered = new LinkedList<>();
            try (PreparedStatement ps = con.prepareStatement("SELECT filter FROM macfilters"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filtered.add(rs.getString("filter"));
                }
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)")) {
                for (String mac : macs) {
                    boolean matched = false;
                    for (String filter : filtered) {
                        if (mac.matches(filter)) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        ps.setString(1, mac);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
        }
    }

    public int finishLogin() {
        synchronized (MapleClient.class) {
            if (getLoginState() > LOGIN_NOTLOGGEDIN) {
                loggedIn = false;
                return 7;
            }
            updateLoginState(LOGIN_LOGGEDIN);
        }
        return 0;
    }
    
public boolean sigup(String name, String pwd) {
        boolean success = false;
        Connection con;
        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception ex) {
            //log.error("ERROR", ex);
            return false;
        }

        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, birthday, macs) VALUES (?, ?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, encrypt.sha1(pwd));//lol, no encryption. in fags we are <3
            ps.setString(3, "0000-00-00");
            ps.setString(4, "00-00-00-00-00-00");
            ps.executeUpdate();
            ps.close();
            success = true;
        } catch (SQLException ex) {
            System.err.println(ex);
            //log.error("ERROR", ex);
        }
        return success;
    }

    public int login(String login, String pwd) {
        loginattempt++;
        if (loginattempt > 4) {
            getSession().close(true);
        }
        int loginok = 5;
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT id, password, gender, banned, gm, 2ndpassword, characterslots, tos FROM accounts WHERE name = ?");
            ps.setString(1, login);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getByte("banned") == 1) {
                    return 3;
                }
                accId = rs.getInt("id");
                gmlevel = rs.getInt("gm");
                secondPassword = rs.getString("2ndpassword");
                gender = rs.getByte("gender");
                characterSlots = rs.getByte("characterslots");
                String passhash = rs.getString("password");

                //we do not unban
                byte tos = rs.getByte("tos");
                ps.close();
                rs.close();
                if (getLoginState() > LOGIN_NOTLOGGEDIN) { // already loggedin
                    loggedIn = false;
                    loginok = 7;
                } else if (pwd.equals(passhash) || encrypt.checkSHA1(pwd, passhash) || encrypt.checkSHA512(pwd, passhash)) {
                    if (tos == 0) {
                        loginok = 0;
                    } else {
                        loginok = 0;
                    }
                } else {
                    loggedIn = false;
                    loginok = 4;
                }

                ps = con.prepareStatement("INSERT INTO iplog (accountid, ip) VALUES (?, ?)");
                ps.setInt(1, accId);
                ps.setString(2, session.getRemoteAddress().toString());
                ps.executeUpdate();
            }else{
                return 1;
            }
        } catch (SQLException e) {
            FilePrinter.printError("MapleClient.txt", e);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException e) {
            }
        }

        if (loginok == 0) {
            loginattempt = 0;
        }
        return loginok;
    }

    public Calendar getTempBanCalendar() {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Calendar lTempban = Calendar.getInstance();
        try {
            ps = con.prepareStatement("SELECT `tempban` FROM accounts WHERE id = ?");
            ps.setInt(1, getAccID());
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            long blubb = rs.getLong("tempban");
            if (blubb == 0) { // basically if timestamp in db is 0000-00-00
                return null;
            }
            lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
            return lTempban;
        } catch (SQLException e) {
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
            }
        }
        return null;//why oh why!?!
    }

    public static long dottedQuadToLong(String dottedQuad) throws RuntimeException {
        String[] quads = dottedQuad.split("\\.");
        if (quads.length != 4) {
            throw new RuntimeException("Invalid IP Address format.");
        }
        long ipAddress = 0;
        for (int i = 0; i < 4; i++) {
            int quad = Integer.parseInt(quads[i]);
            ipAddress += (long) (quad % 256) * (long) Math.pow(256, (double) (4 - i));
        }
        return ipAddress;
    }

    public void updateMacs(String macData) {
        macs.addAll(Arrays.asList(macData.split(", ")));
        StringBuilder newMacData = new StringBuilder();
        Iterator<String> iter = macs.iterator();
        PreparedStatement ps = null;
        while (iter.hasNext()) {
            String cur = iter.next();
            newMacData.append(cur);
            if (iter.hasNext()) {
                newMacData.append(", ");
            }
        }
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?");
            ps.setString(1, newMacData.toString());
            ps.setInt(2, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            FilePrinter.printError("MapleClient.txt", e);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public int getAccID() {
        return accId;
    }

    public void updateLoginState(int newstate) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?")) {
                ps.setInt(1, newstate);
                ps.setInt(2, getAccID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            FilePrinter.printError("MapleClient.txt", e);
        }
        if (newstate == LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
        } else {
            serverTransition = (newstate == LOGIN_SERVER_TRANSITION);
            loggedIn = !serverTransition;
        }
    }

    public int getLoginState() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT loggedin, lastlogin, UNIX_TIMESTAMP(birthday) as birthday FROM accounts WHERE id = ?");
            ps.setInt(1, getAccID());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("getLoginState - MapleClient");
            }
            birthday = Calendar.getInstance();
            long blubb = rs.getLong("birthday");
            if (blubb > 0) {
                birthday.setTimeInMillis(blubb * 1000);
            }
            int state = rs.getInt("loggedin");
            if (state == LOGIN_SERVER_TRANSITION) {
                if (rs.getTimestamp("lastlogin").getTime() + 30000 < System.currentTimeMillis()) {
                    state = LOGIN_NOTLOGGEDIN;
                    updateLoginState(LOGIN_NOTLOGGEDIN);
                }
            } else if (state == LOGIN_LOGGEDIN && player == null) {
                state = LOGIN_NOTLOGGEDIN;
                updateLoginState(LOGIN_NOTLOGGEDIN);
            }
            rs.close();
            ps.close();
            if (state == LOGIN_LOGGEDIN) {
                loggedIn = true;
            } else if (state == LOGIN_SERVER_TRANSITION) {
                ps = con.prepareStatement("UPDATE accounts SET loggedin = 0 WHERE id = ?");
                ps.setInt(1, getAccID());
                ps.executeUpdate();
                ps.close();
            } else {
                loggedIn = false;
            }
            return state;
        } catch (SQLException e) {
            loggedIn = false;
            FilePrinter.printError("MapleClient.txt", e);
            throw new RuntimeException("login state");
        }
    }

    public boolean checkBirthDate(Calendar date) {
        return date.get(Calendar.YEAR) == birthday.get(Calendar.YEAR) && date.get(Calendar.MONTH) == birthday.get(Calendar.MONTH) && date.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH);
    }

    private void removePlayer() {
        try {
            player.cancelAllBuffs(true);
            player.cancelAllDebuffs();
            final MaplePlayerShop mps = player.getPlayerShop();
            if (mps != null) {
                mps.removeVisitors();
                player.setPlayerShop(null);
            }
            final HiredMerchant merchant = player.getHiredMerchant();
            if (merchant != null) {
                if (merchant.isOwner(player)) {
                    merchant.setOpen(true);
                } else {
                    merchant.removeVisitor(player);
                }
                try {
                    merchant.saveItems(false);
                } catch (SQLException ex) {
                    System.out.println("Error while saving Hired Merchant items.");
                }
            }
            player.setMessenger(null);
            final MapleMiniGame game = player.getMiniGame();
            if (game != null) {
                player.setMiniGame(null);
                if (game.isOwner(player)) {
                    player.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(player));
                    game.broadcastToVisitor(MaplePacketCreator.getMiniGameClose());
                } else {
                    game.removeVisitor(player);
                }
            }
            NPCScriptManager.getInstance().dispose(this);
            QuestScriptManager.getInstance().dispose(this);
            if (player.getTrade() != null) {
                MapleTrade.cancelTrade(player);
            }
            if (gmlevel > 0) {
                GMServer.removeInGame(player.getName());
            }
            if (player.getEventInstance() != null) {
                player.getEventInstance().playerDisconnected(player);
            }
            if (player.getMap() != null) {
                player.getMap().removePlayer(player);
            }
        } catch (final Throwable t) {
            FilePrinter.printError(FilePrinter.ACCOUNT_STUCK, t);
        }
    }

    public final void disconnect(boolean shutdown, boolean cashshop) {//once per MapleClient instance
        //System.out.println("儲存" + this.player.getName() + "中...");
        
        if (disconnecting) {
            return;
        }
        disconnecting = true;

        if (player != null && player.isLoggedin() && player.getClient() != null) {

            MapleMap map = player.getMap();
            final MapleParty party = player.getParty();
            final MapleGuild guild = player.getGuild();
            final int idz = player.getId(), messengerid = player.getMessenger() == null ? 0 : player.getMessenger().getId(), fid = player.getFamilyId();
            final String namez = player.getName();
            final BuddyList bl = player.getBuddylist();
            final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
            final MapleMessengerCharacter chrm = new MapleMessengerCharacter(player);
            //final MapleGuildCharacter chrg = player.getMGC();
            final MapleGuildCharacter chrg = player.getMGC();

            removePlayer();
            player.saveToDB();
            if (channel == -1 || shutdown) {
                player = null;
                return;
            }
            final World worlda = getWorldServer();
            try {
                if (!cashshop) {
                    if (worlda != null && messengerid > 0) {
                        worlda.leaveMessenger(messengerid, chrm);
                    }
                    
                        for (MapleQuestStatus status : player.getStartedQuests()) { //This is for those quests that you have to stay logged in for a certain amount of time
                            MapleQuest quest = status.getQuest();
                            if (quest.getTimeLimit() > 0) {
                                MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
                                newStatus.setForfeited(player.getQuest(quest).getForfeited() + 1);
                                player.updateQuest(newStatus);
                            }
                        }
                    
                    if (guild != null) {
                        guild.setOnline(player.getId(), false, channel);
                    }

                    if (party != null && worlda != null) {
                        chrp.setOnline(false);
                        worlda.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                        if (map != null && party.getLeader().getId() == idz) {
                            MaplePartyCharacter lchr = null;
                            for (MaplePartyCharacter pchr : party.getMembers()) {
                                if (pchr != null && map.getCharacterById(pchr.getId()) != null && (lchr == null || lchr.getLevel() < pchr.getLevel())) {
                                    lchr = pchr;
                                }
                            }
                            if (lchr != null) {
                                worlda.updateParty(party.getId(), PartyOperation.CHANGE_LEADER_DC, lchr);
                            }
                        }
                    }
                    if (bl != null && worlda != null) {

                        if (!this.serverTransition) {
                            worlda.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                        } else {
                            worlda.loggedOn(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                        }
                    }
                } else {
                    if (party != null) {
                        chrp.setOnline(false);
                        worlda.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                    }
                    if (!this.serverTransition) {
                        worlda.loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                    } else {
                        worlda.loggedOn(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
                    }
                }
            } catch (final Exception e) {
                FilePrinter.printError(FilePrinter.ACCOUNT_STUCK, e);
            } finally {
                getChannelServer().removePlayer(player);
                if (!this.serverTransition && worlda != null) {
                    worlda.removePlayer(player);
                    if (player != null) {//no idea, occur :(
                        player.empty(false);
                    }
                    player.logOff();
                }
                player = null;
            }
        }
        if (!serverTransition && isLoggedIn()) {
            updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
            session.removeAttribute(MapleClient.CLIENT_KEY); // prevents double dcing during login
        }
        engines.clear();
    }

    private void clear() {
        this.accountName = null;
        this.macs = null;
        this.birthday = null;
        if (this.idleTask != null) {
            this.idleTask.cancel(true);
            this.idleTask = null;
        }
        this.player = null;
        this.receive = null;
        this.send = null;
    }

    public int getChannel() {
        return channel;
    }

    public Channel getChannelServer() {
        return Server.getInstance().getChannel(world, channel);
    }

    public World getWorldServer() {
        return Server.getInstance().getWorld(world);
    }

    public Channel getChannelServer(byte channel) {
        return Server.getInstance().getChannel(world, channel);
    }

    public boolean deleteCharacter(int cid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, guildid, guildrank, name, allianceRank FROM characters WHERE id = ? AND accountid = ?")) {
                ps.setInt(1, cid);
                ps.setInt(2, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    if (rs.getInt("guildid") > 0) {
                        try {
                            Server.getInstance().deleteGuildCharacter(new MapleGuildCharacter(cid, 0, rs.getString("name"), (byte) -1, (byte) -1, 0, rs.getInt("guildrank"), rs.getInt("guildid"), false, rs.getInt("allianceRank")));
                        } catch (Exception re) {
                            return false;
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM wishlists WHERE charid = ?")) {
                ps.setInt(1, cid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM characters WHERE id = ?")) {
                ps.setInt(1, cid);
                ps.executeUpdate();
            }
            String[] toDel = {"famelog", "inventoryitems", "keymap", "queststatus", "savedlocations", "skillmacros", "skills", "eventstats"};
            for (String s : toDel) {
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM `" + s + "` WHERE characterid = ?", cid);
            }
            return true;
        } catch (SQLException e) {
            FilePrinter.printError("MapleClient.txt", e);
            return false;
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String a) {
        this.accountName = a;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void pongReceived() {
        lastPong = System.currentTimeMillis();
    }

    public void sendPing() {
        final long then = System.currentTimeMillis();
        announce(LoginPacket.getPing());
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    if (lastPong < then) {
                        if (getSession() != null && getSession().isConnected()) {
                            getSession().close(true);
                        }
                    }
                } catch (NullPointerException e) {
                }
            }
        }, 15000);
    }

    public Set<String> getMacs() {
        return Collections.unmodifiableSet(macs);
    }

    public int gmLevel() {
        return this.gmlevel;
    }

    public void setScriptEngine(String name, ScriptEngine e) {
        engines.put(name, e);
    }

    public ScriptEngine getScriptEngine(String name) {
        return engines.get(name);
    }

    public void removeScriptEngine(String name) {
        engines.remove(name);
    }

    public ScheduledFuture<?> getIdleTask() {
        return idleTask;
    }

    public void setIdleTask(ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }

    public NPCConversationManager getCM() {
        return NPCScriptManager.getInstance().getCM(this);
    }

    public QuestActionManager getQM() {
        return QuestScriptManager.getInstance().getQM(this);
    }

    public boolean acceptToS() {
        boolean disconnectForBeingAFaggot = false;
        if (accountName == null) {
            return true;
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `tos` FROM accounts WHERE id = ?");
            ps.setInt(1, accId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if (rs.getByte("tos") == 1) {
                    disconnectForBeingAFaggot = true;
                }
            }
            ps.close();
            rs.close();
            ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET tos = 1 WHERE id = ?");
            ps.setInt(1, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
        return disconnectForBeingAFaggot;
    }

    public final Lock getLock() {
        return mutex;
    }

    public final void update2ndPassword() {

        try {

            MessageDigest digester = MessageDigest.getInstance("SHA-1");
            digester.update(secondPassword.getBytes("UTF-8"), 0, secondPassword.length());
            String hash = HexTool.toString(digester.digest()).replace(" ", "").toLowerCase();

            final Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ? WHERE id = ?")) {
                ps.setString(1, hash);
                ps.setInt(2, accId);
                ps.executeUpdate();

            } catch (SQLException ex) {
                FilePrinter.printError("MapleClient.txt", ex);

            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(MapleClient.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    public final void set2ndPassword(final String secondPassword) {
        this.secondPassword = secondPassword;
    }

    public String get2ndPassword() {
        return secondPassword;
    }

    public boolean check2ndPassword(String secondPassword) {
        boolean allow = false;
        // Check if the passwords are correct here. :B
        if (checkHash(this.secondPassword, "SHA-1", secondPassword)) {
            allow = true;
        }
        return allow;
    }

    public void loadAccountData(int accountID) {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT id,name, password, gender, banned, gm, 2ndpassword, characterslots, tos FROM accounts WHERE id = ?");
            ps.setInt(1, accountID);
            rs = ps.executeQuery();
            if (rs.next()) {
                accountName = rs.getString("name");
                accId = rs.getInt("id");
                gmlevel = rs.getInt("gm");
                secondPassword = rs.getString("2ndpassword");
                gender = rs.getByte("gender");
                characterSlots = rs.getByte("characterslots");
                String passhash = rs.getString("password");

                ps.close();
                rs.close();

            }
        } catch (SQLException e) {
            FilePrinter.printError("MapleClient.txt", e);
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
                if (rs != null && !rs.isClosed()) {
                    rs.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    private static class CharNameAndId {

        public String name;
        public int id;

        public CharNameAndId(String name, int id) {
            super();
            this.name = name;
            this.id = id;
        }
    }

    public static boolean checkHash(String hash, String type, String password) {
        try {
            MessageDigest digester = MessageDigest.getInstance(type);
            digester.update(password.getBytes("UTF-8"), 0, password.length());
            return HexTool.toString(digester.digest()).replace(" ", "").toLowerCase().equals(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding the string failed", e);
        }
    }

    public short getCharacterSlots() {
        return characterSlots;
    }

    public boolean gainCharacterSlot() {
        if (characterSlots < 15) {
            Connection con = DatabaseConnection.getConnection();
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET characterslots = ? WHERE id = ?")) {
                    ps.setInt(1, this.characterSlots += 1);
                    ps.setInt(2, accId);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
            }
            return true;
        }
        return false;
    }

    public final byte getGReason() {
        final Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT `greason` FROM `accounts` WHERE id = ?");
            ps.setInt(1, accId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getByte("greason");
            }
        } catch (SQLException e) {
            FilePrinter.printError("MapleClient.txt", e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
            }
        }
        return 0;
    }
    
    

    public byte getGender() {
        return gender;
    }

    public void setGender(byte m) {
        this.gender = m;
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET gender = ? WHERE id = ?")) {
                ps.setByte(1, gender);
                ps.setInt(2, accId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
        }
    }

    public synchronized void announce(final byte[] packet) {//MINA CORE IS A FUCKING BITCH AND I HATE IT <3
        session.write(packet);
    }
    
    public boolean isDisconnection() {
        return disconnecting;
    }
    
    public boolean canClickNPC() {
        return lastNpcClick + 500 < System.currentTimeMillis();
    }

    public void setClickedNPC() {
        lastNpcClick = System.currentTimeMillis();
    }

    public void removeClickedNPC() {
        lastNpcClick = 0;
    }
    
}
