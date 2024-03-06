import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// Due to extreme time pressure I have a lot of duplicate code
public class CalcGOR_III {

    private final SearchWindow window;
    private int gorType;
    private final ArrayList<Sequence> sequencesToPredict;
    // private final HashMap<Character, Integer> totalSecOcc;    // count of sec structs of model file

    public CalcGOR_III(String pathToModelFile, String fastaFile, int gorType) throws IOException {
        // temp init with the three sec types
        char[] secStructTypes = {'H', 'E', 'C'};
        this.window = new SearchWindow(pathToModelFile, gorType);
        //this.totalSecOcc = calcStructureOccurrenciesGor3();
        this.sequencesToPredict = readFasta(fastaFile);
        System.out.println(window.gor3ToString());
    }

    public  HashMap<Character, HashMap<Character, int[][]>> calcStructureOccurrenciesGor3(){
       return new HashMap<>();
    }


    public ArrayList<Sequence> readFasta(String fasta) throws IOException {
        BufferedReader buff = new BufferedReader(new FileReader(fasta));
        String line;
        StringBuilder sequence = new StringBuilder();
        ArrayList<Sequence> sequencesToPredict = new ArrayList<>();
        String currentId = "";

        while ((line = buff.readLine()) != null) {
            if (line.startsWith(">")) {
                if (!currentId.isEmpty()) {
                    // init secondary seq with default '--------'
                    sequencesToPredict.add(new Sequence(currentId, sequence.toString(), "--------"));
                    sequence.setLength(0); // Clear sequence StringBuilder
                }
                currentId = line;
            } else {
                sequence.append(line);
            }
        }

        // Add the last sequence (if any)
        if (!currentId.isEmpty()) {
            sequencesToPredict.add(new Sequence(currentId, sequence.toString(), "--------"));
        }

        buff.close(); // Close the BufferedReader
        return sequencesToPredict;
    }
}
