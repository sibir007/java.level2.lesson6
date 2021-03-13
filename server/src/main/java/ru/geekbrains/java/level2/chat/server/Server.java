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
     * Добавляет clientHandler в списое при подключении
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
    public synchronized boolean checkLogin (ClientHandler clientHandler, String login) {
        for (ClientHandler client: clientList) {
            if (client.getLogin().equals(login)) {
                return false;
            }
        }
        clientHandler.setLogin(login);
        System.out.println("Клиент " + clientHandler.getSocket().getRemoteSocketAddress() + " залогинился, логин: " + login);
        return true;
    }

    public synchronized void sendBroadcastMsg(String msg) {
        for (ClientHandler client: clientList) {
            if (!client.getLogin().equals("")) {
                try {
                    client.getOut().writeUTF(msg);
                } catch (IOException e) {
                    client.closeConnection(e);
                }
            }
        }
    }
}
