package me.taunty.quicktype;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class WordManager {
    private ArrayList<String> wordList;

    public WordManager(String filePath) throws IOException {
        this.wordList = new ArrayList<>();
        loadWords(filePath);
    }

    private void loadWords(String filePath) throws IOException{
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()){
            String word = scanner.nextLine().trim().toLowerCase();
            wordList.add(word);
        }
        if (wordList.isEmpty()){
            throw new IOException("File found but its empty, oops!");
        }
    }

    public String sentenceRandomiser(int amount){
        Collections.shuffle(wordList);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<amount && i<wordList.size(); i++){
            sb.append(wordList.get(i)).append(" ");
        }
        return sb.toString().trim();
    }
}
