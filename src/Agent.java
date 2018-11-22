import enumerate.Action;
import simulator.Simulator;
import struct.FrameData;

import java.util.*;

public class Agent {
    Simulator simulator;
    GameState state;
    double epsilon;
    float alpha;
    double lambda;
    float gamma;

    private Random rand = new Random();

    int outputNum = 12;
    int hiddenLayerNum = 17;
    int inputNum = 21;

    float lastValue;

    float[][] inputWeights;
    float[][] outputWeights;

    private float[] lastFeatures;
    private float[] features;
    private float[] nextFeatures;
    private int action;

    boolean player;

    FrameData frameData;

    LinkedList<Action> myAction;

    ArrayList<Transition> memory;
    int maxMemory = 500;
    int memorySize = 32;
    int memoryCount = 0;

    NeuralNet NN;

    public Agent(GameState state, FrameData fd, double epsilon, float gamma, float alpha, double lambda, boolean player) {
        this.state = state;
        this.frameData = fd;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.alpha = alpha;
        this.lambda = lambda;
        this.player = player;

        memory = new ArrayList<>();

        myAction = new LinkedList<>();

        NN = new NeuralNet(inputNum, hiddenLayerNum, outputNum, gamma, alpha);
    }

    public ActionObj getNextAction(FrameData fd, int myOrigHp, int oppOrigHp){

        features = state.getFeatures(fd);

        if(lastFeatures != null) {
            if(player){
                System.out.println("Batch update");
            }

            if(memory.size() >= maxMemory){

                if(memoryCount == maxMemory*2){
                    memoryCount = memoryCount - maxMemory;
                }

                memory.remove(memoryCount - maxMemory);
                memory.add(memoryCount - maxMemory, new Transition(lastFeatures, action, getScore(fd, myOrigHp, oppOrigHp), features));

                memoryCount++;

                updateFromBatch(memorySize);

            }else{
                memory.add(new Transition(lastFeatures, action, getScore(fd, myOrigHp, oppOrigHp), features));
                memoryCount++;

                updateFromBatch(memory.size());
            }
        }

        double random_act = rand.nextDouble();

        if (random_act <= epsilon){
            int randomIndex = rand.nextInt(state.actions.length);

            ActionObj action = new ActionObj(state.actions[randomIndex], randomIndex);

            return action;
        }

        float maxQ = -99999999;
        int chosenActIndex = 0;
        String actionValue = "";

        float[] output = NN.calculate(features);

        for(int i = 0; i < output.length; i++) {

            if (output[i] > maxQ) {
                maxQ = output[i];
                actionValue = state.actions[i];
                chosenActIndex = i;
            }

        }
        action = chosenActIndex;
        lastFeatures = features;
        return new ActionObj(actionValue, chosenActIndex);

    }


    public void updateFromBatch(int size){
        Random random = new Random();
        Set<Integer> intSet = new HashSet<>();

        while(intSet.size() < size){
            int rand = random.nextInt(memory.size());
            intSet.add(rand);
        }

        Iterator<Integer> iter = intSet.iterator();
        while(iter.hasNext()){
            Transition transition = memory.get(iter.next());

            NN.backProp(transition.features, transition.reward, transition.action, transition.nextFeatures);
        }
    }

    public void updateWeights(){
        NN.fixedInputWeights = NN.inputWeights;
        NN.fixedOutputWeights = NN.outputWeights;
    }

    public void setWeights(float[][] inputWeights, float[][] outputWeights){
        System.out.println("Setting Weights");
        this.inputWeights = inputWeights;
        this.outputWeights = outputWeights;
        NN.setWeights(inputWeights, outputWeights);
    }

    public void fillMemory(Storage store){
        for (int i = 0; i < store.size; i++) {
            float[] arr = store.features.get(i);
            float reward = store.rewards.get(i);
            int action = store.actions.get(i);
            float[] nextFeatures = store.nextFeatures.get(i);
            //Double target = store.targets.get(i);

            memory.add(new Transition(arr, action, reward, nextFeatures));
        }

        memoryCount = memory.size();
    }

    public int getScore(FrameData fd, int myOrigHp, int oppOrigHp) {
        int diffHpOp = Math.abs(fd.getCharacter(!player).getHp() - oppOrigHp);
        int diffHpMy = Math.abs(fd.getCharacter(player).getHp() - myOrigHp);

        if (diffHpMy == diffHpOp && diffHpMy != 0) {
            return -1;
        } else {
            return diffHpOp - diffHpMy;
        }
    }
}
