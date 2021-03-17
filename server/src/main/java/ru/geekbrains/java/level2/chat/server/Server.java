package ru.geekbrains.java.level2.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Server {
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static String msg;
    private LinkedList<ClientHandler> clientList;
    private LinkedList<User> users;


    public Server(int port) {
        this.clientList = new LinkedList<>();
        this.users = new LinkedList<>();

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен на порту 8189. Ожидаем подключения клиентов ...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Клиент " + socket.getRemoteSocketAddress() + " подключился");
                ClientHandler clientHandler = new ClientHandler(this, socket);
                clientList.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Удаляет clientHandler из списка при разрыве соединения с клиентом
     * вызываетсся из clientHandler
     * @param clientHandler
     */
    public void removeClientHandlerFromList (ClientHandler clientHandler) {
        clientList.remove(clientHandler);

        System.out.println("clientHandler клиента " + clientHandler.getSocket().getRemoteSocketAddress() + " удалён из списка расслылки");
    }

    /**
     * Проверяет, есть ли
     */
    public synchronized boolean logging (ClientHandler clientHandler, String login, String password) {
        if (checkLogin(login, password)) {
            clientHandler.setLogin(login);
            System.out.println("Клиент " + clientHandler.getSocket().getRemoteSocketAddress() + " залогинился, логин: " + login);
            return true;
        }
        return false;
    }

    /**
     * Проверяет есть ли такой логин в списке clientHandler-ов
     * @param login
     * @return true - логин есть, false - логина нет
     */
    public boolean checkLogin (String login, String password) {
        for (User user: users) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Рассылает сообщение слиентам в списке clientHandler-ов
     * @param msg
     */
    public void sendBroadcastMsg(String msg) {
        for (ClientHandler client: clientList) {
            if (!client.getLogin().equals("")) {
                client.writeOut(msg);
            }
        }
    }

    /**
     * Отправляет сообщение одному клиенту
     * @param login логин клиента
     * @param msg отправляесое сообщение
     */
    public synchronized void sendMsgOneUser(String login, String msg) {
        for (ClientHandler client: clientList) {
            if (client.getLogin().equals(login)) {
                client.writeOut(msg);
            }
        }
    }

    /**
     * возвращает сроку со списком всех логинов
     * @return
     */
    public String getLoginList() {
        String loginList = "";
        for (ClientHandler client: clientList) {
            loginList = loginList + client.getLogin() + " ";
        };
        return loginList;
    }

    /**
     * регистрация user
     */
    public boolean registration (String name, String login, String password) {
        for (User user: users) {
            if (user.getLogin().equals(login)) {
                return false;
            }
        }
        users.add(new User(name, login, password));
        return true;
    }
}
