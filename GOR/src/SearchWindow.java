import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class SearchWindow {
    private final HashMap<Character, Integer[][]> secStructMatrices = new HashMap<>();
    private HashMap<Integer, Character> INDEX_TO_AA = new HashMap<>();
    private char[] secStructTypes = {'H', 'E', 'C'};

    public SearchWindow() {
        this.initMatrices(this.secStructTypes);
        initINDEX_TO_AA();
    }

    public SearchWindow(int windowSize){
        if (windowSize % 2 == 0){
            // search window needs to be odd
            throw new IllegalArgumentException("Search window size needs to be an odd number!");
        }
        initINDEX_TO_AA();
        // this.WINDOWSIZE = windowSize;
        this.initMatrices(this.secStructTypes);
    }

    public SearchWindow(char[] secStructTypes) {
        // this.WINDOWSIZE = 17; // default
        initINDEX_TO_AA();
        this.initMatrices(secStructTypes);
    }

    public SearchWindow(int windowSize, char[] secStructTypes) {
        if (windowSize % 2 == 0){
            // search window needs to be odd
            throw new IllegalArgumentException("Search window size needs to be an odd number!");
        }
        // this.WINDOWSIZE = windowSize;
        initINDEX_TO_AA();
        this.initMatrices(secStructTypes);
    }

    public Integer[][] getMatrix(String secStruct){
        return this.secStructMatrices.get(secStruct);
    }

    public int getWINDOWSIZE() {
        return Constants.WINDOW_SIZE.getValue();
    }

    @Override
    public String toString(){
        // std out all matrices :)
        StringBuilder out = new StringBuilder();
        out.append("// Matrix3D\n");

        for (char key : this.secStructMatrices.keySet()){
            out.append("=").append(key).append("=\n");
            Integer[][] currSecMatrix = secStructMatrices.get(key);

            for (int i = 0; i < Constants.AA_SIZE.getValue(); i++) {
                out.append(this.INDEX_TO_AA.get(i) + "\t");

                for (int j = 0; j < Constants.WINDOW_SIZE.getValue(); j++) {
                    out.append(currSecMatrix[i][j]).append("\t");
                }
                out.append("\n");

            }
            out.append("\n");
        }
        return out.toString();
    }

    public void initMatrices(char[] secStructTypes){
        // init a matrix for each secStructType
        for (char secStruct : secStructTypes){
            this.secStructMatrices.put(secStruct, new Integer[20][17]);

            // put default value into matrices
            for (int i = 0; i <Constants.AA_SIZE.getValue(); i++) {
                for (int j = 0; j <Constants.WINDOW_SIZE.getValue(); j++) {
                    this.secStructMatrices.get(secStruct)[i][j] = 0;
                }
            }
        }
    }

    public HashMap<Character, Integer[][]> getSecStructMatrices() {
        return this.secStructMatrices;
    }

    public void initINDEX_TO_AA(){
        // maps AA to the row of the 2d matrix
        this.INDEX_TO_AA.put(0, 'A');
        this.INDEX_TO_AA.put(1, 'C');
        this.INDEX_TO_AA.put(2, 'D');
        this.INDEX_TO_AA.put(3, 'E');
        this.INDEX_TO_AA.put(4, 'F');
        this.INDEX_TO_AA.put(5, 'G');
        this.INDEX_TO_AA.put(6, 'H');
        this.INDEX_TO_AA.put(7, 'I');
        this.INDEX_TO_AA.put(8, 'K');
        this.INDEX_TO_AA.put(9, 'L');
        this.INDEX_TO_AA.put(10,'M');
        this.INDEX_TO_AA.put(11,'N');
        this.INDEX_TO_AA.put(12,'P');
        this.INDEX_TO_AA.put(13,'Q');
        this.INDEX_TO_AA.put(14,'R');
        this.INDEX_TO_AA.put(15,'S');
        this.INDEX_TO_AA.put(16,'T');
        this.INDEX_TO_AA.put(17,'V');
        this.INDEX_TO_AA.put(18,'W');
        this.INDEX_TO_AA.put(19,'Y');
    }

    public HashMap<Integer, Character> getINDEX_TO_AA() {
        return INDEX_TO_AA;
    }

    public void writeToFile(String modelFilePath) throws IOException {
        try (BufferedWriter buf = new BufferedWriter(new FileWriter(modelFilePath))) {
            // Get the string representation of your object
            String output = this.toString();
            // Write the output to the file
            buf.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
