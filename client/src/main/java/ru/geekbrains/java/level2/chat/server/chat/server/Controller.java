package ru.geekbrains.java.level2.chat.server.chat.server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    //loginBox
    public VBox loggingBox;
    public HBox loginBox;
    public TextField passwordField;
    public TextField loginField;
    public Label logLabel;


    // regBox
    public VBox regBox;
    public TextField regFirstNameField;
    public TextField regLoginField;
    public TextField regPasswordField;
    public Label regLabel;

    // chatBox
    public VBox chatBox;
    public VBox sendMsgBox;
    public ListView clientsList;
    public TextField msgField;
    public TextArea msgArea;


    public Socket socket;
    public DataOutputStream out;
    public DataInputStream in;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loggingBox.setVisible(true);
        loggingBox.setManaged(true);
        regBox.setVisible(false);
        regBox.setManaged(false);
        chatBox.setVisible(false);
        chatBox.setManaged(false);
        connect();
    }

    private void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server localhost:8189");
        }
        new Thread(new InStreamHandler(this)).start();
    }

    // loginBox
    /**
     * отктывает панель регистрации
     */
    public void reg(ActionEvent actionEvent) {
        loggingBox.setVisible(false);
        loggingBox.setManaged(false);
        regBox.setVisible(true);
        regBox.setManaged(true);
        chatBox.setVisible(false);
        chatBox.setManaged(false);
    }

    /**
     * если поля не пустые - отправляет логин и пароль на сервер
     */
    public void logging(ActionEvent actionEvent) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            logLabel.setText("Поля логин и пароль не должены быть пустыми");
            return;
        }
        String msg = "/login " + loginField.getText() + " " + passwordField.getText();
        sendMsg(msg);
        loginField.clear();
        passwordField.clear();
    }

    //regBox
    /**
     * отправляет регистрационные данные на сервер
     */
    public void registration(ActionEvent actionEvent) {
        if (regFirstNameField.getText().isEmpty()
                || regLoginField.getText().isEmpty()
                || regPasswordField.getText().isEmpty()) {
            regLabel.setText("Поля: Имя, Логин, Пароль должны быть заполнены");
            return;
        }
        String msg = "/reg" +
                " name_" + regFirstNameField.getText() +
                " login_" + regLoginField.getText() +
                " pass_" + regPasswordField.getText();
        sendMsg(msg);
    }

    /**
     * отмена регистрации
     */
    public void exitReg(ActionEvent actionEvent) {
        loggingBox.setVisible(true);
        loggingBox.setManaged(true);
        regBox.setVisible(false);
        regBox.setManaged(false);
        chatBox.setVisible(false);
        chatBox.setManaged(false);
        regFirstNameField.clear();
        regLoginField.clear();
        regPasswordField.clear();
    }


    //chatBox

    /**
     * отправка сообщения на сервер
     */
    public void send(ActionEvent actionEvent) {
        sendMsg(msgField.getText());
        msgField.clear();
    }

    public void logout(ActionEvent actionEvent) {
        sendMsg("/logout");
        loggingBox.setVisible(true);
        loggingBox.setManaged(true);
        regBox.setVisible(false);
        regBox.setManaged(false);
        chatBox.setVisible(false);
        chatBox.setManaged(false);
        msgField.clear();
    }


    /**
     * общий метод для отправки сообщения на сервер
     * и обработки исключения
     */
    private void sendMsg(String msg) {
        try {
            out.writeUTF(msg);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }


    /**
     * called from ClientApp when closing program window
     */
    public void onStageClose() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
