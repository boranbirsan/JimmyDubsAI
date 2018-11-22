public class NeuralNet {

    //ArrayList<float[][]> weights = new ArrayList<>();
    float[][] inputWeights;
    float[][] outputWeights;
    float[][] fixedInputWeights;
    float[][] fixedOutputWeights;

    private float[] nodes;
    private float[] output;

    private float gamma;//discount_factor
    private float alpha;//learning_rate

    public NeuralNet(int featureNum, int hiddenLayerNum, int outputNum, float gamma, float alpha){
        inputWeights = new float[featureNum][hiddenLayerNum];
        outputWeights = new float[hiddenLayerNum][outputNum];
        fixedInputWeights = new float[featureNum][hiddenLayerNum];
        fixedOutputWeights = new float[hiddenLayerNum][outputNum];
        nodes = new float[hiddenLayerNum];
        output = new float[outputNum];

        this.gamma = gamma;
        this.alpha = alpha;
    }

    public float[] calculate(float[] features){
        float sum = 0;
        for(int i = 0; i < inputWeights[0].length; i++){
            for(int j = 0; j < inputWeights.length; j++){
                sum = sum + features[j] * inputWeights[j][i];
            }
            nodes[i] = sum;
            sum = 0;
        }

        nodes = leakyRelu(nodes);

        for(int i = 0; i < outputWeights[0].length; i++){
            for(int j = 0; j < outputWeights.length; j++){
                sum = sum + nodes[j] * outputWeights[j][i];
            }
            output[i] = sum;
            sum = 0;
        }

        output = tanH(output);

        return output;
    }

    public float[] calculateTarget(float[] features){

        float sum = 0;
        float[] nodesTemp = new float[nodes.length];
        float[] outputTemp = new float[output.length];
        for(int i = 0; i < fixedInputWeights[0].length; i++){
            for(int j = 0; j < fixedInputWeights.length; j++){
                sum = sum + features[j] * fixedInputWeights[j][i];
            }
            nodesTemp[i] = sum;
            sum = 0;
        }

        nodesTemp = leakyRelu(nodesTemp);

        for(int i = 0; i < fixedOutputWeights[0].length; i++){
            for(int j = 0; j < fixedOutputWeights.length; j++){
                sum = sum + nodesTemp[j] * fixedOutputWeights[j][i];
            }
            outputTemp[i] = sum;
            sum = 0;
        }

        outputTemp = tanH(outputTemp);

        return outputTemp;
    }

    public float[] leakyRelu(float[] nodes){
        for(int i = 0; i < nodes.length; i++){
            if(nodes[i] < 0){
                nodes[i] = nodes[i] * 0.01f;
            }
        }

        return nodes;
    }

    public float[] tanH(float[] output){
        for(int i = 0; i < output.length; i++){
            output[i] = (float) ((1 - Math.pow(Math.E, -(2*output[i]))) / 1 + Math.pow(Math.E, -(2*output[i])));
        }
        return output;
    }

    public void backProp(float[] features, float reward, int action, float[] newFeatures){

        float[] output = calculateTarget(newFeatures);
        float max = 0;
        for(int i = 0; i < output.length; i++){
            if(output[i] > max) max = output[i];
        }

        float target = reward + (gamma * max);

        float out = calculate(features)[action];

//        float deltaError = (target - out) * (1-out*out);
        float deltaError = target - out;

        for(int i = 0; i < nodes.length; i++){
            outputWeights[i][action] = outputWeights[i][action] - (alpha * deltaError) * ((1-out*out)* nodes[i]);
//            outputWeights[i][action] = outputWeights[i][action] - alpha * deltaError;
        }

        for(int i = 0; i < nodes.length; i++){
            for(int j = 0; j < features.length; j++){
                inputWeights[j][i] = inputWeights[j][i] - (alpha * deltaError) * ((1-out*out) * inputWeights[i][action]);
//                inputWeights[j][i] = inputWeights[j][i] - alpha * deltaError;
            }
        }
    }

    public void setWeights(float[][] inputWeights, float[][] outputWeights){
        this.inputWeights = inputWeights;
        this.outputWeights = outputWeights;
        fixedInputWeights = inputWeights;
        fixedOutputWeights = outputWeights;
    }
}
