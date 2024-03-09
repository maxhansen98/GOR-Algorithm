import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// Due to extreme time pressure I have a lot of duplicate code
public class CalcGOR_III {

    private final SearchWindow window;
    private int gorType = 3;
    private final ArrayList<Sequence> sequencesToPredict;
    private boolean probabilities;

    public CalcGOR_III(String pathToModelFile, String fastaFile, boolean probabilities) throws IOException {
        // temp init with the three sec types
        char[] secStructTypes = {'H', 'E', 'C'};
        this.probabilities = probabilities;
        this.window = new SearchWindow(pathToModelFile, this.gorType);
        this.sequencesToPredict = readFasta(fastaFile);
        // System.out.println(window.gor3ToString());
    }


    public ArrayList<Sequence> readFasta(String fasta) throws IOException {
        BufferedReader buff = new BufferedReader(new FileReader(fasta));
        String line;
        StringBuilder sequence = new StringBuilder();
        ArrayList<Sequence> sequencesToPredict = new ArrayList<>();
        String currentId = "";

        // init secondary seq with '--------'
        // TODO:
        // StringBuilder sb = new StringBuilder();
        // sb.append("-".repeat(Math.max(0, window.getWINDOWSIZE() / 2)));
        // sb.append("\n");
        while ((line = buff.readLine()) != null) {
            if (line.startsWith(">")) {
                if (!currentId.isEmpty()) {
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
            // get entry content in readable vars
            window.predictSeqGor(new HashMap<>(), sequence, 3, this.probabilities);
        }
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

    public ArrayList<Sequence> getSequencesToPredict() {
        return sequencesToPredict;
    }

}
