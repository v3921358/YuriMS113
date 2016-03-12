package server.movement;

import java.awt.Point;

public interface LifeMovement extends LifeMovementFragment {

    int getNewstate();

    int getDuration();

    int getType();

    int getNewFh();
}
