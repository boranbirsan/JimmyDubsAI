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
    int inputNum = 21;

    float lastValue;

    float[][] weights;
    float[] features;

    boolean player;

    FrameData frameData;

    LinkedList<Action> myAction;

    ArrayList<Replay> memory;
    int maxMemory = 5000;
    int memorySize = 32;

    public Agent(GameState state, FrameData fd, double epsilon, double discount_factor, double alpha, double lambda, boolean player) {
        this.state = state;
        this.frameData = fd;
        this.discount_factor = discount_factor;
        this.epsilon = epsilon;
        this.alpha = alpha;
        this.lambda = lambda;
        this.player = player;

        memory = new ArrayList<>();

        myAction = new LinkedList<>();
    }

    public ActionObj getNextAction(){

        myAction.clear();
        double random_act = rand.nextDouble();

        if (random_act <= epsilon){
            int max = state.myActionIndex.size();
            int randomIndex = rand.nextInt(max);

            float q = getExpectation(weights[state.myActionIndex.get(randomIndex)]);

            myAction.add(state.totalActions[randomIndex]);

            ActionObj action = new ActionObj(q, randomIndex);
            return action;
        }

        float maxQ = -99999999;
        int chosenActIndex = 0;

        for(int i = 0; i < state.myActionIndex.size(); i++){

            myAction.clear();

            float q = getExpectation(weights[state.myActionIndex.get(i)]);

            if(q > maxQ){
                chosenActIndex = state.myActionIndex.get(i);
                maxQ = q;

                myAction.add(state.totalActions[chosenActIndex]);
            }
        }
        return new ActionObj(maxQ, chosenActIndex);
    }

    public ActionObj update(FrameData fd, double reward, int action_index){

        features = state.getFeatures(fd);

        ActionObj action = getNextAction();

//        int activeInputs = 0;
//        for (int i = 0; i < features.length; i++){
//            if(features[i]){
//                activeInputs++;
//            }
//        }

//        double new_alpha;
//        if (activeInputs == 0 ){
//            new_alpha = alpha;
//        }else{
//            new_alpha = alpha / activeInputs;
//        }

        double td_target = reward + discount_factor*action.getValue();
        double correction = td_target - lastValue;
        double fact1 = alpha * correction;

        updateWeights(weights[action_index], state.previous_features, fact1);

        if(memory.size() >= maxMemory){

            updateFromBatch(memorySize);
        }

        memory.add(new Replay(features, fact1, action.getIndex()));

        if(player) {
            System.out.println("[");
            float[] debugWeights = weights[action_index];
            for(int i = 0; i < weights[action_index].length; i++){
                System.out.println(" " + debugWeights[i]);
            }
            System.out.println("]");
        }

        state.previous_features = features;
        lastValue = action.getValue();
        return action;
    }

    public void setWeights(float[][] weights){
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
            updateWeights(weight, replay.features, replay.target);
        }
    }

    public void updateWeights(float[] weights, float[] features, double a){
        for(int i = 0; i < features.length; i++){
            weights[i] += a * features[i];
        }
    }

    public void setFeatures(float[] features){
        this.features = features;
    }

    public float getExpectation(float[] weights){
        float sum = 0;

        for (int i = 0; i < features.length; i++){
            sum += weights[i] * features[i];
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
