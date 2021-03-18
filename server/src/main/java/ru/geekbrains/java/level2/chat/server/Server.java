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
    private LinkedList<ClientHandler> clientList = new LinkedList<>();


    public Server(int port) {
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
     * Добавляет clientHandler в списое при подключении клиента
     * @param clientHandler
     */
    public void addClientHandlerToList (ClientHandler clientHandler) {
        clientList.add(clientHandler);
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
     * Проверяет есть ли уже такой логин, если есть
     * возвращает false, если нет присваивает clientHandler логин
     * и возвращает true
     * вызываетсся из clientHandler
     */
    public synchronized boolean logging (ClientHandler clientHandler, String login) {
        if (checkLogin(login)) {
            return false;
        }
        clientHandler.setLogin(login);
        System.out.println("Клиент " + clientHandler.getSocket().getRemoteSocketAddress() + " залогинился, логин: " + login);
        return true;
    }

    /**
     * Проверяет есть ли такой логин в users
     * @return true - логин есть, false - логина нет
     */
    public boolean checkLogin (String login) {
        for (ClientHandler client: clientList) {
            if (client.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkLoginInChat (String targetUserLogin) {
        for (ClientHandler client: clientList) {
            if (client.getLogin().equals(targetUserLogin)) {
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
        StringBuilder loginListSB = new StringBuilder();
        for (ClientHandler client: clientList) {
            if (!client.getLogin().equals("")) {
                loginListSB.append(client.getLogin() + " ");
            }
        };
        String loginList = new String(loginListSB);
        return loginList;
    }
}
