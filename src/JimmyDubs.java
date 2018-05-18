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
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;


public class JimmyDubs implements AIInterface {

	private Key inputKey;
	private boolean player;
	private CommandCenter cc;
	private Simulator simulator;
	private GameState state;

	double epsilon = 0.001;
	double decay = 1;
	double gamma = 0.95;//discount_factor
	double alpha = 0.02;
	double lambda = 0.1;

	private boolean train = true;
	private Train trainer = new Train(null);

	private boolean debug = false;

	private ActionObj current_action = new ActionObj(0, 0);

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

		this.gameData = gameData;
		this.player = player;
		this.inputKey = new Key();

		cc = new CommandCenter();
		frameData = new FrameData();

		simulator = gameData.getSimulator();

		myMotion = gameData.getMotionData(this.player);
		oppMotion = gameData.getMotionData(!this.player);

		String tempName = this.gameData.getCharacterName(this.player);

		state = new GameState(gameData, cc, player, tempName);

		this.myActions = new LinkedList<Action>();
		this.oppActions = new LinkedList<Action>();

		agent = new Agent(state, frameData, epsilon, gamma, alpha, lambda, player);
		try{
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

			state.updateState(cc, frameData, player);
			if(cc.getSkillFlag()){
				inputKey = cc.getSkillKey();
			}else {
				inputKey.empty();
				cc.skillCancel();

				myActions = getMyActions(frameData);

				myOrigHp = frameData.getCharacter(player).getHp();
				oppOrigHp = frameData.getCharacter(!player).getHp();

				double reward = agent.getScore(frameData, myOrigHp, oppOrigHp);

				FrameData frameDataAhead = simulator.simulate(frameData, this.player, null, null, 17);
				myNextActions = getMyActions(frameDataAhead);

				if(debug){}
				else{

					ActionObj next_action = agent.update(frameData, reward, current_action.getIndex());
					current_action = next_action;
				}

				cc.commandCall(state.myActions.get(current_action.getIndex()).name());
			}
		}
	}

	@Override
	public void roundEnd(int p1HP, int p2HP, int frames) {
		inputKey.empty();
		cc.skillCancel();

		if (p1HP < p2HP){
			trainer.setWinner(1);
		}
		else if (p1HP > p2HP){
			trainer.setWinner(2);
		}
		else{
			trainer.setWinner(0);
		}
	}

	@Override
	public void close() {
		System.out.println("Saved");
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

	public void loadReplay(){

		ObjectInputStream in;

		try{
			in = new ObjectInputStream(new FileInputStream("Replays/batch.ser"));

			Storage store = (Storage) in.readObject();

			for (int i = 0; i < store.size; i++){
				boolean[] arr = store.inputs.get(i);

				int action = store.actions.get(i);
				Double target = store.targets.get(i);

				Replay replay = new Replay(arr, target, action);

				agent.storage.add(replay);
			}
		}catch (IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
	}

	public void saveReplay(){
		Storage store = new Storage();

		for (int i = 0; i < agent.storage.size(); i++) {

			Replay r = agent.storage.get(i);

			store.inputs.add(r.inputs);
			store.actions.add(r.action);
			store.targets.add(r.target);
			store.size++;
		}
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("Replays/batch.ser"));

			out.writeObject(store);

			out.flush();
			out.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}
