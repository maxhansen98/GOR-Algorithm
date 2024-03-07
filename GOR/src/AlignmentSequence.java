import java.util.ArrayList;

public class AlignmentSequence extends Sequence {
    private ArrayList<String> aliSequences = new ArrayList<>();
    public AlignmentSequence(String id, String aaSequence, String ssSequence){
        super(id, aaSequence, ssSequence);
    }

    public ArrayList<String> getAlignmennts() {
        return this.aliSequences;
    }

    public void addAliSeq(String alignment){
        this.aliSequences.add(alignment);
    }
}
