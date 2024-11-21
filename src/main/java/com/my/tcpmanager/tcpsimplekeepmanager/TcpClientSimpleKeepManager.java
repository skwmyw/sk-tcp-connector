package com.my.tcpmanager.tcpsimplekeepmanager;

import com.my.tcpmanager.observer.TcpReceiveObserver;
import com.my.tcpmanager.observer.TcpStateObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpClientSimpleKeepManager {
    private String ip;
    private int port;
    private TcpStateObserver tcpStateObserver;
    private TcpReceiveObserver tcpReceiveObserver;
    private ExecutorService pool;
    private int state;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public static final int STATE_DISCONNECT = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECT = 3;

    public TcpClientSimpleKeepManager(String ip, int port, TcpStateObserver tcpStateObserver, TcpReceiveObserver tcpReceiveObserver) {
        this.ip = ip;
        this.port = port;
        this.tcpStateObserver = tcpStateObserver;
        this.tcpReceiveObserver = tcpReceiveObserver;
        pool = Executors.newSingleThreadExecutor();
        open();
    }

    private void open() {
        if (state == STATE_CONNECTING || state == STATE_CONNECT) return;
        state = STATE_CONNECTING;
        pool.execute(() -> {
            try {
                socket = new Socket(ip, port);
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

                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                open();
            }
        });
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
        state = STATE_DISCONNECT;
    }
}
