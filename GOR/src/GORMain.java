import utils.CmdParser;
import java.io.IOException;
import java.util.HashMap;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class GORMain {
    public static void main(String[] args) throws IOException, ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor("GOR").build().defaultHelp(true).description("Predict Secondary Structure");
        parser.addArgument("--format", "--type").choices("txt", "html").setDefault("txt");
        parser.addArgument("--model").help("File to calculate checksum");
        parser.addArgument("--probabilities").setDefault(false);
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
        gorI.printPredictions();
        if (format.equals("txt")){
            gorI.writeToTxt();
        }
        else {
            // TODO: to HTML
            gorI.writeToTxt();
        }
    }
}
