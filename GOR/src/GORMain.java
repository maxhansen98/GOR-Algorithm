import utils.CmdParser;
import java.io.IOException;
import java.util.HashMap;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

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


        CalcGOR_I gorI = new CalcGOR_I(pathToModel, fastaPath);
        HashMap<Character, Integer> test = gorI.calcStructureOccurrencies();
        gorI.predict();
        if (format.equals("txt")){
            System.out.println(gorI);
        }
    }
}
