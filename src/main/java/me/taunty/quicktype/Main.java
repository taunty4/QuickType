package me.taunty.quicktype;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main extends Application {
    private Stage stage;
    private WordManager wordManager;
    private int currentIndex = 0;
    private TextFlow typingWords;
    private long startNanoTime = 0;
    private int errors = 0;
    private boolean isTimerRunning = false;
    private double secondsElapsed = 0;
    private double wpm = 0;
    private double rawWpm = 0;
    private double accuracy = 0;

    AnimationTimer wpmTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            updateWPM(now);
        }
    };

    private void updateWPM(long now){
        if (startNanoTime == 0){
            startNanoTime = now;
        }
        secondsElapsed = (now - startNanoTime) / 1000000000.0;
        if (secondsElapsed > 0){
            double minutes = secondsElapsed / 60.0;
            rawWpm = (currentIndex / 5.0) / minutes;

            int correctChars = currentIndex - errors;
            wpm = (correctChars / 5.0) / minutes;

            accuracy = ((double)correctChars / currentIndex) * 100;

        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.stage = primaryStage;
        // Setup window frame
        stage.setTitle("QuickType");
        Image keyboardIcon = new Image("keyboard.png");
        stage.getIcons().add(keyboardIcon);

        try {
            // Loads word manager on startup
            this.wordManager = new WordManager("word_catalogue.txt");
        } catch (IOException e){
            // returns error if file not found
            showErrorOnScreen(e.getMessage());
            return;
        }

        // Load menu
        showMenu();
        // Position window
        centerStage();
    }

    private void centerStage() {
        // Obtains correct bounds of the primary monitor
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        // Calculates correct center point using supplied bounds
        stage.setX(bounds.getMinX() + (bounds.getWidth() - 900) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - 500) / 2);

    }

    private void showMenu(){
        VBox menuLayout = new VBox(20);
        menuLayout.setStyle("-fx-background-color: #323437;");
        menuLayout.setAlignment(Pos.CENTER);

        Text menuTitle = new Text("QUICKTYPE");
        menuTitle.setFill(Color.web("#e2b714"));
        menuTitle.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");

        Button startGame = new Button("Start Game");
        startGame.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color:#e2b714");
        startGame.setOnAction(event -> showGame());

        Button settingsMenu = new Button("Settings");
        settingsMenu.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color:#e2b714");
        settingsMenu.setOnAction(event -> showSettings());

        menuLayout.getChildren().addAll(menuTitle, startGame, settingsMenu);

        Scene startMenu = new Scene(menuLayout, 900, 500);
        stage.setScene(startMenu);
        stage.show();
        menuLayout.requestFocus();
    }

    private void winScreen(){
        String formattedWPM = String.format("%.2f", wpm);
        String formattedRawWPM = String.format("%.2f", rawWpm);
        String formattedAccuracy = String.format("%.2f", accuracy);
        VBox winLayout = new VBox(20);
        winLayout.setStyle("-fx-background-color: #323437;");
        winLayout.setAlignment(Pos.CENTER);

        Text winTitle = new Text("TEST FINISHED");
        winTitle.setFill(Color.web("#e2b714"));
        winTitle.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");

        Text rawWpmResult = new Text("Raw WPM: " + formattedRawWPM);
        rawWpmResult.setFill(Color.web("#be95ff"));
        rawWpmResult.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text wpmResult = new Text("True WPM: " + formattedWPM);
        wpmResult.setFill(Color.web("#be95ff"));
        wpmResult.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text accuracy = new Text("Accuracy: %" + formattedAccuracy);
        accuracy.setFill(Color.web("#be95ff"));
        accuracy.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button backToMenu = new Button("Menu");
        backToMenu.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color:#e2b714");
        backToMenu.setOnAction(event -> showMenu());

        winLayout.getChildren().addAll(winTitle, rawWpmResult, wpmResult, accuracy, backToMenu);

        Scene winScreen = new Scene(winLayout, 900, 500);
        stage.setScene(winScreen);
        stage.show();
        winLayout.requestFocus();

    }

    private void showGame(){
        startNanoTime = 0;
        errors = 0;
        isTimerRunning = false;
        secondsElapsed = 0;
        wpm = 0;
        currentIndex = 0;
        VBox gameLayout = new VBox(20);
        gameLayout.setStyle("-fx-background-color: #323437;");
        gameLayout.setAlignment(Pos.CENTER);

        Button backToMenu = new Button("Menu");
        backToMenu.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color:#e2b714");
        backToMenu.setOnAction(event -> showMenu());

        typingWords = new TextFlow();
        typingWords.setMaxWidth(600);
        typingWords.setTextAlignment(TextAlignment.CENTER);
        String word = wordManager.sentenceRandomiser(5);
        for (char c : word.toCharArray()){
            Text letter = new Text(String.valueOf(c));
            letter.setFill(Color.web("#646669"));
            letter.setStyle("-fx-font-size: 32px; -fx-font-family: 'monospace';");
            typingWords.getChildren().add(letter);
        }

        gameLayout.getChildren().addAll(typingWords, backToMenu);
        updateLetterColor(0, Color.web("#646669"), true);
        Scene gameScene = new Scene(gameLayout, 900, 500);

        gameScene.setOnKeyTyped(event -> {
            if (!isTimerRunning){
                wpmTimer.start();
                isTimerRunning = true;
            }
            String typedChar = event.getCharacter();
            char expectedChar = word.charAt(currentIndex);
            if (typedChar.equals(String.valueOf(expectedChar))){
                updateLetterColor(currentIndex, Color.WHITE, false);
                currentIndex++;
            } else{
                updateLetterColor(currentIndex, Color.RED, false);
                errors++;
                currentIndex++;
            }

            if (currentIndex < typingWords.getChildren().size()){
                updateLetterColor(currentIndex, Color.web("#646669"), true);
            }

            if (currentIndex == typingWords.getChildren().size()){
                wpmTimer.stop();

                winScreen();
            }

        });


        stage.setScene(gameScene);
        stage.show();
        gameLayout.requestFocus();
    }

    private void updateLetterColor(int index, Color colour, boolean underlined){
        if (index >= 0 && index < typingWords.getChildren().size()){
            Text letter = (Text) typingWords.getChildren().get(index);
            letter.setUnderline(underlined);
            letter.setFill(colour);
        }
    }

    private void showSettings(){
        GridPane settingsLayout = new GridPane();
        settingsLayout.setStyle("-fx-background-color: #323437");
        settingsLayout.setAlignment(Pos.TOP_LEFT);
        settingsLayout.setHgap(40);
        settingsLayout.setVgap(20);

        Text wordCount = new Text("Word Count:");
        Text fillerSpace = new Text();
        wordCount.setFont(new Font(24));
        wordCount.setFill(Color.WHITE);

        ChoiceBox<Integer> wordCountBox = new ChoiceBox<>();
        wordCountBox.setPrefSize(100,40);
        wordCountBox.getItems().addAll(10, 25, 50);
        wordCountBox.setValue(25);

        Text themeSelection = new Text("Select Theme:");
        themeSelection.setFont(new Font(24));
        themeSelection.setFill(Color.WHITE);

        ChoiceBox<String> choiceOfTheme = new ChoiceBox<>();
        choiceOfTheme.setPrefSize(100,40);
        choiceOfTheme.getItems().addAll("Default", "Light", "Neon");
        choiceOfTheme.setValue("Default");

        Button backToMenu = new Button("Menu");
        backToMenu.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color:#e2b714");
        backToMenu.setOnAction(event -> showMenu());

        settingsLayout.add(fillerSpace, 0, 0);
        settingsLayout.add(wordCount, 0, 1);
        settingsLayout.add(wordCountBox, 1, 1);
        settingsLayout.add(themeSelection, 0, 2);
        settingsLayout.add(choiceOfTheme, 1, 2);
        settingsLayout.add(backToMenu, 0, 3);

        Scene settingsScene = new Scene(settingsLayout, 900, 500);
        stage.setScene(settingsScene);
        stage.show();
        settingsLayout.requestFocus();

    }

    private void showErrorOnScreen(String message){
        VBox errorLayout = new VBox(20);
        errorLayout.setStyle("-fx-background-color: #323437;");
        errorLayout.setAlignment(Pos.CENTER);

        Text errorTitle = new Text("FILE NOT FOUND");
        errorTitle.setFill(Color.web("#e2b714"));
        errorTitle.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");

        Text errorDescription = new Text(message);
        errorDescription.setFill(Color.WHITE);
        errorDescription.setStyle("-fx-font-size: 18px;");
        errorDescription.setTextAlignment(TextAlignment.CENTER);

        Button retryButton = new Button("Exit game");
        retryButton.setStyle("-fx-background-color: #e2b714; -fx-font-weight: bold;");
        retryButton.setOnAction(event -> stage.close());

        errorLayout.getChildren().addAll(errorTitle, errorDescription, retryButton);

        Scene errorScene = new Scene(errorLayout, 900, 500);
        stage.setScene(errorScene);
        stage.show();
    }
}
