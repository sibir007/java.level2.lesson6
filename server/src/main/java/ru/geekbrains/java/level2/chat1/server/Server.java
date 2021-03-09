package ru.geekbrains.java.level2.chat1.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private static ServerSocket serverSocket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static Thread outThread;
    private static Scanner scanner = new Scanner(System.in);
    private static Socket socket;
    private static int countMsg;
    private static String inMsg;
    private static String outMsg;




    public static void main(String[] args) {
        try {
            initConnection();
            runOutStreamThread();
            runInStream();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * создаёт соединение
     */
    private static void initConnection () throws IOException {
        serverSocket = new ServerSocket(8190);
        System.out.println("Сервер запущен на порту 8190. Ожидаем подключение клиента ...");
        socket = serverSocket.accept();
        System.out.println("Клиент подключился");
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * обрабатывает исходящие сообщения клиенту
     */
    private static void runOutStreamThread() {
        new Thread(() -> {
            while (true) {
                outMsg = scanner.nextLine();
                try {
                    getOut().writeUTF(outMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * обрабатывает входящие сообщения от клиента,
     * при запросе /stat - пишет в исходящий поток кол-во
     * сообщений получанных от клиента
     *
     */
    private static void runInStream() throws IOException {
        while (true) {
            inMsg = in.readUTF();
            countMsg++;
            if (inMsg.endsWith("/stat")) {
                System.out.println("client -> " + inMsg);
                String outMsg = "колличество сообщение от client: " + countMsg;
                System.out.println(outMsg);
                getOut().writeUTF(outMsg);
            } else {
                System.out.println("client -> " + inMsg);
            }
        }
    }

    /**
     * к out возможно одновременное обращение из двух потоков
     * поэтому синхронизируем доступ
     */
    private static synchronized DataOutputStream getOut () {
        return out;
    }
}
