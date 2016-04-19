/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.wztosql;

import java.io.BufferedWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import server.quest.*;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleItemInformationProvider;
import tools.Pair;
import tools.DatabaseConnection;

/*
 * Quests Drop Creator : this basically updates your database with the correct quest IDs so quest items will drop only if the quest is active.
 * By FightDesign RaGEZONE / FXP
 */
public class QuestDropCreator {

    protected static String monsterQueryData = "drop_data"; // Modify this to suite your source
    protected static List<Pair<Integer, String>> itemNameCache = new ArrayList<Pair<Integer, String>>();
    protected static Map<Integer, Boolean> bossCache = new HashMap<Integer, Boolean>();
    private static MapleDataProvider questData;
    private static MapleData requirements;
    private static MapleData info;
    public static List<MapleQuest> quests = new ArrayList<>();
    public static List<Integer> itemIDs = new ArrayList<>();
    private static Connection con;

    public static int getItemAmountNeeded(short questid, int itemid) {
        MapleData data = null;
        try {
            data = requirements.getChildByPath(String.valueOf(questid)).getChildByPath("1");
        } catch (NullPointerException ex) {
            return 0;
        }
        if (data != null) {
            for (MapleData req : data.getChildren()) {
                MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(req.getName());
                if (!type.equals(MapleQuestRequirementType.ITEM)) {
                    continue;
                }

                for (MapleData d : req.getChildren()) {
                    if (MapleDataTool.getInt(d.getChildByPath("id"), 0) == itemid) {
                        return MapleDataTool.getInt(d.getChildByPath("count"), 0);
                    }
                }
            }
        }
        return 0;
    }

    public static boolean isQuestRequirement(int itemid) {
        for (MapleQuest quest : quests) {
            if (getItemAmountNeeded(quest.getId(), itemid) > 0) {
                return true;
            }
        }
        return false;
    }

    public static short getQuestID(int itemid) {
        for (MapleQuest quest : quests) {
            if (getItemAmountNeeded(quest.getId(), itemid) > 0) {
                return quest.getId();
            }
        }
        return 0;
    }

    public static void initializeMySQL() {
        DatabaseConnection.getConnection();
        con = DatabaseConnection.getConnection();
    }

    public static void loadQuests() {
        questData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Quest.wz"));
        requirements = questData.getData("Check.img");
        info = questData.getData("QuestInfo.img");

        
            for (MapleData quest : info.getChildren()) {
                quests.add(MapleQuest.getInstance(Integer.parseInt(quest.getName())));
            }

    }
    
    public static void loadQuestItems() {
        List<Pair<Integer, String>> items = MapleItemInformationProvider.getInstance().getAllItems();
        for (Pair<Integer, String> item : items) {
            int itemid = item.getLeft();
            if (!itemIDs.contains((Integer) itemid)) {
                if (isQuestRequirement(itemid)) {
                    itemIDs.add(itemid);
                    System.out.println(itemid);
                }
            }
        }
    }

    public static void dumpQuery() {
        String filename = "quest_drops.sql";
        try {
            File output = new File(filename);
            BufferedWriter bw = new BufferedWriter(new FileWriter(output));
            PrintWriter pw = new PrintWriter(bw);
            StringBuilder sb = new StringBuilder();
            String sql = "UPDATE drop_data SET questid = %d WHERE itemid = %d;";
            
            
            for (Integer itemid : itemIDs) {
                if (MapleItemInformationProvider.getInstance().isQuestItem(itemid)) {
                    int questId = getQuestID(itemid);
                    pw.write(String.format(sql,questId, itemid));
                    pw.write(" \r\n");
                    System.out.println("Item updated: " + itemid + " questId: " + questId);
                }
            }

            pw.write(sb.toString());
            pw.close();
            bw.close();
        } catch (IOException ioe) {
            System.out.println("Error writing to file: " + ioe.getLocalizedMessage());
        }
    }

    public static void main(String args[]) throws Exception {
        System.out.println("Quest Item Updator");
        System.out.println("...");
        //System.console().readLine();
        long timeStart = System.currentTimeMillis();
        System.out.println("Loading started.\r\n");

        System.out.println("Loading quests...");
        loadQuests();
        System.out.println("Loading quest items...");
        loadQuestItems();
        System.out.println("Initializing MySQL...");
        //dumpQuery();

        initializeMySQL();
        System.out.println("Loading info done.");
        dumpQuery();
        
        try {
            // Do this for reactors and monsters.
            PreparedStatement ps = con.prepareStatement("UPDATE drop_data SET questid = ? WHERE itemid = ?");
            PreparedStatement psr = con.prepareStatement("UPDATE reactordrops SET questid = ? WHERE itemid = ?");
            for (Integer itemid : itemIDs) {
                if (MapleItemInformationProvider.getInstance().isQuestItem(itemid)) {
                    int questId = getQuestID(itemid);
                    ps.setInt(1, questId);
                    ps.setInt(2, itemid);
                    psr.setInt(1, questId);
                    psr.setInt(2, itemid);
                    ps.executeUpdate();
                    psr.executeUpdate();
                    System.out.println("Item updated: " + itemid + " questId: " + questId);
                }
            }
            ps.close();
            psr.close();
        } catch (SQLException sqle) {
            System.out.println(sqle.getMessage());
        }
         
        long timeEnd = System.currentTimeMillis() - timeStart;

        System.out.println("Updating done in " + (int) (timeEnd / 1000) + " seconds.");
    }

}
