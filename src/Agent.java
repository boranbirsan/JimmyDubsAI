import simulator.Simulator;
import struct.FrameData;

import java.util.LinkedList;

public class Agent {
    Simulator simulator;
    double epsilon;
    double alpha;
    double lambda;
    double discount_factor;
//    int max_batch_size = 10000;
//    int batch_update_size = 32;
    boolean player;

    public void Agent(double epsilon, double discount_factor, double alpha, double lambda, boolean player) {
        this.discount_factor = discount_factor;
        this.epsilon = epsilon;
        this.alpha = alpha;
        this.lambda = lambda;
        this.player = player;
    }

    public int getNextAction(){
        return 0;
    }

    public int update(FrameData fd, int current_action, LinkedList myActions, LinkedList myNextActions){
        return 0;
    }

    public double calculateQ(){
        return 0;
    }

    public int getScore(FrameData fd, int myOrigHp, int oppOrigHp) {
        int diffHpOp = Math.abs(fd.getCharacter(false).getHp() - oppOrigHp);
        int diffHpMy = Math.abs(fd.getCharacter(true).getHp() - myOrigHp);

        if (diffHpMy == diffHpOp && diffHpMy != 0) {
            return -1;
        } else {
            return diffHpOp - diffHpMy;
        }
    }
}
