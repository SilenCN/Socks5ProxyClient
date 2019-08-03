package socks5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;


public class ClientSocketThread extends Thread {
    private Socket clientSocket;
    private Semaphore semaphore;
    private Socket targetSocket;
    private Byte protocal = 0;
    private byte switchMethod=0x00;
    public ClientSocketThread(Socket clientSocket, Semaphore semaphore) {
        this.clientSocket = clientSocket;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {

            InputStream clientIn = clientSocket.getInputStream();
            OutputStream clientOut = clientSocket.getOutputStream();
            targetSocket=new Socket(Socks5Server.TARGET_SERVER,Socks5Server.TARGET_SERVER_PORT);
            //传输数据转发
            Thread thread1 = new TransferThread(clientIn, targetSocket.getOutputStream());
            Thread thread2 = new TransferThread(targetSocket.getInputStream(), clientOut);
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
            clientSocket.close();
            targetSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

    public void closeClient(InputStream clientIn, OutputStream clientOut) throws IOException {
        clientIn.close();
        clientOut.flush();
        clientOut.close();
        clientSocket.close();
        semaphore.release();
    }

}
