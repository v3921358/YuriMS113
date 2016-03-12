package client.status;

import client.Skill;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import server.life.MobSkill;
import tools.ArrayMap;

public class MonsterStatusEffect {

    private Map<MonsterStatus, Integer> stati;
    private Skill skill;
    private MobSkill mobskill;
    private boolean monsterSkill;
    private ScheduledFuture<?> cancelTask;
    private ScheduledFuture<?> damageSchedule;

    public MonsterStatusEffect(Map<MonsterStatus, Integer> stati, Skill skillId, MobSkill mobskill, boolean monsterSkill) {
        this.stati = new ArrayMap<>(stati);
        this.skill = skillId;
        this.monsterSkill = monsterSkill;
        this.mobskill = mobskill;
    }

    public Map<MonsterStatus, Integer> getStati() {
        return stati;
    }

    public Integer setValue(MonsterStatus status, Integer newVal) {
        return stati.put(status, newVal);
    }

    public Skill getSkill() {
        return skill;
    }

    public boolean isMonsterSkill() {
        return monsterSkill;
    }

    public final void cancelTask() {
        if (cancelTask != null) {
            cancelTask.cancel(false);
        }
        cancelTask = null;
    }

    public ScheduledFuture<?> getCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(ScheduledFuture<?> cancelTask) {
        this.cancelTask = cancelTask;
    }

    public void removeActiveStatus(MonsterStatus stat) {
        stati.remove(stat);
    }

    public void setDamageSchedule(ScheduledFuture<?> damageSchedule) {
        this.damageSchedule = damageSchedule;
    }

    public void cancelDamageSchedule() {
        if (damageSchedule != null) {
            damageSchedule.cancel(false);
        }
    }

    public MobSkill getMobSkill() {
        return mobskill;
    }
}
