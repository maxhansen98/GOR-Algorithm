import utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

public class AlignmentFileReader {

    public static ArrayList<AlignmentSequence> readAliDir(String pathToAliDir) {
        File dir = new File(pathToAliDir);
        File[] files = dir.listFiles();
        ArrayList<AlignmentSequence> alignmentSequences = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        AlignmentSequence sequence = readAliFile(file.getAbsolutePath());
                        alignmentSequences.add(sequence);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("Failed to read directory: " + pathToAliDir);
        }
        return alignmentSequences;
    }

    public static AlignmentSequence readAliFile(String pathToAli) throws IOException {
        File seqLibFile = new File(pathToAli);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);

        for (int i = 0; i < lines.size(); i++) {
            String currLine = lines.get(i);
            if (currLine.startsWith(">")){
                String pdbId = lines.get(i).substring(2); // get pdb id (maybe useful later
                String aaSequence  = lines.get(i+1).substring(3); // get AS seq
                String ssSequence = lines.get(i+2).substring(3); // get SS seq
                AlignmentSequence currSeq = new AlignmentSequence(pdbId, aaSequence,ssSequence);
                for (int j = 3; j < lines.size(); j++) {
                   String aliSeq = lines.get(j).substring(2);
                   currSeq.addAliSeq(aliSeq);
                }
                return currSeq;
            }
        }

        // this should never be reached
        return null;
    }
}
