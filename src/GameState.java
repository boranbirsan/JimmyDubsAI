import enumerate.State;
import struct.*;

import java.util.Deque;

public class GameState {

    double myLastX =0;
    double myLastY =0;
    double oppLastX =0;
    double oppLastY =0;

    public GameState(){

    }

    public boolean[] getInputs(FrameData frameData){
        CharacterData myChar = frameData.getCharacter(true);
        CharacterData oppChar = frameData.getCharacter(false);

        boolean[] inputs;

        boolean closeProjectile = false;

        int myProjectiles = 0;
        int oppProjectile = 0;

        Deque<AttackData> attacks = frameData.getProjectiles();

        double myX = myChar.getCenterX();
        double oppX = oppChar.getCenterX();

        double myY = myChar.getCenterY();
        double oppY = oppChar.getCenterY();

        double distanceX = frameData.getDistanceX();
        double distanceY = frameData.getDistanceY();

        for(AttackData a: attacks){
            if(a.isPlayerNumber()){
                myProjectiles++;
            }else{
                oppProjectile++;
            }

            HitArea area = a.getCurrentHitArea();

            if(area.getLeft() < (myX + 100) || area.getRight() > (myX - 100)){
                closeProjectile = true;
            }
        }

        inputs = new boolean[]{
                !oppChar.getState().equals(State.AIR) && !oppChar.getState().equals(State.DOWN),
                oppChar.getState().equals(State.AIR),
                oppChar.getState().equals(State.DOWN),

                // Edges
                myX <= 180,
                myX >= 700,

                (myX > 180 && myX <= 260) || (myX <= 620 && myX < 700),
                (myX > 260 && myX <= 360) || (myX <= 420 && myX < 620),
                (myX > 360 && myX < 420),

                myY >= 300 && myY < 350,
                myY >= 250 && myY < 300,
                myY >= 200 && myY < 250,
                myY >= 150 && myY < 200,
                myY >= 100 && myY < 150,
                myY >= 50  && myY < 100,
                myY <  50,

                oppY >= 300 && oppY < 350,
                oppY >= 250 && oppY < 300,
                oppY >= 200 && oppY < 250,
                oppY >= 150 && oppY < 200,
                oppY >= 100 && oppY < 150,
                oppY >= 50  && oppY < 100,
                oppY <  50,

                distanceX <= 60,
                distanceX <= 100 && distanceX > 60,
                distanceX <= 200 && distanceX > 100,
                distanceX <= 300 && distanceX > 200,
                distanceX > 300,

                distanceY <= 50,
                distanceY <= 100 && distanceY > 50,
                distanceY <= 150 && distanceY > 100,
                distanceY <= 200 && distanceY > 150,
                distanceY <= 250 && distanceY > 200,
                distanceY > 250,

                closeProjectile,
                myProjectiles >= 1,
                oppProjectile >= 1,

                // Falling
                myY - myLastY > 0 && myX - myLastX == 0,
                myY - myLastY > 0 && myX - myLastX > 0,
                myY - myLastY > 0 && myX - myLastX < 0,

                // Jumping
                myY - myLastY < 0 && myX - myLastX == 0,
                myY - myLastY < 0 && myX - myLastX > 0,
                myY - myLastY < 0 && myX - myLastX < 0,

                //Opponent Falling
                oppY - oppLastY > 0 && oppX - oppLastX == 0,
                oppY - oppLastY > 0 && oppX - oppLastX > 0,
                oppY - oppLastY > 0 && oppX - oppLastX < 0,

                //Opponent Jumping
                oppY - oppLastY < 0 && oppX - oppLastX == 0,
                oppY - oppLastY < 0 && oppX - oppLastX > 0,
                oppY - oppLastY < 0 && oppX - oppLastX < 0
        };

        return inputs;
    }
}
