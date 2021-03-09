package ru.geekbrains.java.level2.chat.server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class Controller  implements Initializable{
    @FXML
    public TextField msgField;
    public TextArea msgArea;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public void send(ActionEvent actionEvent){
        try {
            out.writeUTF(msgField.getText() + "\n");
            msgField.clear();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server localhost:8189");
        }
        new Thread(() -> {
            while (true) {
                try {
                    msgArea.appendText(in.readUTF());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
