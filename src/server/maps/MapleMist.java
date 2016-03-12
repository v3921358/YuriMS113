package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import java.awt.Point;
import java.awt.Rectangle;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MobSkill;
import tools.packets.MaplePacketCreator;

/**
 *
 * @author LaiLaiNoob
 */
public class MapleMist extends AbstractMapleMapObject {

    private Rectangle mistPosition;
    private MapleCharacter owner = null;
    private MapleMonster mob = null;
    private MapleStatEffect source;
    private MobSkill skill;
    private boolean isMobMist, isPoisonMist;
    private int skillDelay;

    public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
        this.mistPosition = mistPosition;
        this.mob = mob;
        this.skill = skill;
        isMobMist = true;
        isPoisonMist = true;
        skillDelay = 0;
    }

    public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
        this.mistPosition = mistPosition;
        this.owner = owner;
        this.source = source;
        this.skillDelay = 8;
        this.isMobMist = false;
        switch (source.getSourceId()) {
            case 4221006: // Smoke Screen
                isPoisonMist = false;
                break;
            case 2111003: // FP mist
            case 12111005: // Flame Gear
            case 14111006: // Poison Bomb
                isPoisonMist = true;
                break;
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
        return mistPosition.getLocation();
    }

    public Skill getSourceSkill() {
        return SkillFactory.getSkill(source.getSourceId());
    }

    public boolean isMobMist() {
        return isMobMist;
    }

    public boolean isPoisonMist() {
        return isPoisonMist;
    }

    public int getSkillDelay() {
        return skillDelay;
    }

    public MapleMonster getMobOwner() {
        return mob;
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public Rectangle getBox() {
        return mistPosition;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    public final byte[] makeDestroyData() {
        return MaplePacketCreator.removeMist(getObjectId());
    }

    public final byte[] makeSpawnData() {
        if (owner != null) {
            return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId())), this);
        }
        return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    public final byte[] makeFakeSpawnData(int level) {
        if (owner != null) {
            return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), level, this);
        }
        return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(makeSpawnData());
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(makeDestroyData());
    }

    public boolean makeChanceResult() {
        return source.makeChanceResult();
    }
}
