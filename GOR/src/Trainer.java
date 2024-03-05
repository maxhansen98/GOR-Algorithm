import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Trainer {
    private SearchWindow searchWindow;
    private ArrayList<Sequence> trainingSequences;
    public Trainer(String pathToDBfile) throws IOException {
        searchWindow = new SearchWindow();
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }

    public void train(String pathToModelFile) throws IOException {
        // define main loop that goes over the sequences in training sequences
        // for each entry, init
        for (Sequence sequence: this.trainingSequences) {
            // get entry content in readable vars
            String pdbId = sequence.getId();
            String aaSequence = sequence.getAaSequence();
            String ssSequence = sequence.getSsSequence();
            searchWindow.slideWindowAndCount(aaSequence, ssSequence, pdbId);
        }
        searchWindow.writeToFile(pathToModelFile);
    }

    // getters and setters
    public SearchWindow getSearchWindow() {
        return searchWindow;
    }

    public ArrayList<Sequence> getTrainingSequences() {
        return trainingSequences;
    }
}
