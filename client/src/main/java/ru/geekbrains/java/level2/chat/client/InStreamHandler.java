package ru.geekbrains.java.level2.chat.client;

import javafx.application.Platform;

import java.io.*;

public class InStreamHandler implements Runnable {
    private Controller controller;


    public InStreamHandler (Controller controller) {
        this.controller = controller;
    }


    @Override
    public void run() {
        try {
            while (true) {
                String msg = controller.in.readUTF();
                if (msg.startsWith("/")) {
                    serviceMsgHandler(msg);
                } else {
                    normalMsgHandler(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                controller.socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Обработка служебных входящих сообщений
     * @param msg
     */
    private void serviceMsgHandler(String msg) {
        String prefixMsg = msg.split(" ")[0];
        switch (prefixMsg) {
            case "/login":
                loginMsgHandler(msg);
                break;
            case "/reg":
//                System.out.println("mes3" + msg);
                regMsgHandler(msg);
                break;
            case  "/login_list":
                System.out.println("/login_list");
                loginListMsgHandler(msg);

        }
    }

    /**
     * Обработка обычных входящих сообщений сообщений
     * @param msg
     */
    private void normalMsgHandler (String msg) {
        writeHistoryFile(msg); // записываем сообщение в historyFile
        Platform.runLater(() -> {
            controller.msgArea.appendText(msg + "\n");
        });

    }

    /**
     * processing logging messages
     */
    private void loginMsgHandler(String msg) {
        String trueFalse = msg.split(" ")[1];
        String login = msg.split(" ")[2];
        if (trueFalse.equals("true")) {
            Platform.runLater(() -> {
                controller.loggingBox.setVisible(false);
                controller.loggingBox.setManaged(false);
                controller.regBox.setVisible(false);
                controller.regBox.setManaged(false);
                controller.chatBox.setVisible(true);
                controller.chatBox.setManaged(true);
                controller.regFirstNameField.clear();
                controller.regLoginField.clear();
                controller.regPasswordField.clear();
                controller.regLabel.setText("");
                controller.loginField.clear();
                controller.passwordField.clear();
                controller.logMsg.setText("");
                controller.loginLabel.setText(login);
                controller.login = login;
            });

            loadHistoryFile(login); //загружаем файл истории в msgArea
        } else {
            Platform.runLater(() ->{
                controller.logMsg.setText("Неверные login или password");
            });
        }
    }

    /**
     * Загружает файл истории в msgArea
     * @param login
     */
    private void loadHistoryFile(String login) {
        File historyFile = new File("history_" + login + ".txt");
        if(historyFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
                String str;
                while ((str = reader.readLine()) != null) {
                    String finalStr = str;
                    Platform.runLater(() -> {
                        controller.msgArea.appendText(finalStr + "\n");
                    });
                }
               }catch (IOException e)  {
                   e.printStackTrace();
               }
        }
    }

    /**
     * записывает сообщение в historyFile
     */
    public void writeHistoryFile(String msg) {
        System.out.println("in writeHistoryFile");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("history_" + controller.login + ".txt", true))){
            writer.write(msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработчик сообщений о регистрации
     */
    private void regMsgHandler(String msg) {
        Platform.runLater(() -> {
            String trueFalse = msg.split(" ")[1];
            if (trueFalse.equals("true")) {
                controller.loggingBox.setVisible(true);
                controller.loggingBox.setManaged(true);
                controller.regBox.setVisible(false);
                controller.regBox.setManaged(false);
                controller.chatBox.setVisible(false);
                controller.chatBox.setManaged(false);
                controller.regFirstNameField.clear();
                controller.regLoginField.clear();
                controller.regPasswordField.clear();
                controller.logMsg.setText("");
            } else {
                controller.regLabel.setText("Пользователь с таким логином уже зарегистрирован");
            }
        });

    }
    private void loginListMsgHandler(String msg){
        String[] tokens = msg.split(" ");
        Platform.runLater(() -> {
//            System.out.println(Thread.currentThread().getName());
            controller.clientsList.getItems().clear();
            for (int i = 1; i < tokens.length; i++) {
                controller.clientsList.getItems().add(tokens[i]);
            }
        });
    }
}
