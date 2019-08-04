package socks5;


import socks5.encryption.Encryption;
import socks5.encryption.methods.XorEncryption;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Socks5Server {
    private static final int DEFAULT_PORT = 1080;
    private static final int MAX_CONNECTION = 100;
    public static final String TARGET_SERVER = "127.0.0.1";
    public static final int TARGET_SERVER_PORT = 1081;

    private static boolean FLAG = true;
    private static Encryption encryption = new XorEncryption((byte) 0x02);

    public static void startServer() throws InterruptedException, IOException {
        FLAG = true;
        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
        Semaphore semaphore = new Semaphore(MAX_CONNECTION);
        while (FLAG) {
            semaphore.acquire();
            Socket client = serverSocket.accept();
            System.out.println("\n\n收到Socket连接！");
            new ClientSocketThread(client, semaphore,encryption).start();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Socks5Server.startServer();
    }
}