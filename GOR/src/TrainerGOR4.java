import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TrainerGOR4 extends TrainerGOR1 {

    TrainerGOR3 trainerGOR3;
    public TrainerGOR4(String pathToDBfile, int gorType) throws IOException {
        super(pathToDBfile, gorType);
        trainerGOR3 = new TrainerGOR3(pathToDBfile, gorType); // also create a Gor3Trainer
    }

    @Override
    public void train(String pathToModelFile) throws IOException {
        // define main loop that goes over the sequences in training sequences
        // for each entry, init
        for (Sequence sequence: this.getTrainingSequences()) {
            // get entry content in readable vars
            String pdbId = sequence.getId();
            String aaSequence = sequence.getAaSequence();
            String ssSequence = sequence.getSsSequence();
            this.getSearchWindow().trainGor4(aaSequence, ssSequence, pdbId);
        }
        this.trainerGOR3.train(pathToModelFile);
        writeGor4ToFile(pathToModelFile);
    }

    public void writeGor4ToFile(String modelFilePath) throws IOException {
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(modelFilePath))) {
            // Get the string representation of your object
                String gor4outPut = this.getSearchWindow().gor4ToString();
                buf.write(gor4outPut);
                String gor3outPut = this.trainerGOR3.getSearchWindow().gor3ToString();
                buf.write(gor3outPut);
        }
            // Write the output to the file
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
