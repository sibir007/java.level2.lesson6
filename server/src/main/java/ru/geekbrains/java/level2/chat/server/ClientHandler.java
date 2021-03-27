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
    private String infoMsg = "/who_am_i - ваш логин в чате\n" +
            "/w_login message - отправка сообщение только пользователю login\n" +
            "/exit - разрыв соединения с сервером";
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
        try {
            //цикл регистрации и авторизации
            while (true) {
                String regLogMsg = in.readUTF();
                if (regLogMsg.startsWith("/reg")){
                    regMsgHandler(regLogMsg);
                    continue;
                }
                if (regLogMsg.startsWith("/login")) {
                    if (loggingMsgHandler(regLogMsg)) {
                        //цикл общения
                        while (true) {
                            String msg = in.readUTF();
                            if (msg.startsWith("/logout")) {
                                logoutHandler();
                                break;
                            }
                            msgDispatcher(msg);
                        }
                    }
                };
            }


        } catch (IOException e) {
            closeConnection(e);
        }
    }


    /**
     * обработка регистрации "/reg name login password"
     */
    private boolean regMsgHandler(String msg){
        String[] regData = msg.split(" ");
        String name = regData[1].split("_")[1];
        String login = regData[2].split("_")[1];
        String password = regData[3].split("_")[1];
        boolean trueFalse = server.registration(name, login, password);
        if (trueFalse) {
            writeOut("/reg true");
            return true;
        } else {
            writeOut("/reg false");
            return false;
        }
    }

    /**
     * Обработка авторизации "/login login password"
     */
    private boolean loggingMsgHandler(String msg) {
        String login = msg.split(" ")[1];
        String password = msg.split(" ")[2];
        if (server.logging(this, login, password)) {
            writeOut("/login true " + login);
            String loginList = server.getLoginList();
            server.sendBroadcastMsg("/login_list " + loginList);
            return true;
        } else {
            writeOut("/login false " + login);
            return false;
        }
    }

    /**
     * Диспетчер обработки сообщений в цикле общения
     */
    private void msgDispatcher(String msg) {
        if (!msg.startsWith("/")) {
            normalMsgHandler(msg);
            return;
        }
        if (msg.startsWith("/info")) {
            writeOut("server -> \n" + infoMsg);
            return;
        }
        if (msg.startsWith("/who_am_i")) {
            writeOut("server -> your login: " + getLogin());
            return;
        }
        if (msg.startsWith("/w_")) {
            wMsgHandler(msg);
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

    }

    /**
     * Обработчик не служебных сообщений
     */
    private void normalMsgHandler(String msg) {
        countMsg++;
        server.sendBroadcastMsg(getLogin() + " -> " + msg);
    }

    /**
     * Обработка личного сообщения типа "/w_user message"
     */
    private void wMsgHandler(String msg) {
        String targetUserLogin = msg.split(" ")[0].split("_")[1];
        if (!server.checkLoginInChat(targetUserLogin)) {
            writeOut("server -> пользователь с логином " + targetUserLogin + " не зарегистрирован");
            return;
        }
        String message = msg.substring(msg.indexOf(" ") + 1);
        server.sendMsgOneUser(targetUserLogin,getLogin() + " -> " + message);
    }

    /**
     *обработчик выхода пользователя из учётной записи "/logout"
     */
    private void logoutHandler() {
        this.login = "";
        String loginList = server.getLoginList();
        server.sendBroadcastMsg("/login_list " + loginList);
    }

    /**
     * Пишет сообщение в out
     * синхронизироват т.к. может быть вызван из нескольких параллельных потоков
     */
    public synchronized void writeOut(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            closeConnection(e);
        }
    }

    /**
     * Закрывает сокет
     * Удаляет clientHandler из списка рассылки
     * Печатает StackTrace
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


}
