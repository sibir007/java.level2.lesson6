package ru.geekbrains.java.level2.chat.server.chat.server;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class InStreamHandler implements Runnable {
    @FXML
    private TextField msgField;
    private TextArea msgArea;
    private Socket socket;
    private TextField loginField;
    private DataOutputStream out;
    private DataInputStream in;
    public HBox loginBox;
    public VBox sendMsgBox;

    public InStreamHandler (TextArea msgArea, DataInputStream in, Socket socket, HBox loginBox, VBox sendMsgBox, TextField loginField, TextField msgField) {

        this.msgArea = msgArea;
        this.in = in;
        this.socket = socket;
        this.loginBox = loginBox;
        this.sendMsgBox = sendMsgBox;
        this.loginField = loginField;
        this.msgField = msgField;

    }


    @Override
    public void run() {
        try {
            while (true) {
                String msg = in.readUTF();

                if (msg.startsWith("/")) {
                    serviceMsgHandler(msg);
                } else {
                    normalMsgHandler(msg);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     *
     */

    /**
     * Обработка служебных входящих сообщений
     * @param msg
     */
    private void serviceMsgHandler(String msg) {
        String prefixMsg = msg.split("_")[0];
        switch (prefixMsg) {
            case "/login":
                loginMsgHandler(msg);
                break;
        }
    }

    /**
     * Обработка обычных входящих сообщений сообщений
     * @param msg
     */
    private void normalMsgHandler (String msg) {

        msgArea.appendText(msg + "\n");

    }

    private void loginMsgHandler(String msg) {
        String trueFalse = msg.split("_")[1];

        String login = msg.split("_")[2];

        if (trueFalse.equals("true")) {
            sendMsgBox.setVisible(true);
            sendMsgBox.setManaged(true);
            loginBox.setVisible(false);
            loginBox.setManaged(false);

            msgField.setPromptText("Вы подключены под логином " + login + ", введите сообщение");
        } else {
            loginField.setPromptText("Логин " + login + " занят, выберете другой логин");

        }
    }
}
