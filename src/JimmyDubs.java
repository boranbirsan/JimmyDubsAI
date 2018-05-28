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

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;


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

	String zenWeights = "data/aiData/zen_weights.ser";
	String garnetWeights = "data/aiData/garnet_weights.ser";
	String ludWeights = "data/aiData/lud_weights.ser";

	String zenMemory = "data/aiData/zen_batch.ser";
	String garnetMemory = "data/aiData/garnet_batch.ser";
	String ludMemory = "data/aiData/lud_batch.ser";

	private boolean debug = false;

	private ActionObj current_action = new ActionObj(0, 0);

	private int myOrigHp = 0, oppOrigHp = 0;

	private Agent agent;

	private GameData gameData;
	
	private FrameData frameData;

	private String charName;

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

		charName = this.gameData.getCharacterName(this.player);

		state = new GameState(gameData, cc, player);

		agent = new Agent(state, frameData, epsilon, gamma, alpha, lambda, player);

		if(charName.equals("ZEN")){
			loadWeights(zenWeights);
			loadReplay(zenMemory);
		}else if (charName.equals("Garnet")){
			loadWeights(garnetWeights);
			loadReplay(garnetMemory);
		}else if (charName.equals("LUD")){
			loadWeights(ludWeights);
			loadReplay(ludMemory);
		}else{
			loadWeights(zenWeights);
			loadReplay(zenMemory);
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

				myOrigHp = frameData.getCharacter(player).getHp();
				oppOrigHp = frameData.getCharacter(!player).getHp();

				double reward = agent.getScore(frameData, myOrigHp, oppOrigHp);

				FrameData frameDataAhead = simulator.simulate(frameData, this.player, null, null, 17);
				state.setPossibleActions(frameDataAhead);

				if(debug){}
				else{

					ActionObj next_action = agent.update(frameData, reward, current_action.getIndex());
					current_action = next_action;
				}

				cc.commandCall(state.totalActions[current_action.getIndex()].name());
			}
		}
	}

	@Override
	public void roundEnd(int p1HP, int p2HP, int frames) {
		inputKey.empty();
		cc.skillCancel();

		if(charName.equals("ZEN")){
			saveWeights(zenWeights);
			saveReplay(zenMemory);
		}else if(charName.equals("GARNET")){
			saveWeights(garnetWeights);
			saveReplay(garnetMemory);
		}else if(charName.equals("LUD")){
			saveWeights(ludWeights);
			saveReplay(ludMemory);
		}else{
			saveWeights(zenWeights);
			saveReplay(zenMemory);
		}
	}

	@Override
	public void close() {
		System.out.println("Saved");
	}

	public void loadReplay(String fileName){

		ObjectInputStream in;

		try{
			in = new ObjectInputStream(new FileInputStream(fileName));

			Storage store = (Storage) in.readObject();

			for (int i = 0; i < store.size; i++){
				boolean[] arr = store.inputs.get(i);

				int action = store.actions.get(i);
				Double target = store.targets.get(i);

				Replay replay = new Replay(arr, target, action);

				agent.memory.add(replay);
			}
		}catch (IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
	}

	public void saveReplay(String fileName){
		Storage store = new Storage();

		for (int i = 0; i < agent.memory.size(); i++) {

			Replay r = agent.memory.get(i);

			store.inputs.add(r.inputs);
			store.actions.add(r.action);
			store.targets.add(r.target);
			store.size++;
		}
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));

			out.writeObject(store);

			out.flush();
			out.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	public void loadWeights(String fileName) {
		Weights weights;
		Random rand = new Random();

		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));

			weights = (Weights) in.readObject();

			agent.updateWeights(weights.weights);

		}catch (IOException e){
			e.printStackTrace();

			float[][] newWeights = new float[agent.outputNum][agent.inputNum];

			for(int i = 0; i < agent.outputNum; i++){
				for(int j = 0; j < agent.inputNum; j++){
					newWeights[i][j] = rand.nextFloat();
				}
			}

			agent.updateWeights(newWeights);

		}catch (ClassNotFoundException e){
			e.printStackTrace();
		}
	}

	public void saveWeights(String fileName){
		Weights saveWeights = new Weights(agent.weights);

		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));

			out.writeObject(saveWeights);

			out.flush();
			out.close();
		}catch (IOException e){
				e.printStackTrace();
			}
	}
}
