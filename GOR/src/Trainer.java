import java.io.IOException;
import java.util.ArrayList;

public class Trainer {
    private SearchWindow searchWindow;
    private ArrayList<String[]> trainingSequences;
    public Trainer(String pathToDBfile) throws IOException {
        searchWindow = new SearchWindow();
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }

    public SearchWindow getSearchWindow() {
        return searchWindow;
    }

    public ArrayList<String[]> getTrainingSequences() {
        return trainingSequences;
    }

    public Trainer(String pathToDBfile, int windowSize) throws IOException {
        searchWindow = new SearchWindow(windowSize);
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }
    public Trainer(String pathToDBfile, String[] secStructTypes) throws IOException {
        searchWindow = new SearchWindow(secStructTypes);
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }
    public Trainer(String pathToDBfile, int windowSize, String[] secStructTypes) throws IOException {
        searchWindow = new SearchWindow(windowSize, secStructTypes);
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }
    public void train(){

    }

}
