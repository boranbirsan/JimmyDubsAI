import java.io.Serializable;
import java.util.ArrayList;

public class Storage implements Serializable {
    ArrayList<boolean[]> inputs;
    ArrayList<Double> targets;
    ArrayList<Integer> actions;
    int size = 0;

    public Storage() {
        super();
        inputs = new ArrayList<>();
        targets = new ArrayList<>();
        actions = new ArrayList<>();
    }
}
