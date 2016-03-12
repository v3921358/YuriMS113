package server.maps;

import java.awt.Point;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import tools.packets.MaplePacketCreator;

public class MapleSummon extends AbstractAnimatedMapleMapObject {

    private MapleCharacter owner;
    private byte skillLevel;
    private int skill, hp;
    private SummonMovementType movementType;

    public MapleSummon(MapleCharacter owner, int skill, Point pos, SummonMovementType movementType) {
        this.owner = owner;
        this.skill = skill;
        this.skillLevel = owner.getSkillLevel(SkillFactory.getSkill(skill));
        if (skillLevel == 0) {
            throw new RuntimeException();
        }

        this.movementType = movementType;
        setPosition(pos);
    }

    public void sendSpawnData(MapleClient client) {
        if (this != null) {
            client.announce(MaplePacketCreator.spawnSummon(this, false));
        }

    }

    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.removeSummon(this, true));
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public int getSkill() {
        return skill;
    }

    public int getHP() {
        return hp;
    }

    public void addHP(int delta) {
        this.hp += delta;
    }

    public SummonMovementType getMovementType() {
        return movementType;
    }

    public boolean isStationary() {
        return (skill == 3111002 || skill == 3211002 || skill == 5211001 || skill == 13111004);
    }

    public byte getSkillLevel() {
        return skillLevel;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }

    public final boolean isPuppet() {
        switch (skill) {
            case 3111002:
            case 3211002:
            case 13111004:
                return true;
        }
        return false;
    }
}
