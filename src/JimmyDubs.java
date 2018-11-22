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

    double epsilon = 0.25;//random_rate
    float gamma = 0.1f;//discount_factor
    float alpha = 0.1f;//learning_rate
    double lambda = 0.1;

    private MotionData myMotion = new MotionData();

    private String zenWeights = "data/aiData/zen_weights.ser";
    private String garnetWeights = "data/aiData/garnet_weights.ser";
    private String ludWeights = "data/aiData/lud_weights.ser";

    private String zenMemory = "data/aiData/zen_batch.ser";
    private String garnetMemory = "data/aiData/garnet_batch.ser";
    private String ludMemory = "data/aiData/lud_batch.ser";

    private String zenWeights2 = "data/aiData/zen_weights2.ser";
    private String garnetWeights2 = "data/aiData/garnet_weights2.ser";
    private String ludWeights2 = "data/aiData/lud_weights2.ser";

    private String zenMemory2 = "data/aiData/zen_batch2.ser";
    private String garnetMemory2 = "data/aiData/garnet_batch2.ser";
    private String ludMemory2 = "data/aiData/lud_batch2.ser";

    private boolean debug = false;

    private ActionObj current_action = new ActionObj("5", 5);

    private int myOrigHp = 0, oppOrigHp = 0;

    private Agent agent;

    //private GameData gameData;

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

//        this.gameData = gameData;
        this.player = player;
        this.inputKey = new Key();

        cc = new CommandCenter();
        frameData = new FrameData();

        simulator = gameData.getSimulator();

        charName = gameData.getCharacterName(this.player);

        state = new GameState(gameData, cc, player);

        System.out.println("Init_ State");

        agent = new Agent(state, frameData, epsilon, gamma, alpha, lambda, player);

        if (charName.equals("ZEN") && player) {
            loadWeights(zenWeights);
            loadReplay(zenMemory);
        } else if (charName.equals("ZEN") && !player) {
            loadWeights(zenWeights2);
            loadReplay(zenMemory2);
        } else if (charName.equals("GARNET") && player) {
            loadWeights(garnetWeights);
            loadReplay(garnetMemory);
        } else if (charName.equals("GARNET") && !player) {
            loadWeights(garnetWeights2);
            loadReplay(garnetMemory2);
        } else if (charName.equals("LUD") && player) {
            loadWeights(ludWeights);
            loadReplay(ludMemory);
        } else if (charName.equals("LUD") && !player) {
            loadWeights(ludWeights2);
            loadReplay(ludMemory2);
        } else {
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
        if (!frameData.getEmptyFlag()) {

            state.updateState(cc, frameData, player);

            if (cc.getSkillFlag()) {
                inputKey = cc.getSkillKey();
            } else {
                inputKey.empty();
                cc.skillCancel();

			      myOrigHp = frameData.getCharacter(player).getHp();
			      oppOrigHp = frameData.getCharacter(!player).getHp();

//                FrameData frameDataAhead = simulator.simulate(frameData, this.player, null, null, 10);
//                state.setPossibleActions(frameData);

                if (debug) {
                } else {
                    if(player) {
                        System.out.println("Next Action");

                        System.out.println("Frame Number: " + frameData.getFramesNumber());
                    }

                    ActionObj next_action = agent.getNextAction(frameData, myOrigHp, oppOrigHp);

                    current_action = next_action;

                }

                if(player) {
                    int skillNo = current_action.getIndex();
                    System.out.println("Skill No: " + skillNo);
                    System.out.println("Using Skill: " + current_action.getAction());
                }

                cc.commandCall(current_action.getAction());
            }
        }
    }

    @Override
    public void roundEnd(int p1HP, int p2HP, int frames) {
        inputKey.empty();
        cc.skillCancel();

        agent.updateWeights();

        if (charName.equals("ZEN") && player) {
            saveWeights(zenWeights);
            saveReplay(zenMemory);
        } else if (charName.equals("ZEN") && !player) {
            saveWeights(zenWeights2);
            saveReplay(zenMemory2);
        } else if (charName.equals("GARNET") && player) {
            saveWeights(garnetWeights);
            saveReplay(garnetMemory);
        } else if (charName.equals("GARNET") && !player) {
            saveWeights(garnetWeights2);
            saveReplay(garnetMemory2);
        } else if (charName.equals("LUD") && player) {
            saveWeights(ludWeights);
            saveReplay(ludMemory);
        } else if (charName.equals("LUD") && !player) {
            saveWeights(ludWeights2);
            saveReplay(ludMemory2);
        }
    }

    @Override
    public void close() {
        System.out.println("Saved");
    }

    private void loadReplay(String fileName) {

        ObjectInputStream in;

        try {
            in = new ObjectInputStream(new FileInputStream(fileName));

            Storage store = (Storage) in.readObject();

            agent.fillMemory(store);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveReplay(String fileName) {
        Storage store = new Storage();
        Transition tran;

        for (int i = 0; i < agent.memory.size(); i++) {

            tran = agent.memory.get(i);

            store.features.add(tran.features);
            store.actions.add(tran.action);
            //store.targets.add(r.target);
            store.rewards.add(tran.reward);
            store.nextFeatures.add(tran.nextFeatures);
            store.size++;
        }

        save(fileName, store);
    }

    private void loadWeights(String fileName) {
        Weights weights;
        Random rand = new Random();

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));

            weights = (Weights) in.readObject();

            agent.setWeights(weights.inputWeights, weights.outputWeights);

        } catch (IOException | ClassNotFoundException e) {

            System.out.println("In CATCH");
            float[][] inputWeights = new float[agent.inputNum][agent.hiddenLayerNum];
            float[][] outputWeights = new float[agent.hiddenLayerNum][agent.outputNum];

            for (int i = 0; i < agent.hiddenLayerNum; i++) {
                System.out.println("In FOR LOOP");
                for (int j = 0; j < agent.inputNum; j++) {
                    inputWeights[j][i] = rand.nextFloat();
                }
                for (int j = 0; j < agent.outputNum; j++) {
                    outputWeights[i][j] = rand.nextFloat();
                }
            }

            agent.setWeights(inputWeights, outputWeights);

            //e.printStackTrace();

        }
    }

    private void saveWeights(String fileName) {
        Weights saveWeights = new Weights(agent.inputWeights, agent.outputWeights);

        save(fileName, saveWeights);
    }

    private void save(String fileName, Object obj) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));

            out.writeObject(obj);

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
