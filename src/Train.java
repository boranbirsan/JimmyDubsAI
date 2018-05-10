import java.io.PrintWriter;

public class Train {

    private int population = 25;

    // TEMP NUMBERS ---------------------------------------
    private int inputNum = 64;
    private int hiddenNum = 64;
    private int outputNum = 56;
    // ---------------------------------------------------

    private int weightsNum;

    private float[][] weights;

    private float[] fitness;

    private int currentIndividual = 0;
    private int otherIndividual = 1;

    private int generation = 1;

    private PrintWriter log;

    public Train(String weightsFileName){

        weightsNum = inputNum*hiddenNum + hiddenNum*outputNum;

        weights = new float[population][weightsNum];

        if(weightsFileName == null){
            for(int i = 0; i < population; i++){
                for(int j = 0; j < weightsNum; j++){
                    weights[i][j] = (float) Math.random()*2 - 1;
                }
            }
        }else{
            // TODO: Read Weights File
        }
        try {
            log = new PrintWriter("log", "UTF-8");
        }catch(Exception e){
            e.printStackTrace();
        }

        fitness = new float[population];
    }

    public void setWinner(int winner){
        switch(winner)
        {
            case 0: break;
            case 1: fitness[currentIndividual] += 1; otherIndividual++; break;
            case 2: fitness[otherIndividual] += 1; otherIndividual++; break;
        }

        if(otherIndividual == population){
            currentIndividual++;
            otherIndividual = currentIndividual + 1;
        }

        if (currentIndividual == population){
            otherIndividual = 1;
            currentIndividual = 0;

            float maxFitness = 0;
            int bestID = 0;
            for (int i = 0; i < population; i++){
                if(fitness[i] > maxFitness){
                    maxFitness = fitness[i];
                    bestID = i;
                }
            }

            for (int i = 0; i < weightsNum; i++){
                log.print((int)(weights[bestID][i]*256) + " ");
            }
            log.print("\n");
            log.flush();

            System.out.println("Generation "+ generation + " max fitness: " + maxFitness );

            // UPDATE WEIGHTS

            //weights = GeneticProcreation.updateGenetics(population, weightsNum, weights, fitness, .7f, .001f);
            GeneticProcreation.updateGenetics(population, weightsNum, weights, fitness, .7f, .001f);
        }
    }
}