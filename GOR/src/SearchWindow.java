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
    private HashMap<Integer, Character> INDEX_TO_AA = new HashMap<>();
    private HashMap<Character, Integer> AA_TO_INDEX = new HashMap<>();
    private char[] secStructTypes = {'H', 'E', 'C'};

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

    public SearchWindow(int windowSize){
        if (windowSize % 2 == 0){
            // search window needs to be odd
            throw new IllegalArgumentException("Search window size needs to be an odd number!");
        }
        initAAHashMaps();
        // this.WINDOWSIZE = windowSize;
        this.initMatrices(this.secStructTypes);
    }

    public SearchWindow(char[] secStructTypes) {
        // this.WINDOWSIZE = 17; // default
        initAAHashMaps();
        this.initMatrices(secStructTypes);
    }

    public SearchWindow(int windowSize, char[] secStructTypes) {
        if (windowSize % 2 == 0){
            // search window needs to be odd
            throw new IllegalArgumentException("Search window size needs to be an odd number!");
        }
        // this.WINDOWSIZE = windowSize;
        initAAHashMaps();
        this.initMatrices(secStructTypes);
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

    public void slideWindowAndPredict(String aaSequence, HashMap<Character, Integer> totalOcc, Sequence sequence){
        if (aaSequence.length() >= this.getWINDOWSIZE()) {
            int start = 0; // init start index
            int windowMid = this.getWINDOWSIZE() / 2; // define mid index
            int windowEndPosition  = aaSequence.length() - windowMid ; // define end index of seq (this is the max val of windowMid)


            // enter main loop
            while (windowMid < windowEndPosition) {
                String aaSubSeq = aaSequence.substring(windowMid - this.getWINDOWSIZE() / 2, windowMid + 1 + this.getWINDOWSIZE() / 2);
                HashMap<Character, Double> AAsecondaryCounts = new HashMap<>();
                AAsecondaryCounts.put('H', 0.0);
                AAsecondaryCounts.put('C', 0.0);
                AAsecondaryCounts.put('E', 0.0);

                for (Character secType : this.secStructMatrices.keySet()) {
                    for (int index = 0; index < aaSubSeq.length(); index++) {
                        char currAA = aaSubSeq.charAt(index);
                        if (this.AA_TO_INDEX.containsKey(currAA)) {
                            int aaIndex = AA_TO_INDEX.get(currAA);
                            int[][] secStructMatrix = secStructMatrices.get(secType);
                            int sec = secStructMatrix[aaIndex][index]; // f secType|a
                            int notSec = 0; // f_!secType|a
                            int totalSec = totalOcc.get(secType); // f_secType
                            int totalNotSec = 0; // f_!s
                            for (Character notSecType : secStructMatrices.keySet()) {
                                if (!notSecType.equals(secType)) {
                                   notSec += secStructMatrices.get(notSecType)[aaIndex][index];
                                   totalNotSec += totalOcc.get(notSecType);
                                }
                            }
                            // now we got everything we need
                            double normalizedValue = Math.log(1.0 * sec / notSec) + Math.log(1.0 * totalNotSec/ totalSec);
                            AAsecondaryCounts.put(secType, AAsecondaryCounts.get(secType) + normalizedValue);
                        }
                    }
                }
                double scoreH = AAsecondaryCounts.get('H');
                double scoreC = AAsecondaryCounts.get('C');
                double scoreE = AAsecondaryCounts.get('E');

                if(scoreH >= scoreC && scoreH >= scoreE) {
                    sequence.extendSecStruct('H');
                } else if (scoreC >= scoreH && scoreC >= scoreE) {
                    sequence.extendSecStruct('C');
                } else {
                    sequence.extendSecStruct('E');
                }
                windowMid++;
            }
        }
    }
}
