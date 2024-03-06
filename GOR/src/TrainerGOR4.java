import java.io.IOException;

public class TrainerGOR4 extends TrainerGOR1 {

    public TrainerGOR4(String pathToDBfile, int gorType) throws IOException {
        super(pathToDBfile, gorType);
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
        this.getSearchWindow().writeToFile(pathToModelFile);
    }
}
