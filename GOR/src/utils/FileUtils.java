package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class FileUtils {
    public static ArrayList<String> readLines(File r) throws IOException {
        ArrayList<String> fileLines = new ArrayList<String>();
        BufferedReader buff = new BufferedReader(new FileReader(r));

        String line;
        while((line = buff.readLine()) != null){
            fileLines.add(line);
        }
        return fileLines;
    }
    public static ArrayList<String[]> readTsv(File f, String sep) throws IOException {
        ArrayList<String[]> rows = new ArrayList<String[]>();
        BufferedReader buff = new BufferedReader(new FileReader(f));
        String line;
        while((line = buff.readLine()) != null){
            String[] row = line.split(sep);
            rows.add(row);
        }
        return rows;
    }


    public static <X,Y> HashMap<X, Y> readMap(File f, int keyCol, int valCol, String sep,
                                              Function<String, X> convertKey,
                                              Function<String, Y> convertVal) throws IOException{
        if(keyCol == valCol){
            System.out.println("KeyCol shouldn't be equal to valCol!");
        }
        HashMap<X, Y> filteredCols = new HashMap<>();
        BufferedReader buff = new BufferedReader(new FileReader(f));

        String line;
        while((line = buff.readLine()) != null){
            String[] row = line.split("\t");
            X key = convertKey.apply(row[keyCol]);
            Y val = convertVal.apply(row[valCol]);
            filteredCols.put(key, val);
        }
        return filteredCols;
    }
    public static HashMap<String,String> readMap(File f, String sep)throws IOException{
        Function<String, String> def = x -> x; // default function returns String
        return readMap(f, 0, 1, sep, def, def);
    }

    public static void main(String[] args) throws IOException {
        File f = new File("/home/malte/IdeaProjects/BioinformatikTutorium_FileUtils/src/utils/data.tsv");
        HashMap<String, String> map = readMap(f, "\t");
        System.out.println(map.keySet());
        System.out.println(map.values());
        readMap(f, 0 , 1, "\t", Integer::parseInt, Integer::parseInt);
    }
}