import enumerate.Action;
import simulator.Simulator;
import struct.FrameData;

import java.util.*;

public class Agent {
    Simulator simulator;
    GameState state;
    double epsilon;
    double alpha;
    double lambda;
    double discount_factor;

    Random rand = new Random();

    int outputNum = 40;
    int inputNum = 49;

    float lastValue;

    float[][] weights;
    boolean[] inputs;

    int nextAction;

    boolean player;

    FrameData frameData;

    ArrayList<Replay> memory;
    int maxMemory = 1000;
    int memorySize = 32;

    public Agent(GameState state, FrameData fd, double epsilon, double discount_factor, double alpha, double lambda, boolean player) {
        this.state = state;
        this.frameData = fd;
        this.discount_factor = discount_factor;
        this.epsilon = epsilon;
        this.alpha = alpha;
        this.lambda = lambda;
        this.player = player;
    }

    public ActionObj getNextAction(){
        int index = 0;

        double random_act = rand.nextDouble();

        if (random_act <= epsilon){
            int max = state.myActionIndex.size();
            int randomIndex = rand.nextInt(max);

            float q = getExpectation(weights[state.myActionIndex.get(randomIndex)]);

            ActionObj action = new ActionObj(q, randomIndex);
            return action;
        }

        float maxQ = -99999999;
        int chosenAct = 0;
        int chosenActIndex = 0;

        for(int i = 0; i < state.myActionIndex.size(); i++){

            float q = getExpectation(weights[state.myActionIndex.get(i)]);

            if(q > maxQ){
                chosenActIndex = state.myActionIndex.get(i);
                maxQ = q;
            }
        }
        return new ActionObj(maxQ, chosenActIndex);
    }

    public ActionObj update(FrameData fd,double reward, int action_index){

        inputs = state.getInputs(fd);

        ActionObj action = getNextAction();

        int activeInputs = 0;
        for (int i = 0; i < inputs.length; i++){
            if(inputs[i]){
                activeInputs++;
            }
        }

        double new_alpha = 0;
        if (activeInputs == 0 ){
            new_alpha = alpha;
        }else{
            new_alpha = alpha / activeInputs;
        }

        double td_target = reward + discount_factor*action.getValue();
        double delta = td_target - lastValue;
        double fact1 = new_alpha * delta;

        updateWeights(weights[action_index], inputs, fact1);

        if(memory.size() >= maxMemory){

            updateFromBatch(memorySize);
        }

        memory.add(new Replay(inputs, fact1, action.getIndex()));

        lastValue = action.getValue();
        return action;
    }

    public void updateWeights(float[][] weights){
        this.weights = weights;
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
            Replay replay = memory.get(iter.next());
            float[] weight = weights[replay.action];
            updateWeights(weight, replay.inputs, replay.target);
        }
    }

    public void updateWeights(float[] weights, boolean[] inputs, double a){
        for(int i = 0; i < weights.length; i++){
            if(inputs[i]) {
                weights[i] += (weights[i] + a);
            }
        }
    }

    public void updateInputs(boolean[] inputs){
        this.inputs = inputs;
    }

    public float getExpectation(float[] weights){
        float sum = 0;

        for (int i = 0; i < weights.length; i++){
            if(inputs[i]){
                sum += weights[i];
            }
        }
        return sum;
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
