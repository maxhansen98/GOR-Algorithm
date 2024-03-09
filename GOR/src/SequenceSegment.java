import java.util.ArrayList;

public class SequenceSegment {
    private int startIndex;
    private int endIndex;
    private char secStruct;
    private int absLength;
    private ArrayList<SequenceSegment> overLaps = new ArrayList<>();

    public SequenceSegment(int startIndex, int endIndex, char secStruct) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.secStruct = secStruct;
        this.absLength = endIndex - startIndex + 1;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public char getSecStruct() {
        return secStruct;
    }

    public ArrayList<SequenceSegment> getOverLaps() {
        return overLaps;
    }

    public int getMinOverlaps(SequenceSegment s2) {
        int start = Math.max(this.startIndex, s2.getStartIndex());
        int end = Math.min(this.endIndex, s2.getEndIndex());
        return end - start + 1;
    }

    public int getMaxOverlaps(SequenceSegment s2) {
        int start = Math.min(this.startIndex, s2.getStartIndex());
        int end = Math.max(this.endIndex, s2.getEndIndex());
        return end - start + 1;
    }

    public int getAbsLength() {
        return absLength;
    }
}
