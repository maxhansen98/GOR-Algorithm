import com.sun.security.jgss.GSSUtil;
import constants.Constants;
import utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class SearchWindow {
    private final HashMap<Character, int[][]> gor1Matrices = new HashMap<>();
    private final HashMap<Character, HashMap<Character, int[][]>> gor3Matrices = new HashMap<>();
    private final HashMap<String, int[][]> gor4Matrices = new HashMap<>();
    private final HashMap<Integer, Character> INDEX_TO_AA = new HashMap<>();
    private final HashMap<Character, Integer> AA_TO_INDEX = new HashMap<>();
    private final char[] secStructTypes = {'H', 'E', 'C'};
    private int gorType;

    public void initAAHashMaps(){
        char[] aminoAcids = {'A','C','D','E','F','G','H','I','K','L','M','N','P','Q','R','S','T','V','W','Y'};

        // Mapping from index to amino acid
        for (int i = 0; i < aminoAcids.length; i++) {
            INDEX_TO_AA.put(i, aminoAcids[i]);
        }

        // Mapping from amino acid to index
        for (int i = 0; i < aminoAcids.length; i++) {
            AA_TO_INDEX.put(aminoAcids[i], i);
        }
    }

    public SearchWindow(int gorType) {
        this.gorType = gorType;
        initAAHashMaps();
        if (gorType == 1) {
            this.initGor1Matrices(this.secStructTypes);
        } else if (gorType == 3) {
            this.initGor3Matrices(this.secStructTypes);
        } else if (gorType == 4) {
            this.initGor4Matrices(this.secStructTypes);
        }
    }

    public SearchWindow(String pathToModelFile, int gorType) throws IOException {
        initAAHashMaps();
        this.gorType = gorType;
        // init gor1 matrices for GOR I
        if (gorType == 1) {
            this.initGor1Matrices(this.secStructTypes);
            this.readModFile(pathToModelFile);
        }

        // init gor3 matrices for GOR III
        else if (gorType == 3) {
            this.initGor3Matrices(secStructTypes);
            this.readGor3(pathToModelFile);
        }

        // init gor3 & gor4 matrices for GOR IV
        else if (gorType == 4) {
            // also init gor3matrices
            this.initGor3Matrices(secStructTypes);
            this.initGor4Matrices(secStructTypes);
            this.readGor4(pathToModelFile);
        }
    }

    public void initGor3Matrices(char[] secStructTypes) {
        for (char aa : AA_TO_INDEX.keySet()) {
            HashMap<Character, int[][]> secStructHashMapForAA = new HashMap<>();
            for (char secStruct : secStructTypes){
                secStructHashMapForAA.put(secStruct, new int[Constants.AA_SIZE.getValue()][getWINDOWSIZE()]);
                this.gor3Matrices.put(aa, secStructHashMapForAA);
                // put default value into matrices
                for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                    for (int j = 0; j <Constants.WINDOW_SIZE.getValue(); j++) {
                        this.gor3Matrices.get(aa).get(secStruct)[i][j] = 0;
                    }
                }
            }
        }
    }

    public void readGor3(String pathToModFile) throws IOException {

        File seqLibFile = new File(pathToModFile);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("=")) {
                // get keys
                char aaKey = lines.get(i).charAt(1);
                char ssKey = lines.get(i).charAt(3);
                copy4dMatrix(i, lines, aaKey, ssKey);
            }
        }
    }

    public int getWINDOWSIZE() {
        return Constants.WINDOW_SIZE.getValue();
    }

    public String gor3ToString(){
        StringBuilder out = new StringBuilder();
        out.append("// Matrix4D\n\n");

        for (char aa : this.gor3Matrices.keySet()){
            for (char secType : this.gor3Matrices.get(aa).keySet()) {
                out.append("=" + aa + "," + secType).append("=\n\n");
                int[][] currSecMatrix = gor3Matrices.get(aa).get(secType);

                for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                    out.append(this.INDEX_TO_AA.get(i) + "\t");

                    for (int j = 0; j < Constants.WINDOW_SIZE.getValue(); j++) {
                        out.append(currSecMatrix[i][j]).append("\t");
                    }
                    out.append("\n");
                }
                out.append("\n");
            }
        }
        return out.toString();
    }

    public String gor4ToString(){
        ArrayList<String> orderedKeys = new ArrayList<>();
        StringBuilder keyBuilder = new StringBuilder();
        char[] hardCodedOrder = {'C', 'E', 'H'}; //C E H
        for(char secStruct : hardCodedOrder) {
            for (char aa1 : AA_TO_INDEX.keySet()){
                for (char aa2 : AA_TO_INDEX.keySet()){
                    for (int i = 0; i < getWINDOWSIZE(); i++) {
                        String key = keyBuilder.append(secStruct).append(aa1).append(aa2).append(i).toString();
                        orderedKeys.add(key);
                        keyBuilder.setLength(0);
                    }
                }
            }
        }

        StringBuilder out = new StringBuilder();
        out.append("// Matrix6D\n\n");

        for (String key : orderedKeys) {
            int[][] currMatrix = gor4Matrices.get(key);
            String[] splitKey = key.split("");
            String lastKey = splitKey[3];
            if (splitKey.length == 5) {
                lastKey += splitKey[4];
            }
            int actualHeaderVal = Integer.parseInt(lastKey) - (getWINDOWSIZE()/2);
            String header = "=" + splitKey[0] + "," + splitKey[1] + "," + splitKey[2] + "," + actualHeaderVal + "=";

            out.append(header).append("\n\n");
            for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                out.append(this.INDEX_TO_AA.get(i) + "\t");
                for (int j = 0; j < Constants.WINDOW_SIZE.getValue(); j++) {
                    out.append(currMatrix[i][j]).append("\t");
                }
                out.append("\n");
            }
            out.append("\n");
            }
        return out.toString();
    }

    public String gor1ToString(){
        // std out all matrices :)
        StringBuilder out = new StringBuilder();
        out.append("// Matrix3D\n");

        for (char key : this.gor1Matrices.keySet()){
            out.append("=").append(key).append("=\n\n");
            int[][] currSecMatrix = gor1Matrices.get(key);

            for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                out.append(this.INDEX_TO_AA.get(i)).append("\t");

                for (int j = 0; j < Constants.WINDOW_SIZE.getValue(); j++) {
                    out.append(currSecMatrix[i][j]).append("\t");
                }
                out.append("\n");

            }
            out.append("\n");
        }
        return out.toString();
    }

    public void initGor1Matrices(char[] secStructTypes){
        // init a matrix for each secStructType
        for (char secStruct : secStructTypes){
            this.gor1Matrices.put(secStruct, new int[Constants.AA_SIZE.getValue()][Constants.WINDOW_SIZE.getValue()]);

            // put default value into matrices
            for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                for (int j = 0; j <Constants.WINDOW_SIZE.getValue(); j++) {
                    this.gor1Matrices.get(secStruct)[i][j] = 0;
                }
            }
        }
    }

    public void initGor4Matrices(char[] secStructTypes) {
        // keys:CAA-8 → -8 + 8 = 0 → add 8 to every last key instance
        // 3 x 20 x 20 x 17
        StringBuilder keyBuilder = new StringBuilder();
        char[] hardCodedOrder = {'C', 'E', 'H'}; //C E H
        for(char secStruct : hardCodedOrder) {
            for (char aa1 : AA_TO_INDEX.keySet()){
                for (char aa2 : AA_TO_INDEX.keySet()){
                    for (int i = 0; i < getWINDOWSIZE(); i++) {
                        String key = keyBuilder.append(secStruct).append(aa1).append(aa2).append(i).toString();
                        // System.out.println(key);
                        int[][] newDefaultMatrix = init2DMatrix();
                        this.gor4Matrices.put(key, newDefaultMatrix);
                        keyBuilder.setLength(0);
                    }
                }
            }
        }
    }

    public void readGor4(String pathToModFile) throws IOException {

        File seqLibFile = new File(pathToModFile);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);
        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("=")) {
                // get key
                String[] headerLine = lines.get(i).split(",");
                // here we get the 6dMatrix headers
                if (headerLine.length == 4) {
                    // extract the important key parts
                    char key1 = headerLine[0].charAt(1); // sec key
                    char key2 = headerLine[1].charAt(0); // aa1 key
                    char key3 = headerLine[2].charAt(0); // aa2 key
                    String key4String = headerLine[3].replace("=","");
                    int key4 = Integer.parseInt(key4String) + getWINDOWSIZE() / 2;
                    keyBuilder.append(key1).append(key2).append(key3).append(key4);
                    String finishedKey = keyBuilder.toString();

                    // use the finished key
                    copyGor4Matrix(i, lines, finishedKey);

                    // reset keyBuilder
                    keyBuilder.setLength(0);
                }
                else if (headerLine.length == 2) {
                    char aaKey = lines.get(i).charAt(1);
                    char ssKey = lines.get(i).charAt(3);
                    copy4dMatrix(i, lines, aaKey, ssKey);
                }
            }
        }
        // System.out.println(gor4ToString());
        // System.out.println(gor3ToString());
    }
    public int[][] init2DMatrix(){
        return new int[AA_TO_INDEX.size()][getWINDOWSIZE()];
    }


    public HashMap<Character, int[][]> getSecStructMatrices() {
        return this.gor1Matrices;
    }

    public HashMap<Integer, Character> getINDEX_TO_AA() {
        return INDEX_TO_AA;
    }

    public void writeToFile(String modelFilePath) throws IOException {
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(modelFilePath))) {
            // Get the string representation of your object
            if (this.gorType == 1) {
                String output = this.gor1ToString();
                buf.write(output);
            }
            else if (this.gorType == 3) {
                String output = this.gor3ToString();
                buf.write(output);
            }
            // Write the output to the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Character, HashMap<Character, int[][]>> getGor3Matrices() {
        return gor3Matrices;
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
        for (int j = i + 2; j <= i + 21; j++) {
            String[] line = lines.get(j).split("\t");
            for (int k = 0; k < line.length - 1; k++) {
                this.gor1Matrices.get(secType)[relativeMatrixIndex][k] = Integer.parseInt(line[k+1]);
            }
            relativeMatrixIndex++;
        }
    }

    public void copy4dMatrix (int i, ArrayList<String> lines, char aaType, char secType){
        int relativeMatrixIndex = 0;
        for (int j = i + 2; j <= i + 21; j++) {
            String[] line = lines.get(j).split("\t");
            for (int k = 0; k < line.length - 1; k++) {
            this.gor3Matrices.get(aaType).get(secType)[relativeMatrixIndex][k] = Integer.parseInt(line[k+1]);
            }
            relativeMatrixIndex++;
        }
    }

    public void copyGor4Matrix (int i, ArrayList<String> lines, String key){
        int relativeMatrixIndex = 0;
        for (int j = i + 2; j <= i + 21; j++) {
            String[] line = lines.get(j).split("\t");
            for (int k = 0; k < line.length - 1; k++) {
                this.gor4Matrices.get(key)[relativeMatrixIndex][k] = Integer.parseInt(line[k+1]);
            }
            relativeMatrixIndex++;
        }
    }

    /*
    Filling the matrices using training file (Easily counting up the scores for each window)
     */
    public void trainGor1(String aaSequence, String ssSequence, String pdbId){
        if (aaSequence.length() >= this.getWINDOWSIZE()) {
            int windowMid = this.getWINDOWSIZE() / 2; // define mid index
            int windowEndPosition  = aaSequence.length() - windowMid ; // define end index of seq (this is the max val of windowMid)

            // enter main loop
            while (windowMid < windowEndPosition) {
                String aaSubSeq = cutSubsequence(aaSequence, windowMid);
                String ssSubSeq = cutSubsequence(ssSequence, windowMid);

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

    public void trainGor3(String aaSequence, String ssSequence, String pdbId){
        if (aaSequence.length() >= this.getWINDOWSIZE()) {
            int windowMid = this.getWINDOWSIZE() / 2; // define mid index
            int windowEndPosition  = aaSequence.length() - windowMid ; // define end index of seq (this is the max val of windowMid)

            // enter main loop
            while (windowMid < windowEndPosition) {
                String aaSubSeq = cutSubsequence(aaSequence, windowMid);
                String ssSubSeq = cutSubsequence(ssSequence, windowMid);
                char midAA = aaSubSeq.charAt(this.getWINDOWSIZE() / 2);
                if (AA_TO_INDEX.containsKey(midAA)) {
                    char currSS = ssSubSeq.charAt(this.getWINDOWSIZE() / 2) ; // this is the sec struct of the mid AA
                    // in the subSeqs get the corresponding vals
                    for (int index = 0; index < aaSubSeq.length(); index++) {
                        char currAA = aaSubSeq.charAt(index); // AA from 0 - 16
                        if (AA_TO_INDEX.containsKey(currAA)){
                            int row = AA_TO_INDEX.get(currAA);
                            this.getGor3Matrices().get(midAA).get(currSS)[row][index] += 1;
                        }
                    }
                }
                windowMid++;
            }
        }
    }

    public void trainGor4(String aaSequence, String ssSequence, String pdbId){
        if (aaSequence.length() >= this.getWINDOWSIZE()) {
            int windowMid = this.getWINDOWSIZE() / 2; // define mid index
            int windowEndPosition  = aaSequence.length() - windowMid ; // define end index of seq (this is the max val of windowMid)

            // enter main loop
            while (windowMid < windowEndPosition) {
                String aaSubSeq = cutSubsequence(aaSequence, windowMid);
                String ssSubSeq = cutSubsequence(ssSequence, windowMid);
                char midAA = aaSubSeq.charAt(this.getWINDOWSIZE() / 2); //  key2
                if (AA_TO_INDEX.containsKey(midAA)) {
                    char currSS = ssSubSeq.charAt(this.getWINDOWSIZE() / 2) ;  // key1

                    // outer loop begins at -8 → 8
                    // i
                    for (int outer = 0; outer < aaSubSeq.length(); outer++) {
                        // inner loop beings at -7 → 8
                        // j = i + 1
                        for (int inner = outer + 1; inner < aaSubSeq.length(); inner++) {
                            char key3 = aaSubSeq.charAt(outer);
                            if(AA_TO_INDEX.containsKey(key3)) {
                                char innerAA = aaSubSeq.charAt(inner); // V
                                if (AA_TO_INDEX.containsKey(innerAA)) {
                                    int indexInnerAA = AA_TO_INDEX.get(innerAA); // V → row
                                    StringBuilder keyBuilder = new StringBuilder();
                                    String key = keyBuilder.append(currSS).append(midAA).append(key3).append(outer).toString();
                                    int[][] currentMatrix = gor4Matrices.get(key);
                                    currentMatrix[indexInnerAA][inner] += 1;
                                    keyBuilder.setLength(0);
                                }
                            }
                        }
                    }
                }
                windowMid++;
            }
        }
    }

    /*
    Get all params ready and call addSecondaryCounts and extendSecondarySequence
     */
    public void predictSeqGor(HashMap<Character, Integer> totalOcc, Sequence sequence, int gor, boolean probabilities){
        String aaSequence = sequence.getAaSequence();
        if (aaSequence.length() >= this.getWINDOWSIZE()) {
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
                    if (gor == 1) {
                        // TODO: --probabilities >:)
                        predSecStruct = predictGorI(windowSequence, totalOcc, sequence);
                    }
                    else if (gor == 3) {
                        // TODO: --probabilities >:)
                        predSecStruct = predictGorIII(windowSequence, sequence);
                    }
                    else if (gor == 4) {
                        // TODO: --probabilities >:)
                        predSecStruct = predictGorIV(windowSequence, sequence);
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
                for (char secType : secStructTypes) {
                    sequence.getNormalizedProbabilities().get(secType).add(0); // default prob of 0 because we made no prediction
                }
            }
        }
    }

    public char predictGorI(String windowSequence, HashMap<Character, Integer> totalOcc, Sequence sequence){
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
                    int sec = this.getSecStructMatrices().get(secType)[row][column];

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
                    scoresPerSeq.put(secType, scoresPerSeq.get(secType) + calcLog(sec, notSec, totalSec, totalNotSec));
                }
            }
        }

        return getMaxCount(scoresPerSeq, sequence);
    }

    public char predictGorIII(String windowSequence, Sequence sequence){
        HashMap<Character, Double> scoresPerSeq = new HashMap<>();
        // these are our global final values we will use to determine the max value
        scoresPerSeq.put('H', 0.0);
        scoresPerSeq.put('E', 0.0);
        scoresPerSeq.put('C', 0.0);

        // first we get the mid amino acid
        char aaMid = windowSequence.charAt(getWINDOWSIZE() / 2);

        for (char secType : scoresPerSeq.keySet()) { // iterate over secStructs
            int totalSec = 0;
            int totalNotSec = 0;
            int sec = 0;
            int notSec = 0;

            totalSec = calculateMatrixColumn(getGor3Matrices().get(aaMid).get(secType));
            // now we iterate a total of three times ofer the window sequence
            for (int column = 0; column < windowSequence.length(); column++) {
                char currAAinWindow = windowSequence.charAt(column);
                if (AA_TO_INDEX.containsKey(currAAinWindow)) {
                    int row = AA_TO_INDEX.get(currAAinWindow);
                    sec = gor3Matrices.get(aaMid).get(secType)[row][column];

                    for (char antiSecStruct: scoresPerSeq.keySet()) {
                        // get the negative frequencies
                        if (secType != antiSecStruct) {
                            totalNotSec += calculateMatrixColumn(getGor3Matrices().get(aaMid).get(antiSecStruct));
                            notSec += getGor3Matrices().get(aaMid).get(antiSecStruct)[row][column];
                        }
                    }
                    scoresPerSeq.put(secType, scoresPerSeq.get(secType) + calcLog(sec, notSec, totalSec, totalNotSec));
                }
            }
        }

        return getMaxCount(scoresPerSeq, sequence);
    }

    public char predictGorIV(String windowSequence, Sequence sequence) {
        HashMap<Character, Double> scoresPerSeq = new HashMap<>();
        // these are our global final values we will use to determine the max value
        scoresPerSeq.put('H', 0.0);
        scoresPerSeq.put('E', 0.0);
        scoresPerSeq.put('C', 0.0);
        int m = getWINDOWSIZE() / 2;

        // get the mid amino acid
        char centerAA = windowSequence.charAt(m);

        // CALCULATION FOR GOR IV
        for (char secType : secStructTypes) {
            if(AA_TO_INDEX.containsKey(centerAA)){
                // left part of the equation
                double outerSum = 0;
                double gor3Sum = 0;
                for (int outer = 0; outer < getWINDOWSIZE(); outer++) { // k sum
                    char outerLoopAA = windowSequence.charAt(outer);
                    double innerSum = 0;
                    // Gor4 inner loop
                    for (int inner = outer + 1; inner < getWINDOWSIZE(); inner++) { // l sum
                        if (AA_TO_INDEX.containsKey(outerLoopAA)) {
                            String completeKey = secType + "" + centerAA + "" + outerLoopAA + "" + (outer);
                            char innerAA = windowSequence.charAt(inner);

                            if (AA_TO_INDEX.containsKey(innerAA)) {
                                int row = AA_TO_INDEX.get(innerAA);
                                int sec = gor4Matrices.get(completeKey)[row][inner]; // upper part of division
                                int notSec = 0;
                                for (char antiSecType: secStructTypes) {
                                    // lower part of division
                                    if (antiSecType!=secType) {
                                        String alternativeKey = antiSecType + "" + centerAA + "" + outerLoopAA + "" + (outer);
                                        notSec += gor4Matrices.get(alternativeKey)[row][inner];
                                    }
                                }
                                innerSum += gor4Log(sec, notSec);
                            }
                        }
                    }
                    outerSum += innerSum;

                    // right part of the equation (GORIII)
                    char aaOuter = windowSequence.charAt(outer);
                    if (AA_TO_INDEX.containsKey(aaOuter)) {
                        int row = AA_TO_INDEX.get(aaOuter);
                        int gor3sec = gor3Matrices.get(centerAA).get(secType)[row][outer];
                        int gor3NotSec = 0;
                        for (char antiSecType: secStructTypes) {
                            if (antiSecType != secType) {
                                gor3NotSec += gor3Matrices.get(centerAA).get(antiSecType)[row][outer];
                            }
                        }
                        gor3Sum += gor4Log(gor3sec, gor3NotSec);
                    }
                }
                // factor sums
                outerSum *= (2.0 / (( 2 * m ) + 1));
                gor3Sum *= (2.0 * m - 1) / (2.0 * m + 1);
                scoresPerSeq.put(secType, outerSum - gor3Sum);
            }

        }
        return getMaxCount(scoresPerSeq, sequence);
    }

    public void predictGorV(Sequence sequence) {
        String aaSequence =  sequence.getAaSequence();
        ArrayList<String> aliSeqs =  sequence.getAlignmennts();
        if (aaSequence.length() >= this.getWINDOWSIZE()) {
            int windowMid = this.getWINDOWSIZE() / 2; // define mid index
            int windowStop  = aaSequence.length() - windowMid ; // define end index of seq (this is the max val of windowMid)

            // enter main loop that shifts the window across the sequence
            while (windowMid <  windowStop) {
                HashMap<Character, Double> globalCounts = new HashMap<>();
                globalCounts.put('H', 0.0);
                globalCounts.put('C', 0.0);
                globalCounts.put('E', 0.0);
                // get sequence of current window
                char aaAtWindoMid = aaSequence.charAt(windowMid);
                String windowSequence = cutSubsequence(aaSequence, windowMid);

                // get counts for windowSequence (based on which gor type)
                HashMap<Character, Double> predictionsForAAseq = predictForGorType(gorType, windowSequence, sequence);

                // add to global counts
                addCountsToSum(globalCounts, predictionsForAAseq);

                // now do the same for each alignment
                for  (String aliSeq : aliSeqs) {
                    // for each ali get sequence of curr window
                    char aliWindowMidAA = aliSeq.charAt(windowMid);
                    String aliWindowSequence = cutSubsequence(aaSequence, windowMid);

                    HashMap<Character, Double> predictionsForAliSeq = predictForGorType(gorType, windowSequence, sequence);
                    addCountsToSum(globalCounts, predictionsForAliSeq);
                }

                char predSecStruct = getMaxCount(globalCounts, sequence);
                // now cut out a subsequence of size window
                // char predSecStruct = 'C'; // default

                sequence.extendSecStruct(predSecStruct);
                windowMid++;
            }
            normalizeProbabilities(sequence);
        }
        // TODO: edge case handling
        // if the sequence is too short, just give it "-" and prob 0 etc...
        else {
        }

    }
    public HashMap<Character, Double> predictForGorType(int gorType, String windowSequence, Sequence sequence) {
        HashMap<Character, Double> scoresPerSeq = new HashMap<>();

        if (gorType == 1){
            // counts needed for GorI
            HashMap<Character, Integer> secSums = new HashMap<>();
            for (Character secStructType: this.getSecStructMatrices().keySet()){
                int[][] secMatrix = this.getSecStructMatrices().get(secStructType);
                int sum = calculateMatrixColumn(secMatrix);
                secSums.put(secStructType, sum);
            }
            scoresPerSeq = predictGorV_I(windowSequence, secSums, sequence);
        }
        else if (gorType == 3) {

        }
        else if (gorType == 4) {

        }
        return scoresPerSeq;
    }

    // used to update the global hashmap
    public void addCountsToSum(HashMap<Character, Double> global, HashMap<Character, Double> scoresToAdd) {
        for (char secType: global.keySet()) {
           double tmp = global.get(secType);
           double toAdd = scoresToAdd.get(secType);
           global.put(secType, tmp + toAdd);
        }
    }

    // this predictGor1 method is special because we don t want ti return the sec prediction but the probabilities for each sec
    public HashMap<Character, Double> predictGorV_I(String windowSequence, HashMap<Character, Integer> totalOcc, Sequence sequence){
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
                    int sec = this.getSecStructMatrices().get(secType)[row][column];

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
                    scoresPerSeq.put(secType, scoresPerSeq.get(secType) + calcLog(sec, notSec, totalSec, totalNotSec));
                }
            }
        }

        return scoresPerSeq;
    }
    private char getMaxCount(HashMap<Character, Double> scoresPerSeq, Sequence sequence) {
        double max = Math.max(scoresPerSeq.get('H'), Math.max(scoresPerSeq.get('C'), scoresPerSeq.get('E')));
        // update all probabilities for current sequence
        for (char secType : scoresPerSeq.keySet()) {
            sequence.updateProbabilities(secType, scoresPerSeq.get(secType));
        }

        if (max == scoresPerSeq.get('H')) {
            return 'H';
        } else if (max == scoresPerSeq.get('E')) {
            return 'E';
        } else {
            return 'C';
        }
    }

    public static void normalizeProbabilities(Sequence sequence) {
        for (char secType : sequence.getProbabilities().keySet()) {
            ArrayList<Double> probabilities = sequence.getProbabilities().get(secType);

            // probabilities [-2.6, 6.98, -3.1, -21.1, 6.5, ... ]
            // convert to values between 0 and 9 (normalized probabilities)
            double maxProb = Collections.max(probabilities);
            double minProb = Collections.min(probabilities);
            double diffOfMaxMin = maxProb - minProb;

            ArrayList<Integer> normalizedProbabilities = new ArrayList<>();

            for (double prob : probabilities) {
                int normalizedProb = (int) Math.floor((prob - minProb) * (9.0 / diffOfMaxMin));
                normalizedProbabilities.add(normalizedProb);
            }
            sequence.getNormalizedProbabilities().put(secType, normalizedProbabilities);
        }
    }

    private double calcLog(int sec, int notSec, int totalSec, int totalNotSec) {
        double dynamic_value = 1.0 * sec / notSec;
        double static_value = 1.0 * totalNotSec / totalSec;
        return Math.log(dynamic_value * static_value);
    }

    private double gor4Log(double P_s_j, double P_s_not_j){
        double res = Math.log((P_s_j + 1e-10) / (P_s_not_j + 1e-10));
        return res;
//        return Math.log(P_s_j / P_s_not_j);
    }


    public String cutSubsequence(String aaSequence, int windowMid) {
        return aaSequence.substring(windowMid - this.getWINDOWSIZE() / 2, windowMid + this.getWINDOWSIZE() / 2 + 1);
    }

    public HashMap<String, int[][]> getGor4Matrices(){
        return this.gor4Matrices;
    }

    public int calculateMatrixColumn(int[][] matrix) {
        int sum = 0;
        for (int[] ints : matrix) {
            sum += ints[0];
        }
        return sum;
    }
}