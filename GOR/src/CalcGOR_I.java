import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class CalcGOR_I {
    private final SearchWindow window;
    private int gorType;
    private final ArrayList<Sequence> sequencesToPredict;
    private final HashMap<Character, Integer> totalSecOcc;    // count of sec structs of model file


    public CalcGOR_I(String pathToModelFile, String fastaFile, int gorType) throws IOException {
        // temp init with the three sec types
        char[] secStructTypes = {'H', 'E', 'C'};
        this.window = new SearchWindow(pathToModelFile, gorType);
        this.totalSecOcc = calcStructureOccurrencies();
        this.sequencesToPredict = readFasta(fastaFile);
    }

    public void predict() throws IOException {
        // for each sequence → predict sec struct
        for (Sequence sequence: this.sequencesToPredict) {
            // get entry content in readable vars
            String pdbId = sequence.getId();
            String aaSequence = sequence.getAaSequence();
            String ssSequence = sequence.getSsSequence();

            window.predictSeqGor(this.totalSecOcc, sequence, 1);
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

        while ((line = buff.readLine()) != null) {
            if (line.startsWith(">")) {
                if (!currentId.isEmpty()) {
                    // init secondary seq with default '--------'
                    sequencesToPredict.add(new Sequence(currentId, sequence.toString(), "--------"));
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Sequence s : this.sequencesToPredict) {
            // Sequences that were predicted end in eiter [E|C|H] and still need the "-" tail
            if (!(s.getSsSequence().endsWith("-"))) {
                sb.append(s.getId()).append("\n");
                sb.append("AS ").append(s.getAaSequence()).append("\n");
                sb.append("PS ").append(s.getSsSequence()).append("--------\n"); // add tail
            }
            else {
                // here are sequences that were too short and already have a ss struct like "----"
                sb.append(s.getId()).append("\n");
                sb.append("AS ").append(s.getAaSequence()).append("\n");
                sb.append("PS ").append(s.getSsSequence()).append("\n"); // don't add tail
            }
        }
        return sb.toString();
    }
}
