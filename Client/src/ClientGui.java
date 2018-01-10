import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientGui extends Application implements ISendable {
    private Stage primaryStage;
    private Scene secondScene;
    private Scene firstScene;
    private VBox secondLayout;
    private VBox firstLayout;
    private TextArea textArea;
    private TextArea firtsTextArea;
    private Button connectButton;
    private Button stopButton;
    private Button sendButton;
    private TextField sendField;
    private TextField connectField;
    private boolean clientStarted;
    private Client client;
    private boolean isLoggedIn;
    private Pattern pattern;
    private Matcher matcher;


    private void initFirstScene(){
        this.connectButton = new Button("Connect");
        this.connectButton.setPrefWidth(130);

        this.connectButton.setOnAction(e -> {
            if(!clientStarted) {
                this.clientStarted = true;
                try {
                    this.client = new Client(ClientGui.this);
                } catch (Exception e1) {
                    sendTextToGui("cannot establish connection");
                    this.clientStarted = false;
                }
                String msg = connectField.getText();
                if (!isLoggedIn && !msg.equals("")) {
                    client.sendString(Constants.CONNECT + ": " + msg);
                }
            }
        });

        this.sendField = new TextField();
        this.sendField.setEditable(true);
        this.sendField.setPrefColumnCount(100);

        this.connectField = new TextField("Input your name");

        this.firstLayout = new VBox();
        this.firstLayout.setAlignment(Pos.CENTER);
        this.firstLayout.setPadding(new Insets(10,25,25,25));
        this.firstScene = new Scene(firstLayout, 500, 300, Color.BLACK);

        this.firtsTextArea = new TextArea();
        this.firtsTextArea.setEditable(false);
        this.firtsTextArea.setWrapText(true);
        this.firtsTextArea.setScrollTop(10);
        this.firtsTextArea.setPrefHeight(230);

        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10,0,25,0));
        hBox1.getChildren().add(firtsTextArea);
        HBox hBox2 = new HBox();
        hBox2.getChildren().add(connectField);
        hBox2.setPadding(new Insets(10,0,25,0));
        HBox hBox3 = new HBox();
        hBox3.getChildren().addAll(connectButton);
        hBox3.setSpacing(70);
        this.firstLayout.getChildren().addAll(hBox1, hBox2, hBox3);
    }
    private void initSecondScene(){
        this.sendButton = new Button("Send");
        this.sendButton.setOnAction(e -> {
            String msg = sendField.getText();
            sendField.clear();
            if(msg != null && clientStarted && msg != "" && !msg.equals("")){
                client.sendString(msg);
            }
        });
        this.sendButton.setPrefWidth(130);

        this.stopButton = new Button("Stop");
        this.stopButton.setOnAction(e -> {
            if(clientStarted){
                client.sendString(Constants.DISCONNECT + ":");
                this.clientStarted = false;
                this.isLoggedIn = false;
                this.firtsTextArea.appendText(textArea.getText());
                this.primaryStage.setScene(firstScene);
            }else{
                sendTextToGui("you must establish connection!");
            }
        });
        this.stopButton.setPrefWidth(130);

        this.textArea = new TextArea();
        this.textArea.setEditable(false);
        this.textArea.setWrapText(true);
        this.textArea.setScrollTop(10);
        this.textArea.setPrefHeight(230);

        this.sendField = new TextField();
        this.sendField.setEditable(true);
        this.sendField.setPrefColumnCount(100);

        this.secondLayout = new VBox();
        this.secondLayout.setAlignment(Pos.CENTER);
        this.secondLayout.setPadding(new Insets(10,25,25,25));
        secondScene = new Scene(secondLayout, 500, 300, Color.BLACK);

        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10,0,25,0));
        hBox1.getChildren().add(textArea);
        HBox hBox2 = new HBox();
        hBox2.getChildren().add(sendField);
        hBox2.setPadding(new Insets(10,0,25,0));
        HBox hBox3 = new HBox();
        hBox3.getChildren().addAll(sendButton, stopButton);
        hBox3.setSpacing(70);
        this.secondLayout.getChildren().addAll(hBox1, hBox2, hBox3);
    }

    @Override
    public void init() throws Exception {
        this.isLoggedIn = false;

        initFirstScene();
        initSecondScene();
        //this.firstScene.setOnC

        super.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        primaryStage.setTitle("Client");


        this.primaryStage.setScene(getScene());
        this.primaryStage.show();
    }
    private Scene getScene(){
        if(isLoggedIn){
            return secondScene;
        }else{
            return firstScene;
        }
    }

    @Override
    public void sendTextToGui(String text){
        if(isLoggedIn) {
            textArea.appendText(text + "\n");
        }else {
            if(isConnect(text) && !isLoggedIn) {
                isLoggedIn = true;
                this.textArea.appendText(firtsTextArea.getText());
                //primaryStage.setScene(getScene());
            }else{
                this.isLoggedIn = false;
            }
            firtsTextArea.appendText(text + "\n");
        }
    }

    public boolean isConnect(String msg){
        pattern = Pattern.compile("^connect: ok$");
        matcher = pattern.matcher(msg);
            return matcher.matches();
    }
}
