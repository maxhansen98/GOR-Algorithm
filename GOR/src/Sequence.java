import java.util.ArrayList;

public class Sequence {
    private final String id;
    private final String aaSequence;
    private String ssSequence;
    private ArrayList<Double> probabilities = new ArrayList<>();

    public Sequence(String id, String aaSequence, String ssSequence) {
        this.id = id;
        this.aaSequence = aaSequence;
        this.ssSequence = ssSequence;
    }

    public String getAaSequence() {
        return aaSequence;
    }
    public String getSsSequence() {
        return ssSequence;
    }
    public String getId() {
        return id;
    }

    public void setSsSequence(String ssSequence) {
        this.ssSequence = ssSequence;
    }

    public void extendSecStruct(char secStructType){
        this.ssSequence += secStructType;
    }

    public ArrayList<Double> getProbabilities() {
        return probabilities;
    }
}
