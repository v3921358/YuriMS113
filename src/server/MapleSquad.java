package server;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;
import tools.packets.MaplePacketCreator;

public class MapleSquad {

    private MapleCharacter leader;
    private List<MapleCharacter> members = new LinkedList<>();
    private List<MapleCharacter> bannedMembers = new LinkedList<>();
    private int ch;
    private int status = 0;

    public MapleSquad(int ch, MapleCharacter leader) {
        this.leader = leader;
        this.members.add(leader);
        this.ch = ch;
        this.status = 1;
    }

    public MapleCharacter getLeader() {
        return leader;
    }

    public boolean containsMember(MapleCharacter member) {
        for (MapleCharacter mmbr : members) {
            if (mmbr.getId() == member.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBanned(MapleCharacter member) {
        for (MapleCharacter banned : bannedMembers) {
            if (banned.getId() == member.getId()) {
                return true;
            }
        }
        return false;
    }

    public List<MapleCharacter> getMembers() {
        return members;
    }

    public int getSquadSize() {
        return members.size();
    }

    public boolean addMember(MapleCharacter member) {
        if (isBanned(member)) {
            return false;
        } else {
            members.add(member);
            getLeader().getClient().announce(MaplePacketCreator.broadcastMsg(5, member.getName() + " 已經加入的遠征隊！"));
            return true;
        }
    }

    public void banMember(MapleCharacter member, boolean ban) {
        int index = -1;
        for (MapleCharacter mmbr : members) {
            if (mmbr.getId() == member.getId()) {
                index = members.indexOf(mmbr);
            }
        }
        members.remove(index);
        if (ban) {
            bannedMembers.add(member);
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public boolean equals(MapleSquad other) {
        if (other.ch == ch) {
            if (other.leader.getId() == leader.getId()) {
                return true;
            }
        }
        return false;
    }
}
