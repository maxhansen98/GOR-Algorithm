import constants.Constants;
import utils.FileUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class CalcGOR_I {
    private SearchWindow window;

    public CalcGOR_I(String pathToModelFile) throws IOException {
        // temp init with the three sec types
        char[] secStructTypes = {'H', 'E', 'C'};
        this.window = new SearchWindow(pathToModelFile);
    }

    public HashMap<Character, Integer> calcStructureOccurrencies() {
        HashMap<Character, Integer> secSums = new HashMap<>();
        for (Character secStructType: this.window.getSecStructMatrices().keySet()){
            int[][] secMatrix = this.window.getSecStructMatrices().get(secStructType);
            int sum = calculateMatrixSum(secMatrix);
            secSums.put(secStructType, sum);
        }
        return secSums;
    }

    public int calculateMatrixSum(Integer[][] matrix) {
        int sum = 0;
        for (Integer[] row : matrix) {
            for (Integer value : row) {
                sum += value;
            }
        }
        return sum;
    }
    public ArrayList<Sequence> readFasta(String fasta) throws IOException {
        BufferedReader buff = new BufferedReader(new FileReader(fasta));
        String line;
        StringBuilder sequence = new StringBuilder();
        ArrayList<Sequence> sequencesToPredict = new ArrayList<>();
        String currentId = "";

        while ((line = buff.readLine()) != null) {
            if (line.startsWith(">")) {
                if (!currentId.isEmpty()) {
                    sequencesToPredict.add(new Sequence(currentId, sequence.toString(), ""));
                    sequence.setLength(0); // Clear sequence StringBuilder
                }
                currentId = line;
            } else {
                sequence.append(line);
            }
        }

        // Add the last sequence (if any)
        if (!currentId.isEmpty()) {
            sequencesToPredict.add(new Sequence(currentId, sequence.toString(), "--------"));
        }

        buff.close(); // Close the BufferedReader
        return sequencesToPredict;
    }


    public HashMap<Character, Double> sumAAsecStructFrequencies(char secType){
        HashMap<Character, Double> frequencies = new HashMap<>();
        return frequencies;
    }
}
