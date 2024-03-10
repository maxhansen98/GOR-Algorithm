import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.util.HashMap;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

public class TrainPredictMain {
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

        // VALIDATE GOR
        parser.addArgument("-p").help("Prediction File");
        parser.addArgument("-r").help("seclib-file");
        parser.addArgument("-s").help("summary file");
        parser.addArgument("-d").help("detailed file");
        parser.addArgument("-b").action(storeTrue()); // b for boxplot

        Namespace ns = parser.parseArgs(args);
        String pathToModel = ns.getString("model");
        String format = ns.getString("format");
        boolean probabilities = ns.get("probabilities");
        String fastaPath = ns.getString("seq");
        String mafPath = ns.getString("maf");
        String db = ns.getString("db");
        String method = ns.getString("method");
        String model = ns.getString("model");

        String pathToPredictionFile = ns.getString("p");
        String pathToSeclibFile = ns.getString("r");
        String pathToSummaryFile = ns.getString("s");
        String pathToDetailedFile = ns.getString("d");

        // training
        if (method.equals("gor1")) {
            TrainerGOR1 trainer = new TrainerGOR1(db, 1);
            trainer.train(model);
        } else if (method.equals("gor3")) {
            TrainerGOR3 trainerGOR3 = new TrainerGOR3(db, 3);
            trainerGOR3.train(model);
            trainerGOR3.getSearchWindow().writeToFile(model);
        } else if (method.equals("gor4")) {
            TrainerGOR4 trainerGOR4 = new TrainerGOR4(db, 4);
            trainerGOR4.train(model);
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
                if (format.equals("txt")){
                    System.out.println(gorI);
                } else if (format.equals("html")) {
                    GORMain.toJson(gorI.getSequencesToPredict());
                }
            } else if (gorType == 3) {
                CalcGOR_III gorIII = new CalcGOR_III(pathToModel, fastaPath, probabilities);
                gorIII.predict();
                if (format.equals("txt")){
                    System.out.println(gorIII);
                } else if (format.equals("html")) {
                    GORMain.toJson(gorIII.getSequencesToPredict());
                }
            } else if (gorType == 4) {
                CalcGOR_IV gorIV = new CalcGOR_IV(pathToModel, fastaPath, probabilities);
                gorIV.predict();
                System.out.println(gorIV);
            }
        }
        // GOR V
        else {
            int gorType = GORMain.getGorType(pathToModel);
            CalcGOR_V gor_v = new CalcGOR_V(pathToModel, mafPath, gorType, probabilities);
            gor_v.predict();
            if (format.equals("txt")) {
                System.out.println(gor_v.predictionsToString(probabilities));
            }
            else if (format.equals("html")) {
                GORMain.toJson(gor_v.getSequencesToPredict());
            }

        }


        boolean toTxt = format.equals("txt");
        boolean plot = ns.getBoolean("b");

        ValidateGOR validateGor = new ValidateGOR(pathToSeclibFile, pathToPredictionFile, pathToSummaryFile, toTxt, pathToDetailedFile, plot);
    }
}
