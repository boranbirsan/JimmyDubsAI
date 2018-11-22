import java.io.Serializable;
import java.util.ArrayList;

public class Storage implements Serializable {
    ArrayList<float[]> features;
    ArrayList<Integer> actions;
    //ArrayList<Double> targets;
    ArrayList<Float> rewards;
    ArrayList<float[]> nextFeatures;
    int size = 0;

    public Storage() {
        super();
        features = new ArrayList<>();
        actions = new ArrayList<>();
        rewards = new ArrayList<>();
        nextFeatures = new ArrayList<>();
    }
}
