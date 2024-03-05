package constants;

public enum Constants {
    AA_SIZE(20),
    WINDOW_SIZE(17);

    private final int value;

    Constants(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
