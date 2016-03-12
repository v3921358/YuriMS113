package server.maps;

public class ReactorDropEntry {

    public ReactorDropEntry(int itemId, int chance, int questId) {
        this.itemId = itemId;
        this.chance = chance;
        this.questid = questId;
    }
    public int itemId, chance, questid;
    public int assignedRangeStart, assignedRangeLength;
}
