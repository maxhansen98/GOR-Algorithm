import constants.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class CalcGOR_I {
    private final SearchWindow window;
    private int gorType;
    private final ArrayList<Sequence> sequencesToPredict;
    private final HashMap<Character, Integer> totalSecOcc;    // count of sec structs of model file
    private boolean probabilities;


    public CalcGOR_I(String pathToModelFile, String fastaFile, int gorType, boolean probabilities) throws IOException {
        // temp init with the three sec types
        char[] secStructTypes = {'H', 'E', 'C'};
        this.probabilities = probabilities;
        this.window = new SearchWindow(pathToModelFile, gorType);
        this.totalSecOcc = calcStructureOccurrencies();
        this.sequencesToPredict = readFasta(fastaFile);
    }

    public void predict() throws IOException {
        // for each sequence → predict sec struct
        for (Sequence sequence: this.sequencesToPredict) {
            window.predictSeqGor(this.totalSecOcc, sequence, 1, probabilities);
        }
    }

    public HashMap<Character, Integer> calcStructureOccurrencies() {
        HashMap<Character, Integer> secSums = new HashMap<>();
        for (Character secStructType: this.window.getSecStructMatrices().keySet()){
            int[][] secMatrix = this.window.getSecStructMatrices().get(secStructType);
            int sum = calculateMatrixColumn(secMatrix);
            secSums.put(secStructType, sum);
        }
        return secSums;
    }

    public int calculateMatrixColumn(int[][] matrix) {
        int sum = 0;
        for (int[] ints : matrix) {
            sum += ints[0];
        }
        return sum;
    }

    public ArrayList<Sequence> readFasta(String fasta) throws IOException {
        BufferedReader buff = new BufferedReader(new FileReader(fasta));
        String line;
        StringBuilder sequence = new StringBuilder();
        ArrayList<Sequence> sequencesToPredict = new ArrayList<>();
        String currentId = "";

        // init secondary seq with '--------'
        StringBuilder sb = new StringBuilder();
        sb.append("-".repeat(Math.max(0, window.getWINDOWSIZE() / 2)));
        String tail = sb.toString();

        while ((line = buff.readLine()) != null) {
            if (line.startsWith(">")) {
                if (!currentId.isEmpty()) {
                    sequencesToPredict.add(new Sequence(currentId, sequence.toString(), tail));
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

    public ArrayList<Sequence> getSequencesToPredict() {
        return sequencesToPredict;
    }

    public String predictionsToString(boolean probabilities) {
        StringBuilder sb = new StringBuilder();
        for (Sequence s : this.sequencesToPredict) {
            // Sequences that were predicted end in eiter [E|C|H] and still need the "-" tail
            if (!(s.getSsSequence().endsWith("-"))) {
                sb.append(s.getId()).append("\n");
                sb.append("AS ").append(s.getAaSequence()).append("\n");
                sb.append("PS ").append(s.getSsSequence());
                sb.append("-".repeat(Math.max(0, Constants.WINDOW_SIZE.getValue()) / 2));
                sb.append("\n");

            }
            else {
                // here are sequences that were too short and already have a ss struct like "----"
                sb.append(s.getId()).append("\n");
                sb.append("AS ").append(s.getAaSequence()).append("\n");
                sb.append("PS ").append(s.getSsSequence()).append("\n"); // don't add tail
            }

            if (probabilities) {
                char[] orderedSecTypes = {'H', 'E', 'C'};
                for (char secType: orderedSecTypes) {
                    sb.append(secType).append("P ");
                    sb.append("0".repeat(Math.max(0, Constants.WINDOW_SIZE.getValue() / 2)));
                    for (int i = 0; i < s.getNormalizedProbabilities().get(secType).size(); i++) {
                        sb.append(s.getNormalizedProbabilities().get(secType).get(i));
                    }
                    sb.append("0".repeat(Math.max(0, Constants.WINDOW_SIZE.getValue() / 2)));
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

}
