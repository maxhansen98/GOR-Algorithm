import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GORMain {
    public static void main(String[] args) throws IOException {
        CalcGOR_I gorI = new CalcGOR_I("hobohm96.mod", "cb513.fasta");
        HashMap<Character, Integer> test = gorI.calcStructureOccurrencies();
        System.out.println(Math.log((float)test.get('C')/(test.get('H')+test.get('E'))));
        System.out.println(Math.log((float)test.get('H')/(test.get('C')+test.get('E'))));
        System.out.println(Math.log((float)test.get('E')/(test.get('H')+test.get('C'))));
        // ArrayList<Sequence> test1 = gorI.readFasta("cb513.fasta");
        // Sequence s = test1.get(test1.size()-1);
        // System.out.println(s.getId());
        // System.out.println(s.getAaSequence());
        gorI.predict();
        gorI.printPredictions();
    }
}
