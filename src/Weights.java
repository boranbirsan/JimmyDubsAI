import java.io.Serializable;

public class Weights implements Serializable{
    float[][] weights;

    public Weights(float[][] weights){
        this.weights = weights;
    }
}
