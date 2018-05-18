public class Replay {
    boolean[] inputs;
    Double target;
    Integer action;

    public Replay(boolean[] inputs, Double target, Integer action){
        this.inputs = inputs;
        this.target = target;
        this.action = action;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public Double getTarget() {
        return target;
    }

    public void setTarget(Double target) {
        this.target = target;
    }
}
