import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GORMain {
    public static void main(String[] args) throws IOException {
        CalcGOR_I gorI = new CalcGOR_I("/home/malte/projects/blockgruppe3/GOR/hobohm96.mod");
        HashMap<Character, Integer> test = gorI.calcStructureOccurrencies();
        System.out.println(test.get('C'));
        System.out.println(test.get('H'));
        System.out.println(test.get('E'));
        ArrayList<Sequence> test1 = gorI.readFasta("/home/malte/projects/blockgruppe3/GOR/cb513.fasta");
        Sequence s = test1.get(test1.size()-1);
        System.out.println(s.getId());
        System.out.println(s.getAaSequence());
    }
}
