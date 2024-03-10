import constants.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CalcGOR_IV {

    private final SearchWindow window;
    private int gorType = 4;
    private final ArrayList<Sequence> sequencesToPredict;
    private boolean probabilities;

    public CalcGOR_IV(String pathToModelFile, String fastaFile, boolean probabilities) throws IOException {
        // temp init with the three sec types
        char[] secStructTypes = {'H', 'E', 'C'};
        this.probabilities = probabilities;
        this.window = new SearchWindow(pathToModelFile, this.gorType);
        this.sequencesToPredict = readFasta(fastaFile);
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
                    // String startSeq = sb.append("-".repeat(Math.max(0, Constants.WINDOW_SIZE.getValue()) / 2)).toString();
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

    public void predict() throws IOException {
        // for each sequence → predict sec struct
        for (Sequence sequence: this.sequencesToPredict) {
            window.predictSeqGor(new HashMap<>(), sequence, 4, this.probabilities);
        }
    }

    public String predictionsToString(boolean probabilities) {
        StringBuilder sb = new StringBuilder();
        for (Sequence s : this.sequencesToPredict) {
            // Sequences that were predicted end in eiter [E|C|H] and still need the "-" tail
            if (!(s.getSsSequence().endsWith("-"))) {
                sb.append("> ").append(s.getId()).append("\n");
                sb.append("AS ").append(s.getAaSequence()).append("\n");
                sb.append("PS ").append(s.getSsSequence());
                sb.append("-".repeat(Math.max(0, Constants.WINDOW_SIZE.getValue()) / 2));
                sb.append("\n");

            }
            else {
                // here are sequences that were too short and already have a ss struct like "----"
                sb.append("> ").append(s.getId()).append("\n");
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

    public ArrayList<Sequence> getSequencesToPredict() {
        return sequencesToPredict;
    }
}
