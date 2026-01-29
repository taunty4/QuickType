package me.taunty.quicktype;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

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
    private String currentTheme = "Dark";
    private int textWordCount = 5;

    AnimationTimer wpmTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            updateWPM(now);
        }
    };

    // Declares background colour for respected theme
    private String getBackgroundColour(){
        return switch (currentTheme) {
            case "Light" -> "#ffffff";
            case "Cyberpunk" -> "#0F0A1E";
            default -> "#323437";
        };
    }

    // Declares typeable letter colour for respected theme
    private String getPrimaryColour(){
        return switch (currentTheme){
            case "Light" -> "#d5d5d5";
            case "Cyberpunk" -> "#E5E7EB";
            default -> "#646669";
        };
    }

    // Declares typed colour for respected theme
    private String getTypedColour(){
        return switch (currentTheme){
            case "Light" -> "#000000";
            case "Cyberpunk" -> "#00F0FF";
            default -> "#ffffff";
        };
    }

    // Calculates the users WPM using elapsed time and correct character count
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

            if (currentIndex>0){
                accuracy = ((double)correctChars / currentIndex) * 100;
            }

        }
    }

    // Moves cursor back by one, recalculating error count and resetting visual style of character.
    private void handleBackspace(){
        if (currentIndex > 0){
            currentIndex--;
            updateLetterColour(currentIndex, getPrimaryColour(), true);
            Text previousNode = (Text) typingWords.getChildren().get(currentIndex);
            if (previousNode.getFill().equals(Color.web("#FF0000"))){
                errors--;
            }

            if (currentIndex+1 < typingWords.getChildren().size()){
                updateLetterColour(currentIndex+1, getPrimaryColour(), false);
            }
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

    // Constructs and displays the main menu scene.
    private void showMenu(){
        VBox menuLayout = new VBox(20);
        menuLayout.setStyle("-fx-background-color: " + getBackgroundColour() + ";");
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

    // Constructs and displays the scene post test.
    private void winScreen(){
        String formattedWPM = String.format("%.2f", wpm);
        String formattedRawWPM = String.format("%.2f", rawWpm);
        String formattedAccuracy = String.format("%.2f", accuracy);

        double currentBest = ScoreManager.getHighScore();
        boolean isNewRecord = false;

        if (wpm > currentBest){
            ScoreManager.saveHighScore(wpm);
            currentBest = wpm;
            isNewRecord = true;

        }

        Text winTitle = new Text("TEST FINISHED");
        winTitle.setFill(Color.web("#e2b714"));
        winTitle.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");

        Text rawWpmResult = new Text("Raw WPM: " + formattedRawWPM);
        rawWpmResult.setFill(Color.web("#e2b714"));
        rawWpmResult.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text bestWPM = new Text("New personal best: " + String.format("%.2f",currentBest) + " WPM");
        bestWPM.setFill(Color.web("#00FF9F"));
        bestWPM.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text wpmResult = new Text("True WPM: " + formattedWPM);
        wpmResult.setFill(Color.web("#e2b714"));
        wpmResult.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text accuracy = new Text("Accuracy: " + formattedAccuracy + "%");
        accuracy.setFill(Color.web("#e2b714"));
        accuracy.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button backToMenu = new Button("Menu");
        backToMenu.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color:#e2b714");
        backToMenu.setOnAction(event -> showMenu());

        Button restartButton = new Button("Restart");
        restartButton.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color:#e2b714");
        restartButton.setOnAction(event -> showGame());

        GridPane successLayout = new GridPane();
        successLayout.setAlignment(Pos.BOTTOM_CENTER);
        successLayout.setHgap(70);

        VBox statsLayout = new VBox(20);
        statsLayout.getChildren().addAll(winTitle, bestWPM, wpmResult, rawWpmResult, accuracy, successLayout);
        statsLayout.setStyle("-fx-background-color: " + getBackgroundColour() + ";");
        statsLayout.setAlignment(Pos.CENTER);

        successLayout.add(backToMenu, 0, 0);
        successLayout.add(restartButton, 1, 0);

        Scene winScreen = new Scene(statsLayout, 900, 500);
        stage.setScene(winScreen);
        stage.show();
        statsLayout.requestFocus();

    }

    // Constructs and displays the main game typing scene.
    private void showGame(){
        startNanoTime = 0;
        errors = 0;
        isTimerRunning = false;
        secondsElapsed = 0;
        wpm = 0;
        currentIndex = 0;
        VBox gameLayout = new VBox(20);
        gameLayout.setStyle("-fx-background-color: " + getBackgroundColour() + ";");
        gameLayout.setAlignment(Pos.CENTER);

        Button backToMenu = new Button("Menu");
        backToMenu.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color:#e2b714");
        backToMenu.setOnAction(event -> showMenu());

        typingWords = new TextFlow();
        typingWords.setMaxWidth(600);
        typingWords.setTextAlignment(TextAlignment.CENTER);
        String word = wordManager.sentenceRandomiser(textWordCount);
        for (char c : word.toCharArray()){
            Text letter = new Text(String.valueOf(c));
            letter.setFill(Color.web(getPrimaryColour()));
            letter.setStyle("-fx-font-size: 32px; -fx-font-family: 'monospace';");
            typingWords.getChildren().add(letter);
        }

        gameLayout.getChildren().addAll(typingWords, backToMenu);
        updateLetterColour(0, "", true);
        Scene gameScene = new Scene(gameLayout, 900, 500);

        gameScene.setOnKeyTyped(event -> {
            if (!isTimerRunning) {
                wpmTimer.start();
                isTimerRunning = true;
            }

            if (event.getCharacter().equals("\b")){
                return;
            }

            String typedChar = event.getCharacter();
            char expectedChar = word.charAt(currentIndex);

            if (typedChar.equals(String.valueOf(expectedChar))){
                updateLetterColour(currentIndex, getTypedColour(), false);
                currentIndex++;
            } else{
                updateLetterColour(currentIndex, "#FF0000", false);
                errors++;
                currentIndex++;
            }

            if (currentIndex < typingWords.getChildren().size()){
                updateLetterColour(currentIndex, getPrimaryColour(), true);
            }

            if (currentIndex == typingWords.getChildren().size()){
                wpmTimer.stop();

                winScreen();
            }

        });

        gameScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                handleBackspace();
            }
        });


        stage.setScene(gameScene);
        stage.show();
        gameLayout.requestFocus();
    }

    // Changes letter colour from "typeable" to "typed"
    private void updateLetterColour(int index, String hexColour, boolean underlined){
        if (index >= 0 && index < typingWords.getChildren().size()){
            Text letter = (Text) typingWords.getChildren().get(index);
            letter.setUnderline(underlined);

            // Makes it so first letter isn't coloured until "key press"
            if (hexColour != null && !hexColour.isEmpty()){
                letter.setFill(Color.web(hexColour));
            }
        }
    }

    // Constructs and displays the settings scene.
    private void showSettings(){
        GridPane settingsLayout = new GridPane();
        settingsLayout.setStyle("-fx-background-color: " + getBackgroundColour() + ";");
        settingsLayout.setAlignment(Pos.TOP_LEFT);
        settingsLayout.setHgap(40);
        settingsLayout.setVgap(20);

        Text wordCount = new Text("Word Count:");
        Text fillerSpace = new Text();
        wordCount.setFont(new Font(24));
        wordCount.setFill(Color.web("#e2b714"));
        wordCount.setStyle("-fx-font-weight: bold;");


        ChoiceBox<Integer> wordCountBox = new ChoiceBox<>();
        wordCountBox.setPrefSize(100,40);
        wordCountBox.getItems().addAll(5, 10, 25);
        wordCountBox.setValue(textWordCount);

        Text themeSelection = new Text("Select Theme:");
        themeSelection.setFont(new Font(24));
        themeSelection.setFill(Color.web("#e2b714"));
        themeSelection.setStyle("-fx-font-weight: bold;");

        ChoiceBox<String> choiceOfTheme = new ChoiceBox<>();
        choiceOfTheme.setPrefSize(100,40);
        choiceOfTheme.getItems().addAll("Dark", "Light", "Cyberpunk");
        choiceOfTheme.setValue(currentTheme);

        choiceOfTheme.setOnAction(event -> {
            currentTheme = choiceOfTheme.getValue();
            settingsLayout.setStyle("-fx-background-color: " + getBackgroundColour() + ";");
            System.out.println("Theme changed to: " + currentTheme);
        });

        wordCountBox.setOnAction(event ->{
            textWordCount = wordCountBox.getValue();
            System.out.println("New word amount: " + textWordCount);
        });

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

    // Constructs and displays the error scene, providing user with information if difficulty obtaining text file.
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
