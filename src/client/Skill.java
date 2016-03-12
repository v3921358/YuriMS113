package client;

import java.util.ArrayList;
import java.util.List;
import server.MapleStatEffect;
import server.life.Element;

public class Skill {

    public int id;
    public List<MapleStatEffect> effects = new ArrayList<>();
    public Element element;
    public int animationTime;
    public boolean action;

    public Skill(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public MapleStatEffect getEffect(int level) {
        return effects.get(level - 1);
    }

    public int getMaxLevel() {
        return effects.size();
    }

    public boolean isFourthJob() {
        return (id / 10000) % 10 == 2;
    }

    public Element getElement() {
        return element;
    }

    public int getAnimationTime() {
        return animationTime;
    }

    public boolean isBeginnerSkill() {
        return id % 10000000 < 10000;
    }

    public boolean getAction() {
        return action;
    }
}
