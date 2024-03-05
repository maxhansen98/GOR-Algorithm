import constants.Constants;
import utils.FileUtils;

import javax.imageio.plugins.tiff.FaxTIFFTagSet;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class CalcGOR_I {
    private final SearchWindow window;
    private final ArrayList<Sequence> sequencesToPredict;
    private final HashMap<Character, Integer> totalSecOcc;    // count of sec structs of model file


    public CalcGOR_I(String pathToModelFile, String fastaFile) throws IOException {
        // temp init with the three sec types
        char[] secStructTypes = {'H', 'E', 'C'};
        this.window = new SearchWindow(pathToModelFile);
        this.totalSecOcc = calcStructureOccurrencies();
        this.sequencesToPredict = readFasta(fastaFile);
    }

    public void predict() throws IOException {
        // for each sequence → predict sec struct
        for (Sequence sequence: this.sequencesToPredict) {
            // get entry content in readable vars
            String pdbId = sequence.getId();
            String aaSequence = sequence.getAaSequence();
            String ssSequence = sequence.getSsSequence();

            window.slideWindowAndPredict(aaSequence, this.totalSecOcc, sequence);
        }
    }

    public HashMap<Character, Integer> calcStructureOccurrencies() {
        HashMap<Character, Integer> secSums = new HashMap<>();
        for (Character secStructType: this.window.getSecStructMatrices().keySet()){
            int[][] secMatrix = this.window.getSecStructMatrices().get(secStructType);
            int sum = calculateMatrixColumn(secMatrix);
            secSums.put(secStructType, sum);
        }
        return secSums;
    }

    public int calculateMatrixColumn(int[][] matrix) {
        int sum = 0;
        for (int i = 0; i < matrix[0].length; i++) {
           sum+=matrix[i][0];
        }
        return sum;
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

    public ArrayList<Sequence> getSequencesToPredict() {
        return sequencesToPredict;
    }

    public void printPredictions(){
        ArrayList<Sequence> predictions = this.getSequencesToPredict();
        for (Sequence s : predictions) {
            System.out.println(s.getAaSequence());
            System.out.println(s.getSsSequence() + "--------");
        }
    }

//    public HashMap<Character, Double> sumAAsecStructFrequencies(char secType){
//        HashMap<Character, Double> frequencies = new HashMap<>();
//        return frequencies;
//    }

//   TODO: To File
//    public void writeResultsToPrd(String outputName) {
//        String fileName = outputName + "_gor1_" + outputName + ".prd";
//        try {
//            FileWriter writer = new FileWriter(fileName);
//            writer.write("> " + id + "\nAS "+ aaSeq + "\nSS "+ ssSeq);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
