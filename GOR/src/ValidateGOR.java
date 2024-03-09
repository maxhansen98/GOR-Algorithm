import constants.Constants;
import utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.stream.Stream;

public class ValidateGOR {

    private HashMap<String, Sequence> sequenceHashMap = new HashMap<>();
    private HashMap<String, ArrayList<Double>> summaryScores = new HashMap<>(); // stores all relevant scores
    private final int CHARS_BEFORE_ID = 2;
    private final int CHARS_BEFORE_SEQ = 3;
    private final int AA_SEQ_LINE = 1;
    private final int SS_SEQ_LINE = 2;
    private final char[] secStructs = {'H', 'E', 'C'};

    private final String[] orderOfScores = {"q3","qObs_H", "qObs_E", "qObs_C", "SOV", "SOV_H", "SOV_E", "SOV_C"};
    private final DecimalFormatSymbols symb = new DecimalFormatSymbols();
    private final DecimalFormat format;


    public ValidateGOR(String pathToSecLib, String pathToPredictions, String pathToSummaryFile, boolean toTxt, String pathToDetailedFile, boolean plot) throws IOException {
        initGlobalScores();
        symb.setDecimalSeparator('.');
        this.format = new DecimalFormat("0.0", symb);

        // init sequences using seclib file
        // reads aaSeq pdbId and valiSeq
        this.sequenceHashMap = readSecLibFile(pathToSecLib);

        // add predictions of predictionfile to seqs
        readPredictions(pathToPredictions);

        for (char ss : secStructs) {
            calculateQ0(ss);
        }
        calculateQ3();
        calculateSOV();
        writeToFile(generateDetailedSummary(), pathToDetailedFile);
        writeToFile(generateSummary(), pathToSummaryFile);
        writeToFile(generatePlottingFile(), pathToSecLib+"_toPlot.txt");

        if (plot) {
            try {
                String command = "python3 plotBoxplots.py " + pathToSecLib+"_toPlot.txt";
                // Create ProcessBuilder
                ProcessBuilder pb = new ProcessBuilder(command.split(" "));
                // Start the process
                Process process = pb.start();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void initGlobalScores(){
        this.summaryScores.put("SOV", new ArrayList<>());
        this.summaryScores.put("SOV_H", new ArrayList<>());
        this.summaryScores.put("SOV_E", new ArrayList<>());
        this.summaryScores.put("SOV_C", new ArrayList<>());
        this.summaryScores.put("q3", new ArrayList<>());
        this.summaryScores.put("qObs_H", new ArrayList<>());
        this.summaryScores.put("qObs_E", new ArrayList<>());
        this.summaryScores.put("qObs_C", new ArrayList<>());
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
        ArrayList<SequenceSegment> segmentsOvserved;
        ArrayList<SequenceSegment> segmentsPrediction;

        // acquire segments of each sequences
        for (Sequence sequence : this.sequenceHashMap.values()) {
            segmentsOvserved = getSegments(sequence.getValiSeq());
            segmentsPrediction = getSegments(sequence.getSsSequence());

            // generate overlaps for validationSegments
            generateOverlaps(segmentsOvserved, segmentsPrediction);
            double totalSov = 0;
            int totalNi = 0;

            for (char secType : secStructs) {
                double rightSum = 0.0;
                int ni = 0;

                for (SequenceSegment segment : segmentsOvserved) {
                    if (segment.getSecStruct() == secType) {
                        if (segment.getOverLaps().size() == 0) {
                            ni += segment.getAbsLength();
                        } else {
                            ni += segment.getAbsLength() * segment.getOverLaps().size();
                        }

                        for (SequenceSegment oSeq : segment.getOverLaps()) {
                            int minOv = segment.getMinOverlaps(oSeq);
                            int delta = calculateDelta(segment, oSeq);
                            int maxOv = segment.getMaxOverlaps(oSeq);
                            rightSum += (1.0 * (minOv + delta) / maxOv) * segment.getAbsLength();
                        }
                    }
                }

                if (!Double.isNaN((rightSum))) {
                    totalSov += rightSum;
                }
                totalNi += ni;
                double sovPerSecType = 100.0 * (1.0 / ni) * rightSum;
                sequence.getStatValues().put("SOV_" + secType, sovPerSecType);
                if (!(Double.isNaN(sovPerSecType))) {
                    this.summaryScores.get("SOV_" + secType).add(sovPerSecType);
                }
            }
            double sovTotal = 100.0 * (1.0 / totalNi) * totalSov;
            sequence.getStatValues().put("SOV", sovTotal);
            if (!(Double.isNaN(sovTotal))) {
                this.summaryScores.get("SOV").add(sovTotal);
            }

        }
    }

    public void generateOverlaps(ArrayList<SequenceSegment> segsOfVali, ArrayList<SequenceSegment> segsOfPred) {
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

    public ArrayList<SequenceSegment> getSegments(String sequence) {
        String regex = buildSOVRegex();
        ArrayList<SequenceSegment> segmentsOfSeq = new ArrayList<>();

        if (sequence.length() >= Constants.WINDOW_SIZE.getValue()) {
            String cutSeq = "        " + sequence.substring(Constants.WINDOW_SIZE.getValue() / 2, sequence.length() - Constants.AA_SIZE.getValue() / 2 + 2) + "        ";

            // save the segments in two lists
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(cutSeq);

            // get matches for referenceSequence
            while (matcher.find()) {
                String occurrence = matcher.group();
                int startIndex = matcher.start();
                int endIndex = matcher.end() - 1;
                char secStruct = cutSeq.charAt(startIndex); // get type of overlap
                segmentsOfSeq.add(new SequenceSegment(startIndex, endIndex, secStruct));
            }
        }
        return segmentsOfSeq;
    }

    private String buildSOVRegex() {
        StringBuilder regBuilder = new StringBuilder("(");
        for (char c : secStructs) {
            regBuilder.append(c).append("+|");
        }
        regBuilder.deleteCharAt(regBuilder.length() - 1); // Remove last |
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
            double q3 = 100.0 * countMatch / seqLength;
            String keyToValue = "Q3"; // build key :)
            sequence.getStatValues().put(keyToValue, q3);
            if (!(Double.isNaN(q3))) {
                this.summaryScores.get("q3").add(q3);
            }
        }
    }

    public int calculateDelta(SequenceSegment s1Vali, SequenceSegment s2Pred) {
        int maxOv = Math.max(s1Vali.getMaxOverlaps(s2Pred), s1Vali.getMinOverlaps(s2Pred));
        int minOv = Math.min(s1Vali.getMaxOverlaps(s2Pred), s1Vali.getMinOverlaps(s2Pred));
        int alpha1 = maxOv - minOv;
        int alpha3 = s1Vali.getAbsLength() / 2;
        int alpha4 = s2Pred.getAbsLength() / 2;
        return Math.min(alpha1, Math.min(minOv, Math.min(alpha3, alpha4)));
    }

    public void calculateQ0(char secStruct) {
        int startAt = Constants.WINDOW_SIZE.getValue() / 2;
        for (Sequence sequence : this.sequenceHashMap.values()) {
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
            double q0 = 100.0 * countOccMatch / countOccValid;
            String keyToValue = "Q" + secStruct; // build key :)
            String keyToValueGlobal = "qObs_" + secStruct; // build key :)
            sequence.getStatValues().put(keyToValue, q0);
            // also save score globally
            if (Double.isNaN(q0)) {
                this.summaryScores.get(keyToValueGlobal).add(0.0);
            } else {
                this.summaryScores.get(keyToValueGlobal).add(q0);
            }
        }
    }

    public HashMap<String, Sequence> getSequenceHashMap() {
        return sequenceHashMap;
    }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        int numOfProteins = this.sequenceHashMap.size();
        int sumOfProtLength = this.sequenceHashMap.values().stream().mapToInt(Sequence::getLength).sum();
        double meanOfProtLength = 1.0 * sumOfProtLength / numOfProteins;
        double sumOfPredPos = sequenceHashMap.values()
                .stream()
                .flatMap(seq -> seq.getSsSequence().chars().mapToObj(c -> (char) c))
                .filter(c -> c != '-')
                .count();

        sb.append("\n");
        sb.append("Statistic for protein validation \n\n");
        sb.append("Number of Proteins:           " + numOfProteins + "\n");
        sb.append("Mean Protein Length:          " + meanOfProtLength + "\n");
        sb.append("Sum of Protein Length:        " + sumOfProtLength + "\n");
        sb.append("Sum of Predicted Positions:   " + sumOfPredPos + "\n\n");
        for (String scoreType : orderOfScores) {
            sb.append(summaryLine(scoreType, this.summaryScores.get(scoreType)));
            if (scoreType.equals("qObs_C")){
               sb.append("\n");
            }
        }

        // System.out.println(sb.toString());
        // sb.append("Sum of Predicted Positions:   "+  + "\n");
        return sb.toString();
    }

    public String summaryLine(String score, ArrayList<Double> values) {
        StringBuilder sb = new StringBuilder();
        sb.append(score + " :\t\t");
        double mean = Math.abs(calculateMean(values));
        double dev = Math.abs(calculateDeviation(values, mean));
        double min = Math.abs(calculateMin(values));
        double max = Math.abs(calculateMax(values));
        double med = Math.abs(calculateMedian(values));
        double q25 = Math.abs(calculateQuantile(values, 25));
        double q75 = Math.abs(calculateQuantile(values, 75));
        double q5 = Math.abs(calculateQuantile(values, 5));
        double q95 = Math.abs(calculateQuantile(values, 95));
        sb.append("Mean:" + "\t" + format.format(mean) + "\t");
        sb.append("Dev:"+ "\t" + format.format(dev) + "\t");
        sb.append("Min:"+ "\t" + format.format(min) + "\t");
        sb.append("Max:"+ "\t" + format.format(max) + "\t");
        sb.append("Median:"+ "\t" + format.format(med) + "\t");
        sb.append("Quantil_25:"+ "\t" + format.format(q25) + "\t");
        sb.append("Quantil_75:"+ "\t" + format.format(q75) + "\t");
        sb.append("Quantil_5:"+ "\t" + format.format(q5) + "\t");
        sb.append("Quantil_95:"+ "\t" + format.format(q95));
        sb.append("\n");
        return sb.toString();
    }

    public static double calculateMean(ArrayList<Double> values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    public static double calculateDeviation(ArrayList<Double> values, double mean) {
        double sumOfSquares = 0.0;
        for (double value : values) {
            sumOfSquares += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sumOfSquares / values.size());
    }

    public static double calculateMin(ArrayList<Double> values) {
        return Collections.min(values);
    }

    public static double calculateMax(ArrayList<Double> values) {
        return Collections.max(values);
    }

    public static double calculateMedian(ArrayList<Double> values) {
        Collections.sort(values);
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }

    public static double calculateQuantile(ArrayList<Double> values, double percentile) {
        Collections.sort(values);
        int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
        return values.get(index);
    }

    public String generateDetailedSummary() {
        StringBuilder sb = new StringBuilder();

        for (Sequence s : this.getSequenceHashMap().values()) {
            HashMap<String, String> stringValues = new HashMap<>();
            // extract scores for clarity
            String seqId = s.getId();

            for (String key: s.getStatValues().keySet()) {
                double statValue = s.getStatValues().get(key);
                if (Double.isNaN(statValue)) {
                    stringValues.put(key, "-");
                }
                else {
                    stringValues.put(key, format.format(statValue));
                }
            }

            // build output >:)
            sb.append("\n> ").append(seqId).append(" ")
                    .append(stringValues.get("Q3")).append(" ")
                    .append(stringValues.get("SOV")).append(" ")
                    .append(stringValues.get("QH")).append(" ")
                    .append(stringValues.get("QE")).append(" ")
                    .append(stringValues.get("QC")).append(" ")
                    .append(stringValues.get("SOV_H")).append(" ")
                    .append(stringValues.get("SOV_E")).append(" ")
                    .append(stringValues.get("SOV_C")).append("\n")
                    .append("AS ").append(s.getAaSequence()).append("\n")
                    .append("PS ").append(s.getSsSequence())
                    .append('\n')
                    .append("SS ").append(s.getValiSeq())
                    .append("\n\n");
        }

        return sb.toString();
    }

    public String generatePlottingFile() {
        StringBuilder sb = new StringBuilder();

        for (Sequence s : this.getSequenceHashMap().values()) {
            HashMap<String, String> stringValues = new HashMap<>();
            // extract scores for clarity

            for (String key: s.getStatValues().keySet()) {
                double statValue = s.getStatValues().get(key);
                if (Double.isNaN(statValue)) {
                    stringValues.put(key, "0");
                }
                else {
                    stringValues.put(key, format.format(statValue));
                }
            }

            // build output >:)
            sb.append(stringValues.get("Q3")).append("\t")
                    .append(stringValues.get("SOV")).append("\t")
                    .append(stringValues.get("QH")).append("\t")
                    .append(stringValues.get("QE")).append("\t")
                    .append(stringValues.get("QC")).append("\t")
                    .append(stringValues.get("SOV_H")).append("\t")
                    .append(stringValues.get("SOV_E")).append("\t")
                    .append(stringValues.get("SOV_C")).append("\n");
        }

        return sb.toString();
    }

    public void writeToFile(String content, String pathOfFile) throws IOException {
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(pathOfFile))) {
                buf.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
