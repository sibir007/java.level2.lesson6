package ru.geekbrains.java.level2.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String login = "";

    public ClientHandler (Server server, Socket socket) {
        this.socket = socket;
        this.server = server;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            try {
                this.socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }

    public DataOutputStream getOut() {
        return out;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        String msg = null;
        try {
            while (true) {
                msg = in.readUTF();
                if (msg.startsWith("/")) {
                    serviceMsgHandler(msg);
                } else {
                    normalMsgHandler(msg);
                }
//                    out.writeUTF("from server: " + msg);
            }
        } catch (IOException e) {
            closeConnection(e);
        }
    }



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
            case "/stat":
                statMsgHandler(msg);

        }

    }

    private void normalMsgHandler (String msg) {
        server.sendBroadcastMsg(getLogin() + " -> " + msg);
    }

    private void loginMsgHandler(String msg) {
        String login = msg.split("_")[1];
        try {
            if (server.checkLogin(this, login)) {
                out.writeUTF("/login_true_" + login);
            } else {
                out.writeUTF("/login_false_" + login);
            }
        }catch (IOException e) {
            closeConnection(e);
        }
    }

    private void statMsgHandler(String msg) {

    }

    /**
     * Закрывает сокет
     * Удаляет clientHandler из списка рассылки
     * Печатает StackTrace
     * @param e
     */
    public void closeConnection (IOException e) {
        e.printStackTrace();
        try {
            socket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        System.out.println("соединение с клиентом " + socket.getRemoteSocketAddress() + " разорвано, socket закрыт");
        server.removeClientHandlerFromList(this);
    }
}
