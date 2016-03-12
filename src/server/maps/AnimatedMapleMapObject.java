package server.maps;

public interface AnimatedMapleMapObject extends MapleMapObject {

    int getStance();

    void setStance(int stance);
    
    void setNewFH(int fh);

    boolean isFacingLeft();
}
