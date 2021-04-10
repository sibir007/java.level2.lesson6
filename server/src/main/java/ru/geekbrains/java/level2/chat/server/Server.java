package ru.geekbrains.java.level2.chat.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    //Trace < Debug < Info < Warn < Error < Fatal
    private static final Logger logger = LogManager.getLogger(Server.class);
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static String msg;
    private LinkedList<ClientHandler> clientList;
    private JdbcRegistrationProvider jdbcRegistrationProvider;
    private LinkedList<User> users;
    private ExecutorService executorService;


    public Server(int port) {
        this.clientList = new LinkedList<>();
        this.users = new LinkedList<>();
        this.jdbcRegistrationProvider = new JdbcRegistrationProvider();

        //20 случайное число, должно выбираться исходя из доступных ресурсов системы,
        //в данном случае это ограничение одновременных пользователей чата до 20.
        this.executorService = Executors.newFixedThreadPool(20);

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен на порту 8189. Ожидаем подключения клиентов ...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Клиент " + socket.getRemoteSocketAddress() + " подключился");
                ClientHandler clientHandler = new ClientHandler(this, socket);
                clientList.add(clientHandler);
                executorService.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            jdbcRegistrationProvider.disconnect();
            executorService.shutdown();
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
        if (jdbcRegistrationProvider.checkLoginAndPassword(login, password)) {
            clientHandler.setLogin(login);
            System.out.println("Клиент " + clientHandler.getSocket().getRemoteSocketAddress() + " залогинился, логин: " + login);
            return true;
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
    public synchronized void sendBroadcastMsg(String msg) {
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

    /**
     * регистрация user
     */
//    public synchronized boolean registration (ClientHandler clientHandler, String name, String login, String password) {
//        System.out.println("Клиент " + clientHandler.getSocket().getRemoteSocketAddress() +
//                " регистрируется - имя: " + name + ", логин: " + login + ", пароль: " + password);
//        for (User user: users) {
//            if (user.getLogin().equals(login)) {
//                System.out.println("Пользователь с таким логином уже зарегистрирован. Регистрация - false");
//                return false;
//            }
//        }
//        users.add(new User(name, login, password));
//        System.out.println("Пользователь зарегистрирован. Регистрация - true");
//        return true;
//    }
    public synchronized boolean registration (ClientHandler clientHandler, String name, String login, String password){
        return jdbcRegistrationProvider.userRegistration(name, login, password);
    }
}
