package ru.geekbrains.java.level2.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String login = "";
    private int countMsg = 0;
    //service msg


    public ClientHandler(Server server, Socket socket) {
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
        String msg;
        try {
            while (true) {
                msg = in.readUTF();
                msgHandler(msg);
            }
        } catch (IOException e) {
            closeConnection(e);
        }
    }

    /**
     * Диспетчер обработки сообщений
     *
     * @param msg
     */
    private void msgHandler(String msg) {
        if (!msg.startsWith("/")) {
            normalMsgHandler(msg);
            return;
        }
        if (msg.startsWith("/info")) {
            String infoMsg = "/stat - колличество отправленных сообщений\n" +
                    "/who_am_i - ваш логин в чате\n" +
                    "/w_login message - отправка сообщение только пользователю login\n" +
                    "/exit - разрыв соединения с сервером";
            writeOut("server -> \n" + infoMsg);
            return;
        }
        if (msg.startsWith("/login")) { // /login login password
            loggingMsgHandler(msg);
            return;
        }
        ;
        if (msg.startsWith("/stat")) {
            writeOut("server -> count your messages: " + countMsg);
            return;
        }
        if (msg.startsWith("/who_am_i")) {
            writeOut("server -> your login: " + getLogin());
            return;
        }
        if (msg.startsWith("/w_")) {
//            wMsgHandler(msg);
            return;
        }
        if (msg.startsWith("/exit")) {
            writeOut("server -> вы отключились");
            try {
                throw new IOException("Клиент прислал запрос на разрыв соединения");
            } catch (IOException e) {
                closeConnection(e);
            }
            return;
        }
        if (msg.startsWith("/reg")) {
            String[] regData = msg.split(" ");
            String name = regData[1].split("_")[1];
            String login = regData[2].split("_")[1];
            String password = regData[3].split("_")[1];
            boolean trueFalse = server.registration(name, login, password);
            if (trueFalse) {
                writeOut("/reg true");
            } else {
                writeOut("/reg false");
            }
        }
    }

    /**
     * Обработчик не служебных сообщений
     */
    private void normalMsgHandler(String msg) {
        countMsg++;
        server.sendBroadcastMsg(getLogin() + " -> " + msg);
    }

    /**
     * Обработка отправки логина на сервер
     */
    private void loggingMsgHandler(String msg) {
        String login = msg.split(" ")[1];
        String password = msg.split(" ")[2];
        if (server.logging(this, login, password)) {
            writeOut("/login true " + login);
            String loginList = server.getLoginList();
            server.sendBroadcastMsg("/login_list " + loginList);
        } else {
            writeOut("/login false " + login);
        }
    }

    /**
     * Обработка личного сообщения типа "/w_user message"
     */
//    private void wMsgHandler(String msg) {
//        String loginTargetUser = msg.split(" ")[0].split("_")[1];
//        if (!server.checkLogin(loginTargetUser)) {
//            writeOut("server -> пользователь с логином " + loginTargetUser + " не зарегистрирован");
//            return;
//        }
//        String message = msg.substring(msg.indexOf(" ") + 1);
//        server.sendMsgOneUser(loginTargetUser,getLogin() + " -> " + message);
//    }

    /**
     * Закрывает сокет
     * Удаляет clientHandler из списка рассылки
     * Печатает StackTrace
     *
     * @param e
     */
    public void closeConnection(IOException e) {
        e.printStackTrace();
        try {
            socket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        System.out.println("соединение с клиентом " + socket.getRemoteSocketAddress() + " разорвано, socket закрыт");
        server.removeClientHandlerFromList(this);
        String loginList = server.getLoginList();
        server.sendBroadcastMsg("/login_list " + loginList);

    }

    /**
     * Пишет сообщение в out
     * синхронизироват т.к. может быть вызван из нескольких параллельных потоков
     *
     * @param msg
     */
    public synchronized void writeOut(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            closeConnection(e);
        }
    }
}
