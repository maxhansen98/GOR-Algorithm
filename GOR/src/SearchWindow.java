import java.util.HashMap;

public class SearchWindow {
    private int WINDOWSIZE;
    private static int AA_SIZE = 20; // fix value of aa dict size
    private HashMap<String, Integer[][]> secStructMatrices;
    private String[] secStructTypes = {"H", "E", "C"};

    public SearchWindow() {
        this.WINDOWSIZE = 17; // default
        this.secStructMatrices = new HashMap<>();
        this.initMatrices(this.secStructTypes);
    }
    public SearchWindow(int windowSize){
        this.WINDOWSIZE = windowSize;
    }
    public SearchWindow(String[] secStructTypes) {
        this.WINDOWSIZE = 17; // default
        this.secStructMatrices = new HashMap<>();
        this.initMatrices(secStructTypes);
    }
    public SearchWindow(int windowSize, String[] secStructTypes) {
        this.WINDOWSIZE = windowSize;
        this.secStructMatrices = new HashMap<>();
        this.initMatrices(secStructTypes);
    }

    public Integer[][] getMatrix(String secStruct){
        return this.secStructMatrices.get(secStruct);
    }
    @Override
    public String toString(){
        // std out all matrices :)
        StringBuilder out = new StringBuilder();
        for (String key : this.secStructMatrices.keySet()){
            out.append("Matrix of ").append(key).append(":\n");
            Integer[][] currSecMatrix = secStructMatrices.get(key);
            for (int i = 0; i < AA_SIZE; i++) {
                for (int j = 0; j < WINDOWSIZE; j++) {
                    out.append(currSecMatrix[i][j]).append(" ");
                }
                out.append("\n");
            }
            out.append("\n");
        }
        return out.toString();
    }

    public void initMatrices(String[] secStructTypes){
        // init a matrix for each secStructType
        for (String secStruct : secStructTypes){
            secStructMatrices.put(secStruct, new Integer[20][17]);

            // put default value into matrices
            for (int i = 0; i < AA_SIZE; i++) {
                for (int j = 0; j < WINDOWSIZE; j++) {
                    secStructMatrices.get(secStruct)[i][j] = 0;
                }
            }
        }
    }
}
