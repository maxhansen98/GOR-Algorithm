import java.io.File;
import java.util.HashMap;
import java.io.IOException;
import java.util.ArrayList;
import utils.FileUtils;

public class SecLibFileReader {

    static ArrayList<String[]> readSecLibFile(String pathToFile) throws IOException {
        File seqLibFile = new File(pathToFile);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);
        ArrayList<String[]> sequences = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String currLine = lines.get(i);
            if (currLine.startsWith(">")){
                String[] sequencePair = new String[3];
                sequencePair[0] = lines.get(i+1).substring(3); // get AS seq
                sequencePair[1] = lines.get(i+2).substring(3); // get SS seq
                sequencePair[2] = lines.get(i).substring(2); // get pdb id (maybe useful later
                sequences.add(sequencePair);
                // System.out.println(sequencePair[1]);
            }
        }
        return sequences;
    }

    public static void main(String[] args) throws IOException {
        ArrayList<String[]> seqs = readSecLibFile("/home/malte/projects/blockgruppe3/GOR/CB513DSSP.db");
    }
}
