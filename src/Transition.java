public class Transition {
    float[] features;
    float[] nextFeatures;
    //Double target;
    float reward;
    Integer action;


    public Transition(float[] features, Integer action, float reward, float[] nextFeatures){
        this.features = features;
        this.action = action;
        this.reward = reward;
        this.nextFeatures = nextFeatures;
    }
}
