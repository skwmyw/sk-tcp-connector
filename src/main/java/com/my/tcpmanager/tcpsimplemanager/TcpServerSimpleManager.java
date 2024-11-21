package com.my.tcpmanager.tcpsimplemanager;

import com.my.tcpmanager.observer.TcpReceiveObserver;
import com.my.tcpmanager.observer.TcpStateObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerSimpleManager {
    private int port;
    private int state;
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public static final int STATE_DISCONNECT = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECT = 3;

    public TcpServerSimpleManager(int port) {
        this.port = port;
    }

    public void open(TcpStateObserver tcpStateObserver, TcpReceiveObserver tcpReceiveObserver) {
        if (state == STATE_CONNECTING || state == STATE_CONNECT) return;
        state = STATE_CONNECTING;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                state = STATE_CONNECT;

                tcpStateObserver.onConnect();

                byte[] dataArr = new byte[100];
                int length;
                while ((length = inputStream.read(dataArr)) != -1) {
                    String res = new String(dataArr, 0, length);
                    tcpReceiveObserver.onReceive(res);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close_();

                tcpStateObserver.onDisconnect();
            }
        }).start();
    }

    public void send(String content) {
        if (state == STATE_DISCONNECT || state == STATE_CONNECTING) return;
        try {
            if (outputStream != null) outputStream.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close_() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        state = STATE_DISCONNECT;
    }

    public void close() {
        if (state == STATE_DISCONNECT || state == STATE_CONNECTING) return;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        state = STATE_DISCONNECT;
    }
}
