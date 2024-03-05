import utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CalcGOR_I {
    private final HashMap<Character, Integer[][]> secStructMatrices = new HashMap<>();
    public CalcGOR_I(){
        // temp init with the three sec types
        char[] secStructTypes = {'H', 'E', 'C'};
        initMatrices(secStructTypes);
    }

    public void initMatrices(char[] secStructTypes){
        // init a matrix for each secStructType
        for (char secStruct : secStructTypes){
            this.secStructMatrices.put(secStruct, new Integer[20][17]);

            // put default value into matrices
            for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                for (int j = 0; j < Constants.WINDOW_SIZE.getValue(); j++) {
                    this.secStructMatrices.get(secStruct)[i][j] = 0;
                }
            }
        }
    }

    public void readModFile(String pathToModFile) throws IOException {
        File seqLibFile = new File(pathToModFile);
        ArrayList<String> lines = FileUtils.readLines(seqLibFile);
        for (int i = 0; i < lines.size(); i++) {
           if (lines.get(i).startsWith("=C=")){
               String[] line = lines.get(i).split("\t");
               for (int j = 0; j < line.length; j++) {

               }

           }
           else if (lines.get(i).startsWith("=H=")){

           }
           else if (lines.get(i).startsWith("=E=")){

           }
        }

    }
}
