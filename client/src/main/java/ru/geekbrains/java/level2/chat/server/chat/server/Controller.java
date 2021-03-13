package ru.geekbrains.java.level2.chat.server.chat.server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public HBox loginBox;
    @FXML
    public HBox sendMsgBox;
    @FXML
    private TextField msgField;
    @FXML
    private TextArea msgArea;
    private Socket socket;
    @FXML
    private TextField loginField;
    private DataOutputStream out;
    private DataInputStream in;


    public void send(ActionEvent actionEvent) {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();

        }
    }

    public void login(ActionEvent actionEvent) {
        try {
            out.writeUTF("/login_" + loginField.getText());
            loginField.clear();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void onStageClose() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        new Thread(new InStreamHandler(msgArea, in, socket, loginBox, sendMsgBox, loginField)).start();
    }


}
