import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Trainer {
    private SearchWindow searchWindow;
    private ArrayList<DBEntry> trainingSequences;
    private HashMap<Character, Integer> AA_TO_INDEX = new HashMap<>();
    public Trainer(String pathToDBfile) throws IOException {
        searchWindow = new SearchWindow();
        initAAPosMap();
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }

    public Trainer(String pathToDBfile, int windowSize) throws IOException {
        searchWindow = new SearchWindow(windowSize);
        initAAPosMap();
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }

    public Trainer(String pathToDBfile, char[] secStructTypes) throws IOException {
        searchWindow = new SearchWindow(secStructTypes);
        initAAPosMap();
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }

    public Trainer(String pathToDBfile, int windowSize, char[] secStructTypes) throws IOException {
        searchWindow = new SearchWindow(windowSize, secStructTypes);
        initAAPosMap();
        this.trainingSequences = SecLibFileReader.readSecLibFile(pathToDBfile);
    }
    public void initAAPosMap(){
        // maps AA to the row of the 2d matrix
        this.AA_TO_INDEX.put('A', 0);
        this.AA_TO_INDEX.put('C', 1);
        this.AA_TO_INDEX.put('D', 2);
        this.AA_TO_INDEX.put('E', 3);
        this.AA_TO_INDEX.put('F', 4);
        this.AA_TO_INDEX.put('G', 5);
        this.AA_TO_INDEX.put('H', 6);
        this.AA_TO_INDEX.put('I', 7);
        this.AA_TO_INDEX.put('K', 8);
        this.AA_TO_INDEX.put('L', 9);
        this.AA_TO_INDEX.put('M', 10);
        this.AA_TO_INDEX.put('N', 11);
        this.AA_TO_INDEX.put('P', 12);
        this.AA_TO_INDEX.put('Q', 13);
        this.AA_TO_INDEX.put('R', 14);
        this.AA_TO_INDEX.put('S', 15);
        this.AA_TO_INDEX.put('T', 16);
        this.AA_TO_INDEX.put('V', 17);
        this.AA_TO_INDEX.put('W', 18);
        this.AA_TO_INDEX.put('Y', 19);
    }


    public void train(){
        // define main loop that goes over the sequences in training sequences
        // for each entry, init
        for (DBEntry entry : this.trainingSequences) {
            // get entry content in readable vars
            String pdbId = entry.getPdbId();
            String aaSequence = entry.getAaSequence();
            String ssSequence = entry.getSsSequence();
            System.out.println("-------------------------------------------------------------");
            fillCount(aaSequence, ssSequence, this.searchWindow, pdbId);
        }
        System.out.println(searchWindow);
    }

    public void fillCount(String aaSequence, String ssSequence, SearchWindow window, String pdbId){
        // check if seq is <= searchWindowSize
        if (aaSequence.length() < window.getWINDOWSIZE()){
            throw new IllegalArgumentException("Sequence " + pdbId + " shorter than search window size");
        }

        int start = 0; // init start index
        int windowMid = searchWindow.getWINDOWSIZE() / 2; // define mid index
        int end  = aaSequence.length() - windowMid ; // define end index of seq (this is the max val of windowMid)


        // enter main loop
        while (windowMid < end) {
            String aaSubSeq = aaSequence.substring(windowMid - searchWindow.getWINDOWSIZE() / 2, windowMid + 1 + searchWindow.getWINDOWSIZE() / 2);
            String ssSubSeq = ssSequence.substring(windowMid - searchWindow.getWINDOWSIZE() / 2, windowMid + 1 + searchWindow.getWINDOWSIZE() / 2);
            System.out.println(aaSubSeq);

            // in the subSeqs get the corresponding vals
            // TODO: This is currently not working
            for (int index = 0; index < aaSubSeq.length(); index++) {
                char currSS = ssSubSeq.charAt(searchWindow.getWINDOWSIZE() / 2) ; // this is the sec struct of the mid AA
                char currAA = aaSubSeq.charAt(index);
                if (!(currAA == 'X' || currAA == 'B' || currAA == 'Z')) {
                    int indexOfAAinMatrix = this.AA_TO_INDEX.get(currAA);
                    window.getSecStructMatrices().get(currSS)[indexOfAAinMatrix][index]++;
                }
            }
            windowMid++;
        }
    }

    // getters and setters
    public SearchWindow getSearchWindow() {
        return searchWindow;
    }

    public ArrayList<DBEntry> getTrainingSequences() {
        return trainingSequences;
    }
}
