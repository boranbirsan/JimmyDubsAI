import aiinterface.CommandCenter;
import aiinterface.AIInterface;

import simulator.Simulator;
import struct.*;

import java.io.*;
import java.util.Random;


public class JimmyDubs implements AIInterface {

	private Key inputKey;
	private boolean player;
	private CommandCenter cc;
	private Simulator simulator;
	private GameState state;

	private int rewardSum = 0;
	private int[] rewardPerRound = new int[3];

	private int roundNum = 0;

	double epsilon = 0.25;
	double gamma = 0.1;//discount_factor
	double alpha = 0.1;
	double lambda = 0.1;

	MotionData myMotion = new MotionData();

	int count = 0;

	String zenWeights = "data/aiData/zen_weights5.ser";
	String garnetWeights = "data/aiData/garnet_weights5.ser";
	String ludWeights = "data/aiData/lud_weights5.ser";

	String zenMemory = "data/aiData/zen_batch5.ser";
	String garnetMemory = "data/aiData/garnet_batch5.ser";
	String ludMemory = "data/aiData/lud_batch5.ser";

	String zenWeights2 = "data/aiData/zen_weights6.ser";
	String garnetWeights2 = "data/aiData/garnet_weights6.ser";
	String ludWeights2 = "data/aiData/lud_weights6.ser";

	String zenMemory2 = "data/aiData/zen_batch6.ser";
	String garnetMemory2 = "data/aiData/garnet_batch6.ser";
	String ludMemory2 = "data/aiData/lud_batch6.ser";

	String rewardFile = "data/aiData/rewards.ser";

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

		System.out.println("Init_ State");

		agent = new Agent(state, frameData, epsilon, gamma, alpha, lambda, player);

		if(charName.equals("ZEN") && player) {
			loadWeights(zenWeights);
			loadReplay(zenMemory);
		}else if(charName.equals("ZEN") && !player){
			loadWeights(zenWeights2);
			loadReplay(zenMemory2);
		}else if (charName.equals("GARNET") && player) {
			loadWeights(garnetWeights);
			loadReplay(garnetMemory);
		}else if (charName.equals("GARNET") && !player){
			loadWeights(garnetWeights2);
			loadReplay(garnetMemory2);
		}else if (charName.equals("LUD") && player){
			loadWeights(ludWeights);
			loadReplay(ludMemory);
		}else if (charName.equals("LUD") && !player){
			loadWeights(ludWeights2);
			loadReplay(ludMemory2);
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
		if(!frameData.getEmptyFlag()){

			state.updateState(cc, frameData, player);
            //System.out.println("State Update");

            count++;
			if(cc.getSkillFlag()){
				inputKey = cc.getSkillKey();
			}else {
				inputKey.empty();
				cc.skillCancel();

				if(count >= myMotion.getFrameNumber()) {

					count = 0;

					double reward = agent.getScore(frameData, myOrigHp, oppOrigHp);

					myOrigHp = frameData.getCharacter(player).getHp();
					oppOrigHp = frameData.getCharacter(!player).getHp();

					//FrameData frameDataAhead = simulator.simulate(frameData, this.player, null, null, 10);
					state.setPossibleActions(frameData);

					if (debug) {
					} else {
						System.out.println("Next Action");

						System.out.println("Frame Number: " + frameData.getFramesNumber());

						ActionObj next_action = agent.update(frameData, reward, current_action.getIndex());
						current_action = next_action;

						rewardSum += reward;
					}

					int skillNo = current_action.getIndex();
					System.out.println("Skill No: " + skillNo);

					cc.commandCall(state.totalActions[current_action.getIndex()].name());

					myMotion = gameData.getMotionData(player).get(state.totalActions[current_action.getIndex()].ordinal());

					if(skillNo == 20 || skillNo == 21 || skillNo == 22){
						System.out.println("Using: " + state.totalActions[current_action.getIndex()].name());
						System.out.println("Frames: " + myMotion.getFrameNumber());
					}

					System.out.println("Using Skill: " + state.totalActions[current_action.getIndex()].name());

				}else{
					//System.out.println("In Action: " + state.totalActions[current_action.getIndex()].name());
				}
			}
		}
	}

	@Override
	public void roundEnd(int p1HP, int p2HP, int frames) {
		inputKey.empty();
		cc.skillCancel();

		if(charName.equals("ZEN") && player) {
			saveWeights(zenWeights);
			saveReplay(zenMemory);
		}else if(charName.equals("ZEN") && !player){
			saveWeights(zenWeights2);
			saveReplay(zenMemory2);
		}else if (charName.equals("GARNET") && player) {
			saveWeights(garnetWeights);
			saveReplay(garnetMemory);
		}else if (charName.equals("GARNET") && !player){
			saveWeights(garnetWeights2);
			saveReplay(garnetMemory2);
		}else if (charName.equals("LUD") && player){
			saveWeights(ludWeights);
			saveReplay(ludMemory);
		}else if (charName.equals("LUD") && !player){
			saveWeights(ludWeights2);
			saveReplay(ludMemory2);
		}

		rewardPerRound[roundNum] = rewardSum;

		roundNum++;
	}

	@Override
	public void close() {
		saveRewards(rewardFile);
		System.out.println("Saved");
	}

	public void loadReplay(String fileName){

		ObjectInputStream in;

		try{
			in = new ObjectInputStream(new FileInputStream(fileName));

			Storage store = (Storage) in.readObject();

			for (int i = 0; i < store.size; i++){
				float[] arr = store.features.get(i);

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

			store.features.add(r.features);
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

			agent.setWeights(weights.weights);

		}catch (IOException e){

			float[][] newWeights = new float[agent.outputNum][agent.inputNum];

//			for(int i = 0; i < agent.outputNum; i++){
//				for(int j = 0; j < agent.inputNum; j++){
//					//newWeights[i][j] = rand.nextFloat();
//				}
//			}

			agent.setWeights(newWeights);
			System.out.println(newWeights);

			e.printStackTrace();

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

	public void saveRewards(String fileName){

		Rewards rewards = new Rewards();

		rewards.rewardsPerRound.add(rewardPerRound);

		float avg;
		int sum = 0;

		for(int i = 0; i < rewardPerRound.length; i++){
			sum += rewardPerRound[i];
		}

		avg = sum/3;

		rewards.avgPerGame.add(avg);

		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));

			out.writeObject(rewards);

			out.flush();
			out.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}
