package constants;

public enum Constants {
    AA_SIZE(20),
    WINDOW_SIZE(17);

    private int value;

    Constants(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setWindowSize(int newValue) {
        if (this == WINDOW_SIZE) {
            if (newValue % 2 != 0) {
                this.value = newValue;
            } else {
                throw new RuntimeException("WINDOW SIZE NEEDS TO BE ODD!");
            }
        }
    }

}
