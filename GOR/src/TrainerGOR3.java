import java.io.IOException;

public class TrainerGOR3 extends TrainerGOR1 {

    // call super constructor
    public TrainerGOR3(String pathToDB, int gorType) throws IOException {
        super(pathToDB, gorType);
    }

    @Override
    public void train(String pathToModelFile) throws IOException{
        // define main loop that goes over the sequences in training sequences
        // for each entry, init
        for (Sequence sequence: this.getTrainingSequences()) {
            // get entry content in readable vars
            String pdbId = sequence.getId();
            String aaSequence = sequence.getAaSequence();
            String ssSequence = sequence.getSsSequence();
            this.getSearchWindow().trainGor3(aaSequence, ssSequence, pdbId);
        }
        // this.getSearchWindow().writeToFile(pathToModelFile);
    }
}
