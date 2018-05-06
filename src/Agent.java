import enumerate.Action;
import simulator.Simulator;
import struct.FrameData;

import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;

public class Agent {
    Simulator simulator;
    GameState state;
    double epsilon;
    double alpha;
    double lambda;
    double discount_factor;

    int nextAction;
//    int max_batch_size = 10000;
//    int batch_update_size = 32;
    boolean player;

    FrameData frameData;

    public void Agent(GameState state, FrameData fd, double epsilon, double discount_factor, double alpha, double lambda, boolean player) {
        this.state = state;
        this.frameData = fd;
        this.discount_factor = discount_factor;
        this.epsilon = epsilon;
        this.alpha = alpha;
        this.lambda = lambda;
        this.player = player;
    }

    public int getNextAction(int current_action, LinkedList myActions){

        return 0;
    }

    public void update(FrameData fd,double reward, int current_action, LinkedList myActions){

        double td_target = reward + discount_factor;//*Next Q Value;
        double delta = td_target; // - This Q Value;
        double fact1 = alpha * delta;
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
