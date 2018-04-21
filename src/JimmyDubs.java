import aiinterface.CommandCenter;
import aiinterface.AIInterface;

import enumerate.Action;
import enumerate.State;
import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.MotionData;
import struct.Key;
import sun.awt.image.ImageWatched;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;


public class JimmyDubs implements AIInterface {

	private Key inputKey;
	private boolean player;
	private CommandCenter cc;
	private Simulator simulator;

	private boolean debug = false;

	private int current_action = 0;

	private int myOrigHp = 0, oppOrigHp = 0;

	private Agent agent;

	private GameData gameData;
	
	private FrameData frameData;

	private LinkedList<Action> myActions;

	private LinkedList<Action> myNextActions;

	private LinkedList<Action> oppActions;

	private Action[] actionAir;

	private Action[] oppActionAir;

	private Action[] actionGround;

	private Action[] oppActionGround;

	private Action chosenAction;

	private Action spSkill;

	private CharacterData myCharacter;

	private CharacterData oppCharacter;

	private ArrayList<MotionData> myMotion;

	private ArrayList<MotionData> oppMotion;

	private CharacterName charName;

	@Override
	public void getInformation(FrameData frameData) {
		this.frameData = frameData;
		cc.setFrameData(this.frameData, player);
	}

	@Override
	public int initialize(GameData gameData, boolean player) {
		System.out.println("Initializing");

		this.player = player;
		this.inputKey = new Key();
		cc = new CommandCenter();
		frameData = new FrameData();
		this.gameData = gameData;

		simulator = gameData.getSimulator();

		actionAir = new Action[] { Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
				Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA, Action.AIR_D_DF_FB,
				Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA, Action.AIR_D_DB_BB };
		actionGround = new Action[] { Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
				Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD, Action.CROUCH_GUARD, Action.THROW_A,
				Action.THROW_B, Action.STAND_A, Action.STAND_B, Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA,
				Action.STAND_FB, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB,
				Action.STAND_F_D_DFA, Action.STAND_F_D_DFB, Action.STAND_D_DB_BB };
		spSkill = Action.STAND_D_DF_FC;

		myCharacter = frameData.getCharacter(player);
		myMotion = gameData.getMotionData(this.player);
		oppMotion = gameData.getMotionData(!this.player);

		String tempName = this.gameData.getCharacterName(this.player);
		if(tempName.equals("ZEN"))
			charName = CharacterName.ZEN;
		else if (tempName.equals("GARNET"))
			charName = CharacterName.GARNET;
		else if (tempName.equals("LUD"))
			charName = CharacterName.LUD;
		else
			charName = CharacterName.OTHER;

		myMotion = gameData.getMotionData(player);
		oppMotion = gameData.getMotionData(!player);
		this.myActions = new LinkedList<Action>();
		this.oppActions = new LinkedList<Action>();


		try{
			agent = new Agent();
			//TODO: Load the Action Weights
		}catch (Exception e){
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public Key input() {
		return inputKey;
	}

	@Override
	public void processing() {
		if(!frameData.getEmptyFlag() && frameData.getRemainingFramesNumber() > 0){
			if(cc.getSkillFlag()){
				inputKey = cc.getSkillKey();
			}else {
				inputKey.empty();
				cc.skillCancel();

				myActions = getMyActions(frameData);

				myOrigHp = frameData.getCharacter(player).getHp();
				oppOrigHp = frameData.getCharacter(!player).getHp();

				FrameData frameDataAhead = simulator.simulate(frameData, this.player, null, null, 17);
				myNextActions = getMyActions(frameDataAhead);

				if(debug){}
				else{

					int next_action = agent.update(frameData, current_action, myActions, myNextActions);
					current_action = next_action;

					chosenAction = myActions.get(current_action);
				}

				cc.commandCall(chosenAction.name());
			}
		}
	}

	@Override
	public void roundEnd(int p1HP, int p2HP, int frames) {
		inputKey.empty();
		cc.skillCancel();
	}

	@Override
	public void close() {
	}

	public LinkedList getMyActions(FrameData frameData) {
		int energy = myCharacter.getEnergy();
		LinkedList<Action> actions = new LinkedList();
		CharacterData character = frameData.getCharacter(player);

		if (character.getState() == State.AIR) {
			for (int i = 0; i < actionAir.length; i++) {
				if (Math.abs(myMotion.get(Action.valueOf(actionAir[i].name()).ordinal()).getAttackStartAddEnergy()) <= energy) {
					actions.add(actionAir[i]);
				}
			}
		} else {
			for (int i = 0; i < actionGround.length; i++) {
				if (Math.abs(myMotion.get(Action.valueOf(actionGround[i].name()).ordinal()).getAttackStartAddEnergy()) <= energy) {
					actions.add(actionGround[i]);
				}
			}
		}
		return actions;
	}
}
