public class Replay {
    float[] features;
    Double target;
    Integer action;

    public Replay(float[] features, Double target, Integer action){
        this.features = features;
        this.target = target;
        this.action = action;
    }
}
