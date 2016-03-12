package server.movement;

import java.awt.Point;

public abstract class AbstractLifeMovement implements LifeMovement {

    private final Point position;
    private final int duration;
    private final int newstate;
    private final int newfh;
    private final int type;

    public AbstractLifeMovement(int type, Point position, int duration, int newstate, int newfh) {
        super();
        this.type = type;
        this.position = position;
        this.duration = duration;
        this.newstate = newstate;
        this.newfh = newfh;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public int getNewstate() {
        return newstate;
    }
    
    @Override
    public int getNewFh() {
        return newfh;
    }
 
    @Override
    public Point getPosition() {
        return position;
    }
}