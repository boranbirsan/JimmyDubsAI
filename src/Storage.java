import java.io.Serializable;
import java.util.ArrayList;

public class Storage implements Serializable {
    ArrayList<boolean[]> inputs;
    ArrayList<Double> targets;
    ArrayList<Integer> actions;
    int size = 0;

    public Storage() {
        super();
        inputs = new ArrayList<boolean[]>();
        targets = new ArrayList<Double>();
        actions = new ArrayList<Integer>();
    }

    public ArrayList<boolean[]> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<boolean[]> inputs) {
        this.inputs = inputs;
    }

    public ArrayList<Double> getTargets() {
        return targets;
    }

    public void setTargets(ArrayList<Double> targets) {
        this.targets = targets;
    }

    public ArrayList<Integer> getActs() {
        return actions;
    }

    public void setActs(ArrayList<Integer> actsions) {
        this.actions = actions;
    }
}
