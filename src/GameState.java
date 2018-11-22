import aiinterface.CommandCenter;
import enumerate.Action;
import enumerate.State;
import struct.*;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

public class GameState {

    private int inputNum = 21;
    private int ouputNum = 40;

    private GameData gameData;

    private CommandCenter cc;

    private FrameData frameData;

    private boolean player;

    public Action[] totalActions;

    public String[] actions;

    public LinkedList<Action> myActions;
    public ArrayList<Integer> myActionIndex;

    public float[] previous_features;

    public GameState(GameData gd, CommandCenter cc, boolean player){
        this.gameData = gd;
        this.cc = cc;
        this.player = player;

        previous_features = new float[inputNum];

        actions = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C"};
//        myActions  = new LinkedList<>();
//        myActionIndex = new ArrayList<>();



//        actionAir = new Action[] { Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
//                Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB,
//                Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA, Action.AIR_D_DB_BB };
//        actionGround = new Action[] { Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
//                Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD, Action.CROUCH_GUARD, Action.THROW_A,
//                Action.THROW_B, Action.STAND_A, Action.STAND_B, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA,
//                Action.STAND_FB, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB,
//                Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_D_DB_BB };
//        spSkill = Action.STAND_D_DF_FC;
//
//        totalActions = new Action[]{ Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA,
//                Action.AIR_DB, Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB,
//                Action.AIR_D_DF_FA, Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB,
//                Action.AIR_D_DB_BA, Action.AIR_D_DB_BB, Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK,
//                Action.DASH, Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
//                Action.CROUCH_GUARD, Action.THROW_A, Action.THROW_B, Action.STAND_A, Action.STAND_B,
//                Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
//                Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
//                Action.STAND_F_D_DFB, Action.STAND_D_DB_BB, Action.STAND_D_DF_FC};
    }

    public void updateState(CommandCenter cc, FrameData frameData, boolean player){
        this.cc = cc;
        this.cc.setFrameData(frameData, player);
        this.frameData = frameData;
    }

