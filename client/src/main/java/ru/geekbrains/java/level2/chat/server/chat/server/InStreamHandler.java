package ru.geekbrains.java.level2.chat.server.chat.server;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

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
    public HBox sendMsgBox;

    public InStreamHandler (TextArea msgArea, DataInputStream in, Socket socket, HBox loginBox, HBox sendMsgBox, TextField loginField) {
        this.msgArea = msgArea;
        this.in = in;
        this.socket = socket;
        this.loginBox = loginBox;
        this.sendMsgBox = sendMsgBox;
        this.loginField = loginField;
    }


    @Override
    public void run() {
        try {
            while (true) {
                String msg = in.readUTF();
                msgArea.appendText(msg + "\n");
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

    }

    private void loginMsgHandler(String msg) {
        String trueFalse = msg.split("_")[1];
        if (trueFalse.equals("true")) {
            sendMsgBox.setVisible(true);
            sendMsgBox.setManaged(true);
            loginBox.setVisible(false);
            loginBox.setManaged(false);
        } else {
            loginField.setPromptText("Логин " + msg.split("_")[2] + " занят, выберете другой логин");
        }
    }
}
