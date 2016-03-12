package server.movement;

import java.awt.Point;
import tools.data.output.LittleEndianWriter;

public class RelativeLifeMovement extends AbstractLifeMovement {

 private short unk;

    public RelativeLifeMovement(int type, Point position, int duration, int newstate, int newfh) {
        super(type, position, duration, newstate, newfh);
    }

    public void setUnk(short unk) {
        this.unk = unk;
    }

    public short getUnk() {
        return unk;
    }


    @Override
    public void serialize(LittleEndianWriter lew) {
        lew.write(getType());
        lew.writeShort(getPosition().x);
        lew.writeShort(getPosition().y);
        if (getType() == 18 || getType() == 19) {
            lew.writeShort(unk);
        }
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
