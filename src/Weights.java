import java.io.Serializable;

public class Weights implements Serializable{
    float[][] inputWeights;
    float[][] outputWeights;

    public Weights(float[][] inputWeights, float[][] outputWeights){
        this.inputWeights = inputWeights;
        this.outputWeights = outputWeights;
    }
}
