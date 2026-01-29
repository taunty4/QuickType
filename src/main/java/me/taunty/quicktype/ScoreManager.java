package me.taunty.quicktype;


import java.io.IOException;
import java.nio.file.*;

public class ScoreManager {

    private static final String FILE_PATH = "highscore.txt";

    public static void saveHighScore(double score){
        try{
            Files.writeString(Paths.get(FILE_PATH), String.valueOf(score));
        } catch (IOException e){
            System.out.println("Could not save score: " + e.getMessage());
        }
    }

    public static double getHighScore(){
        try{
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path)){
                return 0.0;
            }
            String content = Files.readString(path);
            return Double.parseDouble(content.trim());
        } catch (Exception error){
            return 0.0;
        }

    }
}
