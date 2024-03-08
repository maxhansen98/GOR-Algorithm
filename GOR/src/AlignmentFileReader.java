import utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

public class AlignmentFileReader {

    public static ArrayList<Sequence> readAliDir(String pathToAliDir) {
        File dir = new File(pathToAliDir);
        File[] files = dir.listFiles();
        ArrayList<Sequence> alignmentSequences = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        Sequence sequence = readAliFile(file.getAbsolutePath());
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

    public static Sequence readAliFile(String pathToAli) throws IOException {
        File seqLibFile = new File(pathToAli);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);

        for (int i = 0; i < lines.size(); i++) {
            String currLine = lines.get(i);
            if (currLine.startsWith(">")){
                StringBuilder sb = new StringBuilder();
                String pdbId = lines.get(i).substring(2); // get pdb id (maybe useful later
                String aaSequence  = lines.get(i+1).substring(3); // get AS seq
                String validationSequenceSecondaryStruct = lines.get(i+2).substring(3); // get actual sec struct
                sb.append("-".repeat(Math.max(0,  8)));
                Sequence currSeq = new Sequence(pdbId, aaSequence,sb.toString());
                for (int j = 3; j < lines.size(); j++) {
                   String aliSeq = lines.get(j).substring(2);
                   currSeq.addAliSeq(aliSeq);
                }
                currSeq.setValidationSequenceSecondaryStructure(validationSequenceSecondaryStruct);
                sb.setLength(0); // reset just in case
                return currSeq;
            }
        }

        // this should never be reached
        return null;
    }
}
