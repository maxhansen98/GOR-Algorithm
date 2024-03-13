import constants.Constants;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

public class TrainPredict {

    public static void main(String[] args) throws ArgumentParserException, IOException {
        ArgumentParser parser = ArgumentParsers.newFor("GOR").build().defaultHelp(true).description("Predict Secondary Structure");
        // GOR TRAIN ARGS
        parser.addArgument("--db").setDefault("-1");
        parser.addArgument("--method").setDefault("-1");
        parser.addArgument("--modelT").setDefault("-1");

        // GOR PREDICT
        parser.addArgument("--format", "--type").choices("txt", "html").setDefault("txt");
        parser.addArgument("--model").help("File to calculate checksum");
        parser.addArgument("--probabilities").action(storeTrue());
        parser.addArgument("--seq").setDefault("-1");
        parser.addArgument("--maf").setDefault("-1");
        parser.addArgument("--out").setDefault("-1");
        parser.addArgument("--w").setDefault("17");
        parser.addArgument("--post").action(storeTrue());


        Namespace ns = parser.parseArgs(args);
        String pathToModel = ns.getString("model");
        String format = ns.getString("format");
        boolean probabilities = ns.get("probabilities");
        String fastaPath = ns.getString("seq");
        String mafPath = ns.getString("maf");
        String db = ns.getString("db");
        String method = ns.getString("method");
        String model = ns.getString("modelT");
        String pathOut = ns.getString("out");
        boolean postProc = ns.get("post");

        // get window size
        int wSize = Integer.parseInt(ns.getString("w"));

        Constants.WINDOW_SIZE.setWindowSize(wSize);

        // training
        ArrayList<Sequence> predictions = new ArrayList<>();
        if (method.equals("gor1")) {
            TrainerGOR1 trainer = new TrainerGOR1(db, 1);
            trainer.train(model);
            trainer.getSearchWindow().writeToFile(model);
        } else if (method.equals("gor3")) {
            TrainerGOR3 trainerGOR3 = new TrainerGOR3(db, 3);
            trainerGOR3.train(model);
            trainerGOR3.getSearchWindow().writeToFile(model);
        } else if (method.equals("gor4")) {
            TrainerGOR4 trainerGOR4 = new TrainerGOR4(db, 4);
            trainerGOR4.train(model);
            trainerGOR4.getSearchWindow().writeToFile(model);
        }

        // PREDICTION
        // either gor1|3|4 or gor5 !!!
        if(mafPath.equals("-1") && fastaPath.equals("-1")) {
            return;
        }

        // for gor1 gor3 gor4 we need a fasta path
        if (!(fastaPath.equals("-1"))) {
            int gorType = GORMain.getGorType(pathToModel);
            if (gorType == 1){
                CalcGOR_I gorI = new CalcGOR_I(pathToModel, fastaPath, gorType, probabilities);
                HashMap<Character, Integer> test = gorI.calcStructureOccurrencies();
                gorI.predict();
                if(postProc) {
                    predictions = gorI.getSequencesToPredict(); // for post processing
                    postProcess(predictions);
                }
                if (format.equals("txt")){
                    System.out.println(gorI.predictionsToString(probabilities));
                } else if (format.equals("html")) {
                    GORMain.toJson(gorI.getSequencesToPredict());
                }

                if (!(pathOut.equals("-1"))) {
                    writeToFile(gorI.predictionsToString(probabilities), pathOut);
                }
            } else if (gorType == 3) {
                CalcGOR_III gorIII = new CalcGOR_III(pathToModel, fastaPath, probabilities);
                gorIII.predict();

                if(postProc) {
                    predictions = gorIII.getSequencesToPredict(); // for post processing
                    postProcess(predictions);
                }
                if (format.equals("txt")){
                    System.out.println(gorIII.predictionsToString(probabilities));
                } else if (format.equals("html")) {
                    GORMain.toJson(gorIII.getSequencesToPredict());
                }
                if (!(pathOut.equals("-1"))) {
                    writeToFile(gorIII.predictionsToString(probabilities), pathOut);
                }
            } else if (gorType == 4) {
                CalcGOR_IV gorIV = new CalcGOR_IV(pathToModel, fastaPath, probabilities);
                gorIV.predict();

                if(postProc) {
                    predictions = gorIV.getSequencesToPredict(); // for post processing
                    postProcess(predictions);
                }
                if (format.equals("txt")){
                    System.out.println(gorIV.predictionsToString(probabilities));
                } else if (format.equals("html")) {
                    GORMain.toJson(gorIV.getSequencesToPredict());
                }
                if (!(pathOut.equals("-1"))) {
                    writeToFile(gorIV.predictionsToString(probabilities), pathOut);
                }
            }
        }
        // GOR V
        else {
            int gorType = GORMain.getGorType(pathToModel);
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
                GORMain.toJson(gor_v.getSequencesToPredict());
            }
            if (!(pathOut.equals("-1"))) {
                writeToFile(gor_v.predictionsToString(probabilities), pathOut);
            }
        }
    }

    private static void postProcess(ArrayList<Sequence> predictions) {
        for (Sequence sequence : predictions) {
            StringBuilder seqBuilder = new StringBuilder(sequence.getSsSequence());

            for (int i = Constants.WINDOW_SIZE.getValue() / 2 + 1; i < seqBuilder.length() - Constants.WINDOW_SIZE.getValue() / 2 - 2; i++) {
                if (seqBuilder.charAt(i) != seqBuilder.charAt(i - 1) &&
                        seqBuilder.charAt(i) != seqBuilder.charAt(i + 1) &&
                        seqBuilder.charAt(i - 1) == seqBuilder.charAt(i + 1)) {
                    seqBuilder.setCharAt(i, seqBuilder.charAt(i - 1));
                } else if (seqBuilder.charAt(i) != seqBuilder.charAt(i - 1) &&
                        seqBuilder.charAt(i) != seqBuilder.charAt(i + 1)) {
                    double leftProb = sequence.getProbabilities().get(sequence.getSsSequence().charAt(i-1)).get(i);
                    double rightProb = sequence.getProbabilities().get(sequence.getSsSequence().charAt(i+1)).get(i);
                    if (leftProb >= rightProb) {
                        seqBuilder.setCharAt(i, sequence.getSsSequence().charAt(i-1)); // Default C, cause highest std. prob.
                    } else {
                        seqBuilder.setCharAt(i, sequence.getSsSequence().charAt(i+1)); // Default C, cause highest std. prob.
                    }
                    // HEH
                }
            }
            sequence.setSsSequence(seqBuilder.toString());
        }
    }

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

    public static void writeToFile(String content, String pathOfFile) throws IOException {
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(pathOfFile))) {
            buf.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}