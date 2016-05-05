/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package constants;

/**
 *
 * @author yuri
 */
public enum JobTypeMappingJob {
    ALL(-1,-1,"無"),
    BEGINNER(0, 0, "初心者"),
    WARRIOR(1, 1, "劍士"),
    MAGICIAN(2, 2, "法師"),
    BOWMAN(4, 3, "弓箭手"),
    THIEF(8, 4, "盜賊"),
    PIRATE(16, 5, "海盜");

    private int type = -1;
    private int job = -1;
    private String name = "";

    private JobTypeMappingJob(int type, int job, String name) {
        this.type = type;
        this.name = name;
        this.job = job;
    }

    private JobTypeMappingJob(int code) {
        this.type = code;
    }

    public int getType() {
        return type;
    }

    public int getJob() {
        return job;
    }

    public int getJob(int type) {
        for(JobTypeMappingJob Type : JobTypeMappingJob.values()){
            if(Type.getType()==type)
                return Type.getJob();
        }
        return -1;
    }
    
    public String getName() {
        return name;
    }
}
