public class DBEntry {
    private String pdbId;
    private String aaSequence;
    private String ssSequence;

    public DBEntry(String pdbId,String aaSequence,String ssSequence) {
        this.pdbId = pdbId;
        this.aaSequence = aaSequence;
        this.ssSequence = ssSequence;
    }

    public String getAaSequence() {
        return aaSequence;
    }
    public String getSsSequence() {
        return ssSequence;
    }
    public String getPdbId() {
        return pdbId;
    }
}
