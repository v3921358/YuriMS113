package server.maps;

public abstract class AbstractAnimatedMapleMapObject extends AbstractMapleMapObject implements AnimatedMapleMapObject {

    private int stance;
    int fh;

    public int getStance() {
        return stance;
    }

    public int getNewFH() {
        return fh;
    }

    @Override
    public void setNewFH(int fh) {
        this.fh = fh;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

    public boolean isFacingLeft() {
        return Math.abs(stance) % 2 == 1;
    }
}
