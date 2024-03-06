import utils.CmdParser;

import java.io.IOException;

public class TrainerMain {
    public static void main(String[] args) throws IOException {
        CmdParser cmd = new CmdParser("--db", "--method", "--model");
        cmd.setMandatoryOpts("--db", "--method", "--model");
        cmd.parse(args);
        String db = cmd.getOptionValue("--db");
        String method = cmd.getOptionValue("--method");
        String model = cmd.getOptionValue("--model");
        // System.out.println(db);
        // System.out.println(method);
        // System.out.println(model);
        if (method.equals("gor1")) {
            TrainerGOR1 trainer = new TrainerGOR1(db, 1);
            trainer.train(model);
        } else if (method.equals("gor3")) {
            TrainerGOR3 trainerGOR3 = new TrainerGOR3(db);
            trainerGOR3.train(model);
            trainerGOR3.getSearchWindow().writeToFile(model);
        } else if (method.equals("gor4")) {
            TrainerGOR4 trainerGOR4 = new TrainerGOR4(db, 4);
            trainerGOR4.train(model);
        }

        // "/home/malte/projects/blockgruppe3/GOR/CB513DSSP.db"
    }
}