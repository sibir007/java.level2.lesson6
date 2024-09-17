package ru.geekbrains.java.level2.chat.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private final String HOST = "localhost";
    private final int PORT = 8189;
    //loginBox
    public VBox loggingBox;
    public HBox loginBox;
    public TextField passwordField;
    public TextField loginField;
    public Label logMsg;


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
    public Label loginLabel;


    public Socket socket;
    public DataOutputStream out;
    public DataInputStream in;
    public String login;



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
            socket = new Socket(HOST, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            showAlert(String.format("Не возможно установить соединение с сервером %s:%d", HOST,PORT));
//            throw new RuntimeException("Unable to connect to server localhost:8189");
        }
        new Thread(new InStreamHandler(this)).start();
    }

    // loginBox
    /**
     * Обработчик кнопки registration на loginBox
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
     * Обработчик кнопки login на loginBox
     * если поля логин и пароль не заполнены делает сообщение и выходит
     * отправляет логин и пароль на сервер
     */
    public void logging(ActionEvent actionEvent) {

        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            logMsg.setText("Поля логин и пароль не должены быть пустыми");
            return;
        }
        String msg = "/login " + loginField.getText() + " " + passwordField.getText();
        sendMsg(msg);
        loginField.clear();
        passwordField.clear();
    }

    //regBox
    /**
     * Обработчик кнопки registration на regBox
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
     * Обработчик кнопки exit на regBox - отмена регистрации
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
     * Обработчик кнопки send в chatBox-е
     * отправка сообщения на сервер
     */
    public void send(ActionEvent actionEvent) {
        sendMsg(msgField.getText());
        msgField.clear();
    }

    /**
     * Обработчик кнопки logout в chatBox-е     *
     */
    public void logout(ActionEvent actionEvent) {
        sendMsg("/logout");
        loggingBox.setVisible(true);
        loggingBox.setManaged(true);
        regBox.setVisible(false);
        regBox.setManaged(false);
        chatBox.setVisible(false);
        chatBox.setManaged(false);
        msgField.clear();
        msgArea.clear();
    }



    /**
     * общий метод для отправки сообщения на сервер
     * и обработки исключения
     */
    private void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            showAlert("невозможно отправить сообщение");
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

    /**
     * Alert
     */
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.setTitle("Chat");
        alert.setHeaderText(null);
        alert.showAndWait();
    }


}
