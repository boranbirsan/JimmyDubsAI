import aiinterface.CommandCenter;
import enumerate.Action;
import enumerate.State;
import struct.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

public class GameState {

    private GameData gameData;

    private CommandCenter cc;

    private FrameData frameData;

    private boolean player;

    private String myChar;

    private Action[] actionAir;
    private Action[] actionGround;
    private Action spSkill;

    public LinkedList<Action> myActions;
    public ArrayList<Integer> myActionIndex;

    private double myLastX =0;
    private double myLastY =0;
    private double oppLastX =0;
    private double oppLastY =0;

    public GameState(GameData gd, CommandCenter cc, boolean player, String myChar){
        this.gameData = gd;
        this.cc = cc;
        this.player = player;
        this.myChar = myChar;

        myActions  = new LinkedList<>();

        actionAir = new Action[] { Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
                Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB,
                Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA, Action.AIR_D_DB_BB };
        actionGround = new Action[] { Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
                Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD, Action.CROUCH_GUARD, Action.THROW_A,
                Action.THROW_B, Action.STAND_A, Action.STAND_B, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA,
                Action.STAND_FB, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB,
                Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_D_DB_BB };
        spSkill = Action.STAND_D_DF_FC;
    }

    public void updateState(CommandCenter cc, FrameData frameData, boolean player){
       this.cc.setFrameData(frameData, player);
       this.frameData = frameData;

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

    public void setPossibleActions(FrameData frameData){
        CharacterData myChar = frameData.getCharacter(player);
        if(myChar.getState() == State.AIR){
            for(int i = 0; i < actionAir.length; i++){
                myActions.add(actionAir[i]);
                myActionIndex.add(i);
            }
        }else{
            for(int i = 0; i < actionGround.length; i++){
                myActions.add(actionAir[i]);
                myActionIndex.add(i + actionAir.length);
            }
        }
    }
}