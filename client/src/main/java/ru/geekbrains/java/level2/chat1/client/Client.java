package ru.geekbrains.java.level2.chat1.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static Scanner scanner = new Scanner(System.in);
    private static String inMsg;
    private static String outMsg;
    private static int countMsg = 0;


    public static void main(String[] args) {
        initConnection();
        runInStreamThread();
        runOutStream();

    }

    /**
     * создаёт соединение
     */
    private static void initConnection () {
        try {
            socket = new Socket("localhost", 8190);
            System.out.println("Подключились к localhost: 8190");
            System.out.println("Введите сообщение");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * обрабатывает входящие сообщения с сервера,
     * при запросе /stat - пишет в исходящий поток кол-во
     * сообщений получанных от сервера
     *
     */
    private static void runInStreamThread() {
       new Thread(() -> {
            try {
                while (true) {
                    inMsg = in.readUTF();
                    countMsg++;
                    if (inMsg.endsWith("/stat")) {
                        System.out.println("server -> " + inMsg);
                        String outMsg = "колличество сообщение от server: " + countMsg;
                        System.out.println(outMsg);
                        getOut().writeUTF(outMsg);
                    } else {
                        System.out.println("server -> " + inMsg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    /**
     * обрабатывает исходящие сообщения на сервер
     */
    private static void runOutStream () {
        try {
            while (true) {
                outMsg = scanner.nextLine();
                getOut().writeUTF(outMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
