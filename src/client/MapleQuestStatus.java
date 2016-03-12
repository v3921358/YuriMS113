package client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.quest.MapleQuest;
import tools.StringUtil;

public class MapleQuestStatus {

    public enum Status {

        UNDEFINED(-1),
        NOT_STARTED(0),
        STARTED(1),
        COMPLETED(2);
        final int status;

        private Status(int id) {
            status = id;
        }

        public int getId() {
            return status;
        }

        public static Status getById(int id) {
            for (Status l : Status.values()) {
                if (l.getId() == id) {
                    return l;
                }
            }
            return null;
        }
    }
    private MapleQuest quest;
    private Status status;
    private Map<Integer, String> progress = new LinkedHashMap<Integer, String>();
    private List<Integer> medalProgress = new LinkedList<Integer>();
    private int npc;
    private long completionTime;
    private int forfeited = 0;

    public MapleQuestStatus(MapleQuest quest, Status status) {
        this.quest = quest;
        this.setStatus(status);
        this.completionTime = System.currentTimeMillis();
        if (status == Status.STARTED) {
            registerMobs();
        }
    }

    public MapleQuestStatus(MapleQuest quest, Status status, int npc) {
        this.quest = quest;
        this.setStatus(status);
        this.setNpc(npc);
        this.completionTime = System.currentTimeMillis();
        if (status == Status.STARTED) {
            registerMobs();
        }
    }

    public MapleQuest getQuest() {
        return quest;
    }

    public Status getStatus() {
        return status;
    }

    public final void setStatus(Status status) {
        this.status = status;
    }

    public int getNpc() {
        return npc;
    }

    public final void setNpc(int npc) {
        this.npc = npc;
    }

    private void registerMobs() {
        for (int i : quest.getRelevantMobs()) {
            progress.put(i, "000");
        }
    }

    public boolean addMedalMap(int mapid) {
        if (medalProgress.contains(mapid)) {
            return false;
        }
        medalProgress.add(mapid);
        return true;
    }

    public int getMedalProgress() {
        return medalProgress.size();
    }

    public List<Integer> getMedalMaps() {
        return medalProgress;
    }

    public boolean progress(int id) {
        if (progress.get(id) != null) {
            int current = Integer.parseInt(progress.get(id));
            String str = StringUtil.getLeftPaddedStr(Integer.toString(current + 1), '0', 3);
            progress.put(id, str);
            return true;
        }
        return false;
    }

    public void setProgress(int id, String pr) {
        progress.put(id, pr);
    }

    public boolean madeProgress() {
        return progress.size() > 0;
    }

    public String getProgress(int id) {
        if (progress.get(id) == null) {
            return "";
        }
        return progress.get(id);
    }

    public Map<Integer, String> getProgress() {
        return Collections.unmodifiableMap(progress);
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public int getForfeited() {
        return forfeited;
    }

    public void setForfeited(int forfeited) {
        if (forfeited >= this.forfeited) {
            this.forfeited = forfeited;
        } else {
            throw new IllegalArgumentException("Can't set forfeits to something lower than before.");
        }
    }

    public String getQuestData() {
        StringBuilder str = new StringBuilder();
        for (String ps : progress.values()) {
            str.append(ps);
        }
        return str.toString();
    }
}
