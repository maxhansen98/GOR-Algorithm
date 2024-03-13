import utils.CmdParser;
import constants.Constants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import utils.FileUtils;

import java.io.File;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

public class GORMain {
    public static void main(String[] args) throws IOException, ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor("GOR").build().defaultHelp(true).description("Predict Secondary Structure");
        parser.addArgument("--format", "--type").choices("txt", "html").setDefault("txt");
        parser.addArgument("--model").help("File to calculate checksum");
        parser.addArgument("--probabilities").action(storeTrue());
        parser.addArgument("--post").action(storeTrue());
        parser.addArgument("--seq").setDefault("-1");
        parser.addArgument("--maf").setDefault("-1");

        Namespace ns = parser.parseArgs(args);
        String pathToModel = ns.getString("model");
        String format = ns.getString("format");
        boolean probabilities = ns.get("probabilities");
        String fastaPath = ns.getString("seq");
        String mafPath = ns.getString("maf");
        boolean postProc = ns.get("post");

        // either gor1|3|4 or gor5 !!!
        if(mafPath.equals("-1") && fastaPath.equals("-1")) {
           return;
        }


        // store predictions
        ArrayList<Sequence> predictions = new ArrayList<>();
        if (!(fastaPath.equals("-1"))) {
            int gorType = getGorType(pathToModel);
            if (gorType == 1){
                CalcGOR_I gorI = new CalcGOR_I(pathToModel, fastaPath, gorType, probabilities);
                //HashMap<Character, Integer> test = gorI.calcStructureOccurrencies();
                gorI.predict();

                if(postProc) {
                    predictions = gorI.getSequencesToPredict(); // for post processing
                    postProcess(predictions);
                }
                if (format.equals("txt")){
                    System.out.println(gorI.predictionsToString(probabilities));
                } else if (format.equals("html")) {
                    toJson(gorI.getSequencesToPredict());
                }
            } else if (gorType == 3) {
                CalcGOR_III gorIII = new CalcGOR_III(pathToModel, fastaPath, probabilities);
                gorIII.predict();

                if(postProc) {
                    predictions = gorIII.getSequencesToPredict();  // for post processing
                    postProcess(predictions);
                }
                if (format.equals("txt")){
                    System.out.println(gorIII.predictionsToString(probabilities));
                } else if (format.equals("html")) {
                    toJson(gorIII.getSequencesToPredict());
                }
            } else if (gorType == 4) {
                CalcGOR_IV gorIV = new CalcGOR_IV(pathToModel, fastaPath, probabilities);
                gorIV.predict();
                if(postProc) {
                    predictions = gorIV.getSequencesToPredict();  // for post processing
                    postProcess(predictions);
                }
                if (format.equals("txt")){
                    System.out.println(gorIV.predictionsToString(probabilities));
                } else if (format.equals("html")) {
                    toJson(gorIV.getSequencesToPredict());
                }
            }
        }
        // GOR V
        else {
            int gorType = getGorType(pathToModel);
            CalcGOR_V gor_v = new CalcGOR_V(pathToModel, mafPath, gorType, probabilities);
            gor_v.predict();
            if(postProc) {
                predictions = gor_v.getSequencesToPredict();  // for post processing
                postProcess(predictions);
            }
            if (format.equals("txt")) {
                System.out.println(gor_v.predictionsToString(probabilities));
            }
            else if (format.equals("html")) {
               toJson(gor_v.getSequencesToPredict());
            }
        }
    }

    private static void postProcess(ArrayList<Sequence> predictions) {
        for (Sequence sequence : predictions) {
            StringBuilder seqBuilder = new StringBuilder(sequence.getSsSequence());

            for (int i = Constants.WINDOW_SIZE.getValue() / 2; i < seqBuilder.length() - Constants.WINDOW_SIZE.getValue() / 2 - 1; i++) {
                if (seqBuilder.charAt(i) != seqBuilder.charAt(i - 1) &&
                        seqBuilder.charAt(i) != seqBuilder.charAt(i + 1) &&
                        seqBuilder.charAt(i - 1) == seqBuilder.charAt(i + 1)) {
                    seqBuilder.setCharAt(i, seqBuilder.charAt(i - 1));
                } else if (seqBuilder.charAt(i) != seqBuilder.charAt(i - 1) &&
                        seqBuilder.charAt(i) != seqBuilder.charAt(i + 1)) {
                    seqBuilder.setCharAt(i, 'C'); // Default C, cause highest std. prob.
                }
            }
            sequence.setSsSequence(seqBuilder.toString());
        }
    }


    // determine which gor should be initialized
    public static int getGorType(String pathToModel) throws IOException{
        File seqLibFile = new File(pathToModel);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);
        String typeLine = lines.get(0);
        if (typeLine.startsWith("// Matrix6D")){
            return 4;
        } else if (typeLine.startsWith("// Matrix4D")) {
            return 3;
        }
        else if (typeLine.startsWith("// Matrix3D")){
            return 1;
        }
        else {
            return -1; // input file is wrong
        }
    }


    // to html (actually json) method
    static void toJson(ArrayList<Sequence> sequences) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Sequence s : sequences) {
            sb.append(s.toJson());
        }
        sb.deleteCharAt(sb.length() - 2); // deletes the last comma in the last entry "} ," ← this comma :)
        sb.append("]");
        System.out.println(sb.toString());
    }

    public static void writeToFile(String content, String pathOfFile) throws IOException {
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(pathOfFile))) {
            buf.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
