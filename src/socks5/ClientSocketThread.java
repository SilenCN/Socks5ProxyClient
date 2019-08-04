package socks5;


import socks5.encryption.Encryption;
import socks5.encryption.EncryptionInputStream;
import socks5.encryption.EncryptionOutputStream;
import socks5.encryption.methods.XorEncryption;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.Semaphore;


public class ClientSocketThread extends Thread {
    private Socket clientSocket;
    private Semaphore semaphore;
    private Socket targetSocket;
    private Byte protocal = 0;
    private byte switchMethod=0x00;
    private Encryption encryption;
    public ClientSocketThread(Socket clientSocket, Semaphore semaphore,Encryption encryption) {
        this.clientSocket = clientSocket;
        this.semaphore = semaphore;
        this.encryption=encryption;
    }

    @Override
    public void run() {
        try {

            InputStream clientIn = clientSocket.getInputStream();
            OutputStream clientOut = clientSocket.getOutputStream();
            targetSocket=getSSLTargetSocket(Socks5Server.TARGET_SERVER,Socks5Server.TARGET_SERVER_PORT);
            //传输数据转发
            Thread thread1 = new TransferThread(clientIn, new EncryptionOutputStream(targetSocket.getOutputStream(), encryption));
            Thread thread2 = new TransferThread(new EncryptionInputStream(targetSocket.getInputStream(),encryption), clientOut);
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

    public Socket getSSLTargetSocket(String host,int port) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException, CertificateException {
        SSLContext ctx = SSLContext.getInstance("SSL");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore tks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/socks5/kclient.keystore"), "qianxin.com".toCharArray());
        tks.load(new FileInputStream("src/socks5/tclient.keystore"), "qianxin.com".toCharArray());
        kmf.init(ks, "qianxin.com".toCharArray());
        tmf.init(tks);
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        Socket sslSocket = (SSLSocket) ctx.getSocketFactory().createSocket(host, port);
        return sslSocket;
    }

    public void closeClient(InputStream clientIn, OutputStream clientOut) throws IOException {
        clientIn.close();
        clientOut.flush();
        clientOut.close();
        clientSocket.close();
        semaphore.release();
    }

}
