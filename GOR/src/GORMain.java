import utils.CmdParser;
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
        parser.addArgument("--seq").setDefault("");
        parser.addArgument("--maf").setDefault("");

        Namespace ns = parser.parseArgs(args);
        String pathToModel = ns.getString("model");
        String format = ns.getString("format");
        boolean probabilities = ns.get("probabilities");
        String fastaPath = ns.getString("seq");
        String mafPath = ns.getString("maf");

        int gorType = getGorType(pathToModel);


        if (gorType == 1){
            CalcGOR_I gorI = new CalcGOR_I(pathToModel, fastaPath, gorType);
            HashMap<Character, Integer> test = gorI.calcStructureOccurrencies();
            gorI.predict();
            if (format.equals("txt")){
                System.out.println(gorI);
            }
        } else if (gorType == 3) {
            CalcGOR_III gorIII = new CalcGOR_III(pathToModel, fastaPath, gorType);
            gorIII.predict();
            if (format.equals("txt")){
                System.out.println(gorIII);
            }
        } else if (gorType == 4) {
            CalcGOR_IV gorIV = new CalcGOR_IV(pathToModel, fastaPath, gorType);
            gorIV.predict();
            System.out.println(gorIV);
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
}
