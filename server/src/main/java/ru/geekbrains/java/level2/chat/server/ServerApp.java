package ru.geekbrains.java.level2.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static String msg;
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            serverSocket = new ServerSocket(8189);
            System.out.println("Сервер запущен на порту 8189. Ожидаем подключения клиентов ...");
            Socket socket = serverSocket.accept();
            System.out.println("Клиент подключился");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            while (true) {
                msg = in.readUTF();
                System.out.print(msg);
                out.writeUTF("from server: " + msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            serverSocket.close();
        }

    }
}
