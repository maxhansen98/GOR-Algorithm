import constants.Constants;
import utils.FileUtils;

import javax.swing.text.Segment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateGOR {

    private HashMap<String, Sequence> sequenceHashMap = new HashMap<>();
    final int CHARS_BEFORE_ID = 2;
    final int CHARS_BEFORE_SEQ = 3;
    final int AA_SEQ_LINE = 1;
    final int SS_SEQ_LINE = 2;
    final char[] secStructs = {'H', 'E', 'C'};


    public ValidateGOR(String pathToSecLib, String pathToPredictions, String pathToSummaryFile, boolean toTxt, String pathToDetailedFile) throws IOException {

        // init sequences using seclib file
        // reads aaSeq pdbId and valiSeq
        // this.sequenceHashMap = readSecLibFile(pathToSecLib);

        // add predictions of predictionfile to seqs
        // readPredictions(pathToPredictions);

        // testing
        Sequence testSequence = new Sequence("TEST", "");
        testSequence.setValiSeq("--------CCEEECCCCCCEEEEEECCC--------");
        testSequence.setSsSequence("--------CCCCCCCEEEEECCCEECCC--------");
        this.getSequenceHashMap().put("TEST", testSequence);

        for (char ss : secStructs) {
            calculateQ0(ss);
        }

        calculateQ3();
        calculateSOV();
        // Sequence t = sequenceHashMap.get("TEST");
        // System.out.println(t.getId());
        // System.out.println(t.getValiSeq());
        // System.out.println(t.getSsSequence());
        // System.out.println(t.getStatValues().get("QH"));
        // System.out.println(t.getStatValues().get("QC"));
        // System.out.println(t.getStatValues().get("QE"));
        // System.out.println(t.getStatValues().get("Q3"));
    }

    public void readPredictions(String pathToPredictions) throws IOException {
        // init
        File seqLibFile = new File(pathToPredictions);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);

        // we read the predictions and store the predicted ss struct in a hashmap >:)
        HashMap<String, String> idToPrediction = getStringStringHashMap(lines);

        // now add the predictions to the Sequences of validation
        for (String id : idToPrediction.keySet()) {
            if (sequenceHashMap.containsKey(id)) {
                Sequence currSeq = sequenceHashMap.get(id);
                currSeq.setSsSequence(idToPrediction.get(id));
            }
            // error handling
            else {
                System.out.println("Predictions contain sequences which are not present in ");
            }
        }
    }

    private HashMap<String, String> getStringStringHashMap(ArrayList<String> lines) {
        HashMap<String, String> idToPrediction = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String currLine = lines.get(i);
            if (currLine.startsWith(">")) {
                String pdbId = lines.get(i).substring(CHARS_BEFORE_ID);
                String predSecStruct = lines.get(i + SS_SEQ_LINE).substring(CHARS_BEFORE_SEQ);
                idToPrediction.put(pdbId, predSecStruct);
            }
        }
        return idToPrediction;
    }

    public HashMap<String, Sequence> readSecLibFile(String pathToFile) throws IOException {
        // init
        File seqLibFile = new File(pathToFile);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);
        HashMap<String, Sequence> sequences = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            String currLine = lines.get(i);
            if (currLine.startsWith(">")) {
                String pdbId = lines.get(i).substring(CHARS_BEFORE_ID); // get pdb id (maybe useful later
                String aaSequence = lines.get(i + AA_SEQ_LINE).substring(CHARS_BEFORE_SEQ); // get AS seq
                String validationSequenceSec = lines.get(i + SS_SEQ_LINE).substring(CHARS_BEFORE_SEQ); // get SS seq
                Sequence seq = new Sequence(pdbId, aaSequence);
                seq.setValiSeq(validationSequenceSec);
                sequences.put(pdbId, seq);
            }
        }
        return sequences;
    }

    public void calculateSOV() {
        ArrayList<SequenceSegment> segsOfVali = new ArrayList<>();
        ArrayList<SequenceSegment> segsOfPred = new ArrayList<>();

        for (Sequence sequence : this.sequenceHashMap.values()) {
            segsOfVali = getSegments(sequence.getValiSeq());
            segsOfPred = getSegments(sequence.getSsSequence());
        }

        // generate overlaps for validationSegments
        generateOverlaps(segsOfVali, segsOfPred);

        for (SequenceSegment segment: segsOfVali) {
            // System.out.println("SEGMENT " + segment.getStartIndex() + " " + segment.getEndIndex());
            for (SequenceSegment oSeq: segment.getOverLaps()) {
                System.out.println("MAX OV " + segment.getMaxOverlaps(oSeq));
                System.out.println("MIN OV " + segment.getMinOverlaps(oSeq));
                System.out.println(calculateDelta(segment, oSeq));
            }
        }
    }

    public void generateOverlaps (ArrayList<SequenceSegment> segsOfVali, ArrayList<SequenceSegment> segsOfPred) {
        for (SequenceSegment valiSegment : segsOfVali) {
            for (SequenceSegment predSegment : segsOfPred) {
                if (valiSegment.getSecStruct() == predSegment.getSecStruct()) {
                    int overlapStart = Math.max(valiSegment.getStartIndex(), predSegment.getStartIndex());
                    int overlapEnd = Math.min(valiSegment.getEndIndex(), predSegment.getEndIndex());
                    if (overlapStart <= overlapEnd) {
                        // if we have an overlap, add that to our vali seq
                        valiSegment.getOverLaps().add(predSegment);
                    }
                }
            }
        }
    }

    public ArrayList<SequenceSegment> getSegments(String sequence){
        String regex = buildSOVRegex();

        // save the segments in two lists
        ArrayList<SequenceSegment> segmentsOfSeq = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sequence);

        // get matches for referenceSequence
        while (matcher.find()) {
            String occurrence = matcher.group();
            int startIndex = matcher.start();
            int endIndex = matcher.end() - 1;
            char secStruct = sequence.charAt(startIndex); // get type of overlap
            segmentsOfSeq.add(new SequenceSegment(startIndex, endIndex, secStruct));
        }
        return segmentsOfSeq;
    }

    private String buildSOVRegex() {
        StringBuilder regBuilder = new StringBuilder("(");
        for (char c : secStructs) {
            regBuilder.append(c).append("+|");
        }
        regBuilder.deleteCharAt(regBuilder.length()-1); // Remove last |
        regBuilder.append(")");
        return regBuilder.toString();
    }

    public void calculateQ3() {
        int startAt = Constants.WINDOW_SIZE.getValue() / 2;
        for (Sequence sequence : this.sequenceHashMap.values()) {
            int countMatch = 0;
            char[] predSeqArr = sequence.getSsSequence().toCharArray();
            char[] validSeqArr = sequence.getValiSeq().toCharArray();

            for (int i = startAt; i < predSeqArr.length - startAt; i++) {
                if (validSeqArr[i] == predSeqArr[i]) {
                    countMatch++;
                }
            }
            int seqLength = (predSeqArr.length - (2 * startAt));
            double q3 = 1.0 * countMatch / seqLength;
            String keyToValue = "Q3"; // build key :)
            sequence.getStatValues().put(keyToValue, q3);
        }
    }

     public int calculateDelta(SequenceSegment s1Vali, SequenceSegment s2Pred) {
         int maxOv = s1Vali.getMaxOverlaps(s2Pred);
         int minOv = s1Vali.getMinOverlaps(s2Pred); //alpha2
         int alpha1 = maxOv - minOv;
         int alpha3 = s1Vali.getAbsLength() / 2;
         int alpha4 = s2Pred.getAbsLength() / 2;
         int test = Math.min(alpha1, Math.min(minOv, Math.min(alpha3, alpha4)));
         return Math.min(alpha1, Math.min(minOv, Math.min(alpha3, alpha4)));
     }

    public void calculateQ0(char secStruct) {
        int startAt  = Constants.WINDOW_SIZE.getValue() / 2;
        for (Sequence sequence: this.sequenceHashMap.values()) {
            int countOccValid = 0;
            int countOccMatch = 0;
            char[] predSeqArr = sequence.getSsSequence().toCharArray();
            char[] validSeqArr = sequence.getValiSeq().toCharArray();
            for (int i = startAt; i < predSeqArr.length - startAt; i++) {
                if (validSeqArr[i] == secStruct) {
                    countOccValid++;
                    if (predSeqArr[i] == secStruct) {
                        countOccMatch++;
                    }
                }
            }
            double q0 = 1.0 * countOccMatch / countOccValid;
            String keyToValue = "Q" + secStruct; // build key :)
            sequence.getStatValues().put(keyToValue, q0);
        }
    }

    public HashMap<String, Sequence> getSequenceHashMap() {
        return sequenceHashMap;
    }

    public String generateSummary(){
        StringBuilder sb = new StringBuilder();
        for (Sequence s : this.getSequenceHashMap().values()) {
            HashMap<String, Double> values = s.getStatValues();
            // extract scores for clarity
            String seqId = s.getId();
            double q3 = values.get("Q3");
            double sov = values.get("SOV");
            double qH = values.get("QH");
            double qE = values.get("QE");
            double qC = values.get("QC");
            double sovH = values.get("SOVH");
            double sovE = values.get("SOVE");
            double sovC =  values.get("SOVC");

            // build output >:)
            sb.append(">").append(seqId).append(" ")
                    .append(q3).append(" ")
                    .append(sov).append(" ")
                    .append(qH).append(" ")
                    .append(qE).append(" ")
                    .append(qC).append(" ")
                    .append(sovH).append(" ")
                    .append(sovE).append(" ")
                    .append(sovC).append("\n");
            sb.append("AS ").append(s.getAaSequence()).append("\n");
            sb.append("PS ").append(s.getSsSequence());
            sb.append("-".repeat(Math.max(0, Constants.WINDOW_SIZE.getValue())/ 2));
            sb.append('\n');

        }

        return sb.toString();
    }
}
