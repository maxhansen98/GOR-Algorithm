import java.io.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

public class ValidateMain {
    public static void main(String[] args) throws IOException, ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor("Validate").build().defaultHelp(true).description("Validate Secondary Structure Prediction");
        parser.addArgument("-p").help("Prediction File");
        parser.addArgument("-r").help("seclib-file");
        parser.addArgument("-s").help("summary file");
        parser.addArgument("-d").help("detailed file");
        parser.addArgument("-f").choices("txt", "html").setDefault("txt");
        parser.addArgument("-b").action(storeTrue()); // b for boxplot

        Namespace ns = parser.parseArgs(args);
        String pathToPredictionFile = ns.getString("p");
        String pathToSeclibFile = ns.getString("r");
        String pathToSummaryFile = ns.getString("s");
        String pathToDetailedFile = ns.getString("d");
        String format = ns.getString("f");
        boolean toTxt = format.equals("txt");
        boolean plot = ns.getBoolean("b");

        ValidateGOR validateGor = new ValidateGOR(pathToSeclibFile, pathToPredictionFile, pathToSummaryFile, toTxt, pathToDetailedFile, plot);
    }
}