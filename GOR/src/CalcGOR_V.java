import java.io.IOException;
import java.util.ArrayList;

public class CalcGOR_V {

    private SearchWindow gorX;
    private boolean probabilities;
    private final ArrayList<Sequence> sequencesToPredict;

    public CalcGOR_V(String pathToModel, String mafPath, int gorType) throws IOException {

        // based on the gortype that gets passed (1|3|4), the searchWindow will automatically initialize the
        // needed matrices
        this.gorX = new SearchWindow(pathToModel, gorType);
        this.probabilities = true; // we always want the probabilities
        this.sequencesToPredict = AlignmentFileReader.readAliDir(mafPath);
    }

    public void predict(){
        for(Sequence aliSeq: sequencesToPredict) {
           gorX.predictGorV(aliSeq);
        }
        System.out.println(this.toString());

        // testing normalization of probabilities
        // for(Sequence aliSeq: sequencesToPredict) {
        //     ArrayList<Double> testProbs = new ArrayList<>();
        //     for (int i = 0; i < 100; i++) {
        //         double rand = Math.random();
        //         testProbs.add(rand);
        //     }
        //     aliSeq.setProbabilities(testProbs);
        //     // norm probabilities
        //     SearchWindow.normalizeProbabilities(aliSeq);

        // }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Sequence s : this.sequencesToPredict) {
            // Sequences that were predicted end in eiter [E|C|H] and still need the "-" tail
            if (!(s.getSsSequence().endsWith("-"))) {
                sb.append(s.getId()).append("\n");
                sb.append("AS ").append(s.getAaSequence()).append("\n");
                sb.append("PS ").append(s.getSsSequence());
                sb.append("-".repeat(Math.max(0, gorX.getWINDOWSIZE() / 2)));
                sb.append('\n');

            }
            else {
                // here are sequences that were too short and already have a ss struct like "----"
                sb.append(s.getId()).append("\n");
                sb.append("AS ").append(s.getAaSequence()).append("\n");
                sb.append("PS ").append(s.getSsSequence()).append("\n"); // don't add tail
            }
            for (char secType: sequencesToPredict.get(0).getProbabilities().keySet()) {
                sb.append(secType).append("P         ");
                for (int i = 0; i < s.getNormalizedProbabilities().get(secType).size(); i++) {
                    sb.append(s.getNormalizedProbabilities().get(secType).get(i));
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
