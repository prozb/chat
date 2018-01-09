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

public class ClientGui extends Application implements ISendable {
    private Stage primaryStage;
    private Scene scene;
    private VBox layout;
    private TextArea textArea;
    private Button connectButton;
    private Button stopButton;
    private Button sendButton;
    private TextField sendField;
    private boolean clientStarted;
    private Client client;

    @Override
    public void init() throws Exception {
        this.clientStarted = false;
        this.connectButton = new Button("Connect");
        this.sendButton = new Button("Send");
        this.sendButton.setOnAction(e -> {
            String msg = sendField.getText();
            sendField.clear();
            if(msg != null && clientStarted && msg != "" && !msg.equals("")){
                client.sendString(msg);
            }
        });
        this.sendButton.setPrefWidth(130);
        this.connectButton.setPrefWidth(130);

        this.connectButton.setOnAction(e -> {
            if(!clientStarted) {
                try {
                    this.client = new Client(ClientGui.this);
                    this.clientStarted = true;
                } catch (Exception e1) {
                    sendTextToGui("cannot establish connection!");
                    this.clientStarted = false;
                }
            }else {
                sendTextToGui("you are already connected!");
            }
        });
        this.stopButton = new Button("Stop");
        this.stopButton.setOnAction(e -> {
            if(clientStarted){
                client.sendString(Constants.DISCONNECT + ":");
                this.clientStarted = false;
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

        this.layout = new VBox();
        this.layout.setAlignment(Pos.CENTER);
        this.layout.setPadding(new Insets(10,25,25,25));
        scene = new Scene(layout, 500, 300, Color.BLACK);

        HBox hBox1 = new HBox();
        hBox1.setPadding(new Insets(10,0,25,0));
        hBox1.getChildren().add(textArea);
        HBox hBox2 = new HBox();
        hBox2.getChildren().add(sendField);
        hBox2.setPadding(new Insets(10,0,25,0));
        HBox hBox3 = new HBox();
        hBox3.getChildren().addAll(connectButton, sendButton, stopButton);
        hBox3.setSpacing(70);
        this.layout.getChildren().addAll(hBox1, hBox2, hBox3);

        super.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        primaryStage.setTitle("Client");

        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }

    @Override
    public void sendTextToGui(String text){
        textArea.appendText(text + "\n");
    }
}