    public float[] getFeatures(FrameData frameData){
        CharacterData myChar = frameData.getCharacter(player);
        CharacterData oppChar = frameData.getCharacter(!player);

        float[] features = new float[inputNum];
        int v = 0;

        boolean closeProjectile = false;

        int myProjectiles;
        int oppProjectile;

        if(player) {
            myProjectiles = frameData.getProjectilesByP1().size();
            oppProjectile = frameData.getProjectilesByP2().size();
        }else {
            myProjectiles = frameData.getProjectilesByP2().size();
            oppProjectile = frameData.getProjectilesByP1().size();
        }

        Deque<AttackData> attacks = frameData.getProjectiles();

        float myX = myChar.getCenterX();
        float oppX = oppChar.getCenterX();

        float myY = myChar.getCenterY();
        float oppY = oppChar.getCenterY();

        float distanceX = frameData.getDistanceX();
        float distanceY = frameData.getDistanceY();

        for(AttackData a: attacks){

            HitArea area = a.getCurrentHitArea();

            if(area.getLeft() < (myX + 100) || area.getRight() > (myX - 100)){
                closeProjectile = true;
            }
        }

        if(oppChar.getState().equals(State.STAND)) features[v] = 1;
        v++;
        if(oppChar.getState().equals(State.AIR)) features[v] = 1;
        v++;
        if(oppChar.getState().equals(State.CROUCH)) features[v] = 1;
        v++;

        features[v] = Math.abs(myChar.getHp()/400);
        v++;
        features[v] = Math.abs(oppChar.getHp()/400);
        v++;
        features[v] = (Math.abs(Math.abs(myX - 480)-480)/480);
        v++;
        features[v] = myY/640;
        v++;
        features[v] = (Math.abs(Math.abs(oppX - 480)-480)/480);
        v++;
        features[v] = oppY/640;
        v++;
        features[v] = Math.abs(distanceX-960)/960;
        v++;
        features[v] = Math.abs(distanceY/640);
        v++;
        features[v] = myChar.getSpeedY()/10;
        v++;
        features[v] = myChar.getSpeedX()/10;
        v++;
        features[v] = oppChar.getSpeedY()/10;
        v++;
        features[v] = oppChar.getSpeedX()/10;
        v++;
        features[v] = myChar.getEnergy()/100;
        v++;
        features[v] = oppChar.getEnergy()/100;
        v++;

        features[v] = myProjectiles/10;
        v++;
        features[v] = oppProjectile/10;
        v++;
        if(closeProjectile)features[v] = 1;
        else features[v] = 0;
        v++;

        if(oppChar.getAttack().getHitDamage() > 0) features[v] = 1;
        else features[v] = 0;
        v++;

//        features = new boolean[]{
//                oppChar.getState().equals(State.STAND),
//                oppChar.getState().equals(State.AIR),
//                oppChar.getState().equals(State.CROUCH),
//
//                // Edges
//                myX <= 180,
//                myX >= 700,
//
//                (myX > 180 && myX <= 260) || (myX <= 620 && myX < 700),
//                (myX > 260 && myX <= 360) || (myX <= 420 && myX < 620),
//                (myX > 360 && myX < 420),
//
//                myY >= 300 && myY < 350,
//                myY >= 250 && myY < 300,
//                myY >= 200 && myY < 250,
//                myY >= 150 && myY < 200,
//                myY >= 100 && myY < 150,
//                myY >= 50  && myY < 100,
//                myY <  50,
//
//                oppY >= 300 && oppY < 350,
//                oppY >= 250 && oppY < 300,
//                oppY >= 200 && oppY < 250,
//                oppY >= 150 && oppY < 200,
//                oppY >= 100 && oppY < 150,
//                oppY >= 50  && oppY < 100,
//                oppY <  50,
//
//                distanceX <= 60,
//                distanceX <= 100 && distanceX > 60,
//                distanceX <= 200 && distanceX > 100,
//                distanceX <= 300 && distanceX > 200,
//                distanceX > 300,
//
//                distanceY <= 50,
//                distanceY <= 100 && distanceY > 50,
//                distanceY <= 150 && distanceY > 100,
//                distanceY <= 200 && distanceY > 150,
//                distanceY <= 250 && distanceY > 200,
//                distanceY > 250,
//
//                closeProjectile,
//                myProjectiles >= 1,
//                oppProjectile >= 1,
//
//                // Falling
//                myY - myLastY > 0 && myX - myLastX == 0,
//                myY - myLastY > 0 && myX - myLastX > 0,
//                myY - myLastY > 0 && myX - myLastX < 0,
//
//                // Jumping
//                myY - myLastY < 0 && myX - myLastX == 0,
//                myY - myLastY < 0 && myX - myLastX > 0,
//                myY - myLastY < 0 && myX - myLastX < 0,
//
//                //Opponent Falling
//                oppY - oppLastY > 0 && oppX - oppLastX == 0,
//                oppY - oppLastY > 0 && oppX - oppLastX > 0,
//                oppY - oppLastY > 0 && oppX - oppLastX < 0,
//
//                //Opponent Jumping
//                oppY - oppLastY < 0 && oppX - oppLastX == 0,
//                oppY - oppLastY < 0 && oppX - oppLastX > 0,
//                oppY - oppLastY < 0 && oppX - oppLastX < 0
//        };

        return features;
    }

//    public void setPossibleActions(FrameData frameData){
//        myActions.clear();
//        myActionIndex.clear();
//
//        CharacterData myChar = frameData.getCharacter(player);
//        int energy = myChar.getEnergy();
//        ArrayList<MotionData> VM = gameData.getMotionData(player);
//
//        if(myChar.getState() == State.AIR){
//            for(int i = 0; i < actionAir.length; i++){
//                if (Math.abs(VM.get(actionAir[i].ordinal()).getAttackStartAddEnergy()) <= energy) {
//                    myActions.add(actionAir[i]);
//                    myActionIndex.add(i);
//                }
//            }
//        }else{
//            for(int i = 0; i < actionGround.length; i++){
//                if (Math.abs(VM.get(actionGround[i].ordinal()).getAttackStartAddEnergy())  <= energy) {
//                    myActions.add(actionGround[i]);
//                    myActionIndex.add(i + actionAir.length);
//                }
//            }
//
//            if (Math.abs(VM.get(totalActions[actionAir.length + actionGround.length].ordinal()).getAttackStartAddEnergy()) <= energy) {
//                myActions.add(spSkill);
//                myActionIndex.add(actionAir.length + actionGround.length);
//            }
//        }
//    }
}