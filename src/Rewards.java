import java.io.Serializable;
import java.util.ArrayList;

public class Rewards implements Serializable {
    ArrayList<int[]> rewardsPerRound;
    ArrayList<Float> avgPerGame;

    public Rewards(){
        this.rewardsPerRound = new ArrayList<>();
        this.avgPerGame = new ArrayList<>();
    }
}
