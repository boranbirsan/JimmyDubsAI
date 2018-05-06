public class GeneticProcreation {

    public static void updateGenetics(int population, int weightsNum, float[][] weights, float[] fitness, float cross, float mutation)
    {
        float total = 0;

        for(int i = 0; i < population; i++){
            total += fitness[i];
        }

        if (total == 0) return;

        float[][] newWeights = new float[population][weightsNum];

        for (int i = 0; i < population; i++){
            int p1 = 0, p2 = 0;

            float num = (float)Math.random() * total;

            for (int j = 0; j < population; j++){
                if (num <= fitness[j]){
                    p1 = j;
                    break;
                }
                num -= fitness[j];
            }

            num = (float)Math.random() * total;

            for (int j = 0; j < population; j++){
                if (num <= fitness[j]){
                    p2 = j;
                    break;
                }
                num -= fitness[j];
            }

            for(int j = 0; j < weightsNum; j++){
                newWeights[i][j] = weights[p1][j];
            }

            // CROSS
            if(Math.random() < cross){
                int pivot = (int)(Math.random() * weightsNum);
                if (pivot == weightsNum) pivot = 0;

                for(int j = pivot; j < weightsNum; j++){
                    newWeights[i][j] = weights[p2][j];
                }
            }

            // MUTATIONS
            for (int j = 0; j < weightsNum; j++){
                if (Math.random() < mutation){
                    newWeights[i][j] += Math.random();

                    while (newWeights[i][j] > 1) newWeights[i][j] -= 1;
                    while (newWeights[i][j] < -1) newWeights[i][j] += 1;
                }
            }
        }

        weights = newWeights;

        // return newWeights;
    }
}
