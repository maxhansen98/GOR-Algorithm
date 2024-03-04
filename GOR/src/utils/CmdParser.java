package utils;

import java.util.HashMap;
import java.util.HashSet;

public class CmdParser {
   private final HashMap<String, Option> optionsMap = new HashMap<String, Option>();
   private HashSet<Option> mandatoryOptions = new HashSet<>();
   private HashSet<Option> inputSwitches = new HashSet<>();
    public CmdParser(String... options){
        for (String option : options) {
           this.optionsMap.put(option, new Option(option));
        }
    }
    public void setMandatoryOpts(String... options){
        for (String opt : options) {
            this.optionsMap.get(opt).setMandatory();
            // Add option to mandatory set
            mandatoryOptions.add(optionsMap.get(opt));
        }
    }

    public void setSwitches(String... options){
        for (String opt : options) {
            this.optionsMap.get(opt).setSwitch();
        }
    }

    public void parse(String[] args){
        int i = 1; // variable used for indexing args, start at 1 and add 2 for each loop
        for (String arg: args) {
            if(optionsMap.containsKey(arg)){ // -input
                if(!optionsMap.get(arg).getSwitch()) {
                    optionsMap.get(arg).setArg(args[i]);
                    i+=2;
                } else{
                    // add switch to inputSwitches
                    inputSwitches.add(optionsMap.get(arg));
                    i++; // skip switch
                }
            }
        }

        // check, if mandatory options were given as input
        // NOTE: this assumes that switche can't be mandatory (if a switch was mandatory then why would it be a switch in the first place)
        if (!mandatoryOptions.isEmpty()){
            for (Option opt : mandatoryOptions) {
                if(opt.getArg() == null){
                    throw new RuntimeException("Mandatory option is missing");
                }
            }
        }
    }
    public String getOptionValue(String opt){
        return this.optionsMap.get(opt).getArg();
    }
    public HashSet<Option> getInputSwitches(){
        return this.inputSwitches;
    }
    public boolean isSet(String opt) {
        return optionsMap.get(opt).getSwitch();
    }
    public HashMap<String,Option> getOptionsMap(){
        return this.optionsMap;
    }
}