import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CalcGOR_V {

    private SearchWindow gorX;
    private boolean probabilities;
    private final ArrayList<Sequence> sequencesToPredict;

    public CalcGOR_V(String pathToModel, String mafPath, int gorType, boolean probabilities) throws IOException {

        // based on the gortype that gets passed (1|3|4), the searchWindow will automatically initialize the
        // needed matrices
        this.gorX = new SearchWindow(pathToModel, gorType);
        this.probabilities = probabilities; // we always want the probabilities
        this.sequencesToPredict = AlignmentFileReader.readAliDir(mafPath);
    }

    public void predict(){
        for(Sequence aliSeq: sequencesToPredict) {
           gorX.predictGorV(aliSeq, probabilities);
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
                sb.append("-".repeat(Math.max(0, gorX.getWINDOWSIZE() / 2)));
                sb.append("\n");

            }
            else {
                // here are sequences that were too short and already have a ss struct like "----"
                sb.append("> ").append(s.getId()).append("\n");
                sb.append("AS ").append(s.getAaSequence()).append("\n");
                sb.append("PS ").append(s.getSsSequence()).append("\n\n"); // don't add tail
            }

            if (probabilities) {
                char[] orderedSecTypes = {'H', 'E', 'C'};
                for (char secType: orderedSecTypes) {
                    sb.append(secType).append("P ");
                    sb.append("0".repeat(Math.max(0, gorX.getWINDOWSIZE() / 2)));
                    for (int i = 0; i < s.getNormalizedProbabilities().get(secType).size(); i++) {
                        sb.append(s.getNormalizedProbabilities().get(secType).get(i));
                    }
                    sb.append("0".repeat(Math.max(0, gorX.getWINDOWSIZE() / 2)));
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
