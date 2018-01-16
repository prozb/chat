package pis.server;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.ServerSocket;

/**
 * Class gui
 *
 * @version 1.0.0
 */
public class LaunchServer extends Application implements IInterconnect {
    private Stage primaryStage;
    private Scene scene;
    private VBox layout;
    private TextArea textArea;
    private Button startButton;
    private Button stopButton;
    private Button clearButton;
    private Server server;
    private boolean serverStarted;
    private ServerSocket serverSocket;

    @Override
    public void init() throws Exception {
        this.serverSocket = new ServerSocket(Constants.DEFAULT_PORT);
        this.serverStarted = false;
        this.startButton = new Button("Start");
        this.clearButton = new Button("Clear");
        this.clearButton.setPrefWidth(130);
        this.startButton.setPrefWidth(130);
        this.clearButton.setOnAction(e -> {
            textArea.clear();
        });
        this.startButton.setOnAction(e -> {
            if(!serverStarted) {
                this.server = null;
                this.server = new Server(LaunchServer.this);
                this.serverStarted = true;
            }else {
                sendTextToGui("Server has been already started!");
            }
        });
        this.stopButton = new Button("Stop");
        this.stopButton.setOnAction(e -> {
            if(!serverStarted){
                sendTextToGui("Server isn't started!");
            }else{
                server.finish();
                serverStarted = false;
            }
        });
        this.stopButton.setPrefWidth(130);

        this.textArea = new TextArea();
        this.textArea.setEditable(false);
        this.textArea.setWrapText(true);
        this.textArea.setScrollTop(10);
        this.textArea.setPrefHeight(230);

        this.layout = new VBox();
        this.layout.setAlignment(Pos.CENTER);
        this.layout.setPadding(new Insets(10,25,25,25));
        scene = new Scene(layout, 500, 300, Color.BLACK);

        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10,0,25,0));
        hBox1.getChildren().add(textArea);
        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(startButton, stopButton, clearButton);
        hBox2.setSpacing(70);
        this.layout.getChildren().addAll(hBox1, hBox2);

        super.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        primaryStage.setTitle("Server");

        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }

    @Override
    public void sendTextToGui(String text){
        textArea.appendText(text + "\n");
    }

    @Override
    public ServerSocket getSocket() {
        return serverSocket;
    }
}
