package pis.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pis.server.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class client gui
 *
 * @version 1.0.0
 */
public class LaunchClient extends Application implements ISend {
    private Stage primaryStage;
    private Scene firstScene;
    private VBox firstLayout;
    private TextArea firstTextArea;
    private Button clearButton;
    private Button exitButton;
    private Button stopButton;
    private Button sendButton;
    private Button setNameButton;
    private TextField setNameField;
    private TextField messageField;
    private TextField clientList;
    private boolean clientStarted;
    private Client client;
    private boolean isLoggedIn;
    private Pattern pattern;
    private Matcher matcher;
    private Label nameListLabel;

    //just initialization of gui
    private void initFirstScene(){
        this.isLoggedIn = false;
        this.clientStarted = false;

        this.setNameButton = new Button("Set Name");
        this.setNameButton.setOnAction(e ->{
            String msg = setNameField.getText();

            if(msg != null && !msg.equals("")) {
                if (!isLoggedIn && clientStarted) {
                    client.sendString(Constants.CONNECT + ": " + msg);
                }else if(!clientStarted){
                    createClient();
                    client.sendString(Constants.CONNECT + ": " + msg);
                }
            }else{
                showTextOnGui("please, input your name!");
            }
        });

        this.sendButton = new Button("Send");
        this.sendButton.setPrefWidth(80);
        this.sendButton.setDisable(true);
        this.sendButton.setOnAction(e -> {
            String msg = messageField.getText();
            messageField.clear();
            if(msg != null && !msg.equals("") && clientStarted) {
                client.sendString(Constants.MESSAGE + ": " + msg);
                showTextOnGui(Constants.MESSAGE + ": " + msg);
            }
        });

        this.clearButton = new Button("Clear");
        this.clearButton.setPrefWidth(80);
        this.clearButton.setOnAction(e ->{
            firstTextArea.clear();
        });

        this.stopButton = new Button("Stop");
        this.stopButton.setPrefWidth(80);
        this.stopButton.setOnAction(e -> {
            if(clientStarted) {
                showTextOnGui(Constants.DISCONNECT + ":");
                setNameField.clear();
                clientList.clear();
            }else{
                showTextOnGui("you are already disconnected!");
            }
        });

        this.exitButton = new Button("Exit");
        this.exitButton.setPrefWidth(80);
        this.exitButton.setOnAction(e -> {
            if(client != null && !client.equals(null)) {
                client.sendString("disconnect:");
                client.disconnect();
            }
            Platform.exit();
        });

        this.setNameField = new TextField("input your name");
        this.setNameField.setEditable(true);
        this.setNameField.setPrefColumnCount(26);

        this.clientList = new TextField("input your name");
        this.clientList.setEditable(false);
        this.clientList.setText("0 clients");
        this.clientList.setPrefColumnCount(26);

        this.messageField = new TextField("input your message");
        this.messageField.setEditable(false);
        this.messageField.setDisable(true);
        this.messageField.setPrefColumnCount(26);

        this.nameListLabel = new Label("are online.");

        this.firstLayout = new VBox();
        this.firstLayout.setAlignment(Pos.CENTER);
        this.firstLayout.setPadding(new Insets(10,25,25,25));
        this.firstScene = new Scene(firstLayout, 500, 400, Color.BLACK);

        this.firstTextArea = new TextArea();
        this.firstTextArea.setEditable(false);
        this.firstTextArea.setWrapText(true);
        this.firstTextArea.setScrollTop(10);
        this.firstTextArea.setPrefHeight(230);

        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10,0,25,0));
        hBox1.getChildren().add(firstTextArea);
        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(setNameField, setNameButton);
        hBox2.setPadding(new Insets(10,0,10, 0));
        hBox2.setSpacing(29);
        HBox hBox3 = new HBox();
        hBox3.getChildren().addAll(messageField, sendButton);
        hBox3.setPadding(new Insets(10,0,25, 0));
        hBox3.setSpacing(29);
        HBox hBox4 = new HBox();
        hBox4.getChildren().addAll(stopButton, clearButton, exitButton);
        hBox4.setSpacing(50);
        HBox hBox5 = new HBox();
        hBox5.getChildren().addAll(clientList, nameListLabel);
        hBox5.setSpacing(30);
        hBox5.setPadding(new Insets(10,0,25, 0));
        this.firstLayout.getChildren().addAll(hBox1, hBox5, hBox2, hBox3, hBox4);
    }


    @Override
    public void init() throws Exception {
        this.isLoggedIn = false;
        initFirstScene();
        super.init();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        primaryStage.setTitle("Client");

        this.primaryStage.setScene(firstScene);
        this.primaryStage.show();
    }

    @Override
    public void showTextOnGui(String text){
        if(isConnect(text)){
            this.isLoggedIn = true;
            changeButtonsState(true);
            sendButton.setDisable(false);
        }

        if(isDisconnect(text) && clientStarted){
            this.isLoggedIn = false;
            this.clientStarted = false;

            client.sendString(Constants.DISCONNECT + ":");
            changeButtonsState(false);
        }

        if(isNamelist(text)){
            this.clientList.setText("");
            String msg = text.replace(":", " ");
            msg = msg.replace("name list", "");
            this.clientList.setText(msg);
        }

        if(isManyUsers(text)){
            this.clientStarted = false;
            this.client = null;
        }
        if(!isNamelist(text)) {
            firstTextArea.appendText(text + "\n");
        }
    }

    //method "be never DRY"
    private void changeButtonsState(boolean state){
        setNameButton.setDisable(state);
        setNameField.setDisable(state);
        setNameField.setEditable(!state);

        messageField.setEditable(state);
        messageField.setDisable(!state);
        sendButton.setDisable(!state);
    }

    //creating new client
    private void createClient(){
        try {
            try {
                this.client = new Client(LaunchClient.this);
                this.clientStarted = true;
            }catch (Exception e){
                client.disconnect();
            }
        } catch (Exception e) {
            showTextOnGui("cannot connect to server!\n");
            this.clientStarted = false;
        }
    }

    //=================REGEX for commands==================
    private boolean isConnect(String msg){
        pattern = Pattern.compile("^connect: ok$");
        matcher = pattern.matcher(msg);
        return matcher.matches();
    }
    private boolean isDisconnect(String msg){
        pattern = Pattern.compile("^disconnect:$");
        matcher = pattern.matcher(msg);
        return matcher.matches();
    }
    private boolean isNamelist(String msg){
        pattern = Pattern.compile("^name list: .+");
        matcher = pattern.matcher(msg);
        return matcher.matches();
    }

    private boolean isManyUsers(String msg){
        pattern = Pattern.compile("^refused: too_many_users$");
        matcher = pattern.matcher(msg);
        return matcher.matches();
    }
    //=================REGEX for commands==================
}
