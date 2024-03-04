package utils;

public class Option {
    private boolean mandatory;
    private boolean isSwitch;
    private String arg;
    private String optionName;

    public Option(String name){
        this.optionName = name;
    }

    public String getArg() {
        return arg;
    }
    public String getOptionName(){
        return this.optionName;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }


    public void setMandatory(){
        this.mandatory = true;
    }
    public void setSwitch(){
        this.isSwitch = true;
    }

    public boolean getMandatory(){
        return this.mandatory;
    }

    public boolean getSwitch(){
        return this.isSwitch;
    }

}
