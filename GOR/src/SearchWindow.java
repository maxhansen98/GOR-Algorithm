import java.util.HashMap;

public class SearchWindow {
    private final int WINDOWSIZE = 17; // could be changed later in constructor
    private final int AA_SIZE = 20; // fix value of aa dict size
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
        return WINDOWSIZE;
    }
    @Override
    public String toString(){
        // std out all matrices :)
        StringBuilder out = new StringBuilder();
        for (char key : this.secStructMatrices.keySet()){
            out.append("=").append(key).append("=\n");
            Integer[][] currSecMatrix = secStructMatrices.get(key);
            for (int i = 0; i < AA_SIZE; i++) {
                out.append(this.INDEX_TO_AA.get(i) + "\t");
                for (int j = 0; j < WINDOWSIZE; j++) {
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
            for (int i = 0; i < AA_SIZE; i++) {
                for (int j = 0; j < WINDOWSIZE; j++) {
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
}
