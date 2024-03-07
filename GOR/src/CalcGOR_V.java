import java.io.IOException;
import java.util.ArrayList;

public class CalcGOR_V {

    private SearchWindow gorX;
    private final ArrayList<AlignmentSequence> sequencesToPredict;

    public CalcGOR_V(String pathToModel, String mafPath, int gorType) throws IOException {

        this.gorX = new SearchWindow(pathToModel, gorType);
        this.sequencesToPredict = AlignmentFileReader.readAliDir(mafPath);

        System.out.println("");
        //this.window = new SearchWindow();

    }
}
