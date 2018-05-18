public class Replay {
    boolean[] inputs;
    Double target;
    Integer action;

    public Replay(boolean[] inputs, Double target, Integer action){
        this.inputs = inputs;
        this.target = target;
        this.action = action;
    }
}
