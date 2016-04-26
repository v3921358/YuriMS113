package server.expeditions;

import client.MapleCharacter;
import java.util.List;

public class MapleExpedition {

    private List<MapleCharacter> members;

    public MapleExpedition(MapleCharacter leader) {
        members.add(leader);
    }

    public void addMember(MapleCharacter chr) {
        members.add(chr);
    }

    public void removeMember(MapleCharacter chr) {
        members.remove(chr);
    }

    public List<MapleCharacter> getAllMembers() {
        return members;
    }
}