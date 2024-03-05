import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import utils.FileUtils;

public class SecLibFileReader {

    static ArrayList<Sequence> readSecLibFile(String pathToFile) throws IOException {
        // init
        File seqLibFile = new File(pathToFile);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);
        ArrayList<Sequence> sequences = new ArrayList<>();

        // convert content
        for (int i = 0; i < lines.size(); i++) {
            String currLine = lines.get(i);
            if (currLine.startsWith(">")){
                String pdbId = lines.get(i).substring(2); // get pdb id (maybe useful later
                String aaSequence  = lines.get(i+1).substring(3); // get AS seq
                String ssSequence = lines.get(i+2).substring(3); // get SS seq
                sequences.add(new Sequence(pdbId, aaSequence, ssSequence));
                // System.out.println(sequencePair[1]);
            }
        }
        return sequences;
    }

    public static void main(String[] args) throws IOException {
        ArrayList<Sequence> seqs = readSecLibFile("/home/malte/projects/blockgruppe3/GOR/CB513DSSP.db");
    }
}
