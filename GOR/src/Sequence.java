import java.util.ArrayList;
import java.util.HashMap;

public class Sequence {
    private final String id;
    private final String aaSequence;
    private String ssSequence;
    private HashMap<Character, ArrayList<Integer>> normalizedProbabilities = new HashMap<>();
    private HashMap<Character, ArrayList<Double>> probabilities = new HashMap<>();

    private ArrayList<String> aliSequences = new ArrayList<>();

    public Sequence(String id, String aaSequence, String ssSequence) {
        this.id = id;
        this.aaSequence = aaSequence;
        this.ssSequence = ssSequence;
        this.probabilities.put('H', new ArrayList<>());
        this.probabilities.put('E', new ArrayList<>());
        this.probabilities.put('C', new ArrayList<>());
        this.normalizedProbabilities.put('H', new ArrayList<>());
        this.normalizedProbabilities.put('E', new ArrayList<>());
        this.normalizedProbabilities.put('C', new ArrayList<>());
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

    public HashMap<Character, ArrayList<Integer>> getNormalizedProbabilities() {
        return normalizedProbabilities;
    }

    public HashMap<Character, ArrayList<Double>> getProbabilities() {
        return probabilities;
    }

    public void updateProbabilities(char secType, Double prob) {
        this.probabilities.get(secType).add(prob);
    }

    public ArrayList<String> getAlignmennts() {
        return this.aliSequences;
    }

    public void addAliSeq(String alignment){
        this.aliSequences.add(alignment);
    }
}
