import constants.Constants;

import java.util.ArrayList;
import java.util.HashMap;

public class Sequence {
    private final String id;
    private final String aaSequence;
    private String ssSequence;
    private String valiSeq;

    // store probabilities for validation
    private final HashMap<String, Double> statValues = new HashMap<>();

    // predictionProbabilities
    private final HashMap<Character, ArrayList<Integer>> normalizedProbabilities = new HashMap<>();
    private final HashMap<Character, ArrayList<Double>> probabilities = new HashMap<>();

    private final ArrayList<String> aliSequences = new ArrayList<>();

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

    public Sequence(String id, String aaSequence) {
        this.id = id;
        this.aaSequence = aaSequence;
        this.ssSequence = "";
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

    public String getValiSeq() {
        return valiSeq;
    }

    public void setValiSeq(String valiSeq) {
        this.valiSeq = valiSeq;
    }

    public HashMap<String, Double> getStatValues() {
        return statValues;
    }
    public String toJson() {
        StringBuilder jsonBuilder = new StringBuilder();
        StringBuilder tailBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"id\": \"" + id + "\", ");
        String ssSeqWithTail = ssSequence;
        String tail = tailBuilder.append("-".repeat(Math.max(0, Constants.WINDOW_SIZE.getValue()) / 2)).toString();
        if (!(ssSeqWithTail.endsWith("-"))) {
            ssSeqWithTail += tail;
        }
        jsonBuilder.append("\"aaSequence\": \"" + aaSequence + "\", ");
        jsonBuilder.append("\"ssSequence\": \"" + ssSeqWithTail + "\", ");
        jsonBuilder.append("\"valiSeq\": \"" + valiSeq + "\", ");

        jsonBuilder.append("\"normalizedProbabilities\": {");
        for (char key : normalizedProbabilities.keySet()) {
            jsonBuilder.append("\"" + key + "\": [");
            ArrayList<Integer> probs = normalizedProbabilities.get(key);
            for (int i = 0; i < probs.size(); i++) {
                jsonBuilder.append(probs.get(i));
                if (i < probs.size() - 1) {
                    jsonBuilder.append(", ");
                }
            }
            jsonBuilder.append("], ");
        }
        jsonBuilder.deleteCharAt(jsonBuilder.length() - 2);
        jsonBuilder.append("}, ");

        jsonBuilder.delete(jsonBuilder.length() - 2, jsonBuilder.length());
        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }
}
