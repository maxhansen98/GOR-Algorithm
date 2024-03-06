import constants.Constants;
import utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchWindow {
    private final HashMap<Character, int[][]> secStructMatrices = new HashMap<>();
    private final HashMap<Integer, Character> INDEX_TO_AA = new HashMap<>();
    private final HashMap<Character, Integer> AA_TO_INDEX = new HashMap<>();
    private final char[] secStructTypes = {'H', 'E', 'C'};

    public SearchWindow() {
        this.initMatrices(this.secStructTypes);
        initAAHashMaps();
    }

    public SearchWindow(String pathToModelFile) throws IOException {
        initAAHashMaps();
        this.initMatrices(this.secStructTypes);
        // read model file to init matrices
        this.readModFile(pathToModelFile);
    }

    public int getWINDOWSIZE() {
        return Constants.WINDOW_SIZE.getValue();
    }

    @Override
    public String toString(){
        // std out all matrices :)
        StringBuilder out = new StringBuilder();
        out.append("// Matrix3D\n");

        for (char key : this.secStructMatrices.keySet()){
            out.append("=").append(key).append("=\n");
            int[][] currSecMatrix = secStructMatrices.get(key);

            for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                out.append(this.INDEX_TO_AA.get(i) + "\t");

                for (int j = 0; j < Constants.WINDOW_SIZE.getValue(); j++) {
                    out.append(currSecMatrix[i][j]).append("\t");
                }
                out.append("\n");

            }
            out.append("\n");
        }
        return out.toString();
    }

    public void initMatrices(char[] secStructTypes){
        // init a matrix for each secStructType
        for (char secStruct : secStructTypes){
            this.secStructMatrices.put(secStruct, new int[20][17]);

            // put default value into matrices
            for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                for (int j = 0; j <Constants.WINDOW_SIZE.getValue(); j++) {
                    this.secStructMatrices.get(secStruct)[i][j] = 0;
                }
            }
        }
    }

    public HashMap<Character, int[][]> getSecStructMatrices() {
        return this.secStructMatrices;
    }

    public void initAAHashMaps(){
        // maps AA to the row of the 2d matrix
        this.INDEX_TO_AA.put(0, 'A');
        this.INDEX_TO_AA.put(1, 'C');
        this.INDEX_TO_AA.put(2, 'D');
        this.INDEX_TO_AA.put(3, 'E');
        this.INDEX_TO_AA.put(4, 'F');
        this.INDEX_TO_AA.put(5, 'G');
        this.INDEX_TO_AA.put(6, 'H');
        this.INDEX_TO_AA.put(7, 'I');
        this.INDEX_TO_AA.put(8, 'K');
        this.INDEX_TO_AA.put(9, 'L');
        this.INDEX_TO_AA.put(10,'M');
        this.INDEX_TO_AA.put(11,'N');
        this.INDEX_TO_AA.put(12,'P');
        this.INDEX_TO_AA.put(13,'Q');
        this.INDEX_TO_AA.put(14,'R');
        this.INDEX_TO_AA.put(15,'S');
        this.INDEX_TO_AA.put(16,'T');
        this.INDEX_TO_AA.put(17,'V');
        this.INDEX_TO_AA.put(18,'W');
        this.INDEX_TO_AA.put(19,'Y');
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

    public HashMap<Integer, Character> getINDEX_TO_AA() {
        return INDEX_TO_AA;
    }

    public void writeToFile(String modelFilePath) throws IOException {
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(modelFilePath))) {
            // Get the string representation of your object
            String output = this.toString();
            // Write the output to the file
            buf.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readModFile(String pathToModFile) throws IOException {
        File seqLibFile = new File(pathToModFile);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("=C=")){
                copyLineIntoArray(i, lines, 'C');
            }
            else if (lines.get(i).startsWith("=H=")){
                copyLineIntoArray(i, lines, 'H');
            }
            else if (lines.get(i).startsWith("=E=")){
                copyLineIntoArray(i, lines, 'E');
            }
        }
    }
    public void copyLineIntoArray(int i, ArrayList<String> lines, char secType){
        int relativeMatrixIndex = 0;
        for (int j = i + 1; j <= i + 20; j++) {
            String[] line = lines.get(j).split("\t");
            for (int k = 0; k < line.length - 1; k++) {
                this.secStructMatrices.get(secType)[relativeMatrixIndex][k] = Integer.parseInt(line[k+1]);
            }
            relativeMatrixIndex++;
        }
    }

    /*
    Filling the matrices using training file (Easily counting up the scores for each window)
     */
    public void slideWindowAndCount(String aaSequence, String ssSequence, String pdbId){
        if (aaSequence.length() >= this.getWINDOWSIZE()) {
            int start = 0; // init start index
            int windowMid = this.getWINDOWSIZE() / 2; // define mid index
            int windowEndPosition  = aaSequence.length() - windowMid ; // define end index of seq (this is the max val of windowMid)

            // enter main loop
            while (windowMid < windowEndPosition) {
                String aaSubSeq = aaSequence.substring(windowMid - this.getWINDOWSIZE() / 2, windowMid + 1 + this.getWINDOWSIZE() / 2);
                String ssSubSeq = ssSequence.substring(windowMid - this.getWINDOWSIZE() / 2, windowMid + 1 + this.getWINDOWSIZE() / 2);

                // in the subSeqs get the corresponding vals
                for (int index = 0; index < aaSubSeq.length(); index++) {
                    char currSS = ssSubSeq.charAt(this.getWINDOWSIZE() / 2) ; // this is the sec struct of the mid AA
                    char currAA = aaSubSeq.charAt(index);
                    if (AA_TO_INDEX.containsKey(currAA)){
                        int indexOfAAinMatrix = this.AA_TO_INDEX.get(currAA);
                        this.getSecStructMatrices().get(currSS)[indexOfAAinMatrix][index]++;
                    }
                }
                windowMid++;
            }
        }
    }

    /*
    Get all params ready and call addSecondaryCounts and extendSecondarySequence
     */
    public void predictSeq(HashMap<Character, Integer> totalOcc, Sequence sequence, int gor){
        String aaSequence = sequence.getAaSequence();
        if (aaSequence.length() >= this.getWINDOWSIZE()) {
            int start = 0; // init start index
            int windowMid = this.getWINDOWSIZE() / 2; // define mid index
            int windowStop  = aaSequence.length() - windowMid ; // define end index of seq (this is the max val of windowMid)

            // enter main loop
            while (windowMid <  windowStop) {
                // get AA at windowMid
                char aaAtWindoMid = aaSequence.charAt(windowMid);

                // now cut out a subsequence of size window
                String windowSequence = cutSubsequence(aaSequence, windowMid);
                char predSecStruct = 'C'; // default
                if (AA_TO_INDEX.containsKey(aaAtWindoMid)){
                    if (gor == 1){
                         predSecStruct = predictGorI(windowSequence, totalOcc);
                    }
                }

                sequence.extendSecStruct(predSecStruct);
                windowMid++;
            }
        }
        // if the sequence is too short, just give it "-"
        else {
            sequence.setSsSequence(""); // reset "--------" prefix which is in seq per default
            for (int i = 0; i < sequence.getAaSequence().length(); i++) {
                sequence.extendSecStruct('-');
            }
        }
    }

    public char predictGorI(String windowSequence, HashMap<Character, Integer> totalOcc){
        // for every secType, loop over sequence and calculate values
        // these are our scores
        HashMap<Character, Double> scoresPerSeq = new HashMap<>();
        // these are our global final values we will use to determine the max value
        scoresPerSeq.put('H', 0.0);
        scoresPerSeq.put('E', 0.0);
        scoresPerSeq.put('C', 0.0);

        for (char secType: this.getSecStructMatrices().keySet())  {
            // loop over sequence
            for (int column = 0; column < windowSequence.length(); column++) {
                char currAAinWindow = windowSequence.charAt(column);

                if (AA_TO_INDEX.containsKey(currAAinWindow)) {
                    // get row and look up value in matrix of curr secType
                    int row = AA_TO_INDEX.get(currAAinWindow);
                    int valueInMatrix = this.getSecStructMatrices().get(secType)[row][column];

                    int totalSec = totalOcc.get(secType); // f a|s
                    int totalNotSec = 0; // get the !s and !a|s freqs
                    int notSec = 0;

                    for (char antiSecStruct : getSecStructMatrices().keySet()){
                       if (secType != antiSecStruct){
                            totalNotSec += totalOcc.get(antiSecStruct); // f !s
                            notSec += this.getSecStructMatrices().get(antiSecStruct)[row][column];// f a|!s
                       }
                    }
                    // sum values into scoresPerSeq
                    double scoreToPutIntoSum = Math.log((1.0 * valueInMatrix / notSec * 1.0 * totalNotSec / totalSec));
                    scoresPerSeq.put(secType, scoresPerSeq.get(secType) + scoreToPutIntoSum);
                }
            }
        }

        if (scoresPerSeq.get('H') >= scoresPerSeq.get('E') && scoresPerSeq.get('H') >= scoresPerSeq.get('C')) {
            return 'H';
        } else if (scoresPerSeq.get('E') >= scoresPerSeq.get('H') && scoresPerSeq.get('E')  >= scoresPerSeq.get('C')) {
            return 'E';
        } else {
            return 'C';
        }

    }
    public String cutSubsequence(String aaSequence, int windowMid) {
        return aaSequence.substring(windowMid - this.getWINDOWSIZE() / 2, windowMid + 1 + this.getWINDOWSIZE() / 2);
    }
    /*
    Loop over all 3 matrices; for each amino acid in search window:
    Get all values needed for Value calculation (f_sec, f_!sec, f_secType and f_!secType)
    Then sum up the value for each Amino Acid in a search window, then normalize them to the SecondaryCounts-HashMap
     */
    public void addSecondaryCounts(String aaSubSeq, HashMap<Character,Integer> totalOcc, HashMap<Character, Double> AASecondaryCounts){
        for (Character secType : this.secStructMatrices.keySet()) {
            int sec = 0; // f secType|a
            double normalizedValue = 0;
            int notSec = 0; // f_!secType|a
            for (int index = 0; index < aaSubSeq.length(); index++) {
                // check if we have counts for the curr AA in the window
                char currAA = aaSubSeq.charAt(index);
                int totalSec = totalOcc.get(secType); // f_s
                int totalNotSec = 0; // f_!s

                if (this.AA_TO_INDEX.containsKey(currAA)) {
                    int aaIndex = AA_TO_INDEX.get(currAA);
                    int[][] secStructMatrix = secStructMatrices.get(secType);
                    sec = secStructMatrix[aaIndex][index];

                    for (Character notSecType : secStructMatrices.keySet()) {
                        if (!notSecType.equals(secType)) {
                            notSec += secStructMatrices.get(notSecType)[aaIndex][index];
                            totalNotSec += totalOcc.get(notSecType);
                        }
                    }
                    normalizedValue += Math.log(1.0 * sec / notSec) + (Math.log(1.0 * totalNotSec/ totalSec));
                }
            }
            AASecondaryCounts.put(secType, normalizedValue);
        }
    }

    /*
    Extend Secondary-Structure Sequence by the Score of addSecondaryCounts-Method
     */
    public void extendSecondarySequence(HashMap<Character, Double> AASecondaryCounts, Sequence sequence){
        double scoreH = AASecondaryCounts.get('H');
        double scoreC = AASecondaryCounts.get('C');
        double scoreE = AASecondaryCounts.get('E');

        if(scoreH >= scoreC && scoreH >= scoreE) {
            sequence.extendSecStruct('H');
        } else if (scoreC >= scoreH && scoreC >= scoreE) {
            sequence.extendSecStruct('C');
        } else {
            sequence.extendSecStruct('E');
        }
    }
}