package com.my.tcpmanager.tcpmanager;

import com.my.tcpmanager.observer.TcpReceiveObserver;
import com.my.tcpmanager.observer.TcpStateObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TcpClientManager {
    private String ip;
    private int port;
    private int state;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public static final int STATE_DISCONNECT = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECT = 3;

    private StringBuilder stringBuilder;
    private static final int threshold = 1024 * 1024 * 15;
    private static final String prefix = "**#";
    private static final String suffix = "#**";
    private static final int prefixLength = prefix.length();
    private static final int suffixLength = suffix.length();

    public TcpClientManager(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void open(TcpStateObserver tcpStateObserver, TcpReceiveObserver tcpReceiveObserver) {
        if (state == STATE_CONNECTING || state == STATE_CONNECT) return;
        state = STATE_CONNECTING;
        stringBuilder = null;
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                state = STATE_CONNECT;

                tcpStateObserver.onConnect();

                byte[] dataArr = new byte[1024 * 5];
                int length;
                while ((length = inputStream.read(dataArr)) != -1) {
                    String res = new String(dataArr, 0, length);
                    List<String> results = dataHandle(res);
                    for (String result : results) tcpReceiveObserver.onReceive(result);
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
            if (outputStream != null) outputStream.write((prefix + content + suffix).getBytes());
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

    public void close() {
        if (state == STATE_DISCONNECT || state == STATE_CONNECTING) return;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        state = STATE_DISCONNECT;
    }

    private List<String> dataHandle(String inputStr) {
        if (stringBuilder != null && stringBuilder.length() > threshold) stringBuilder.setLength(0);

        ArrayList<String> result = new ArrayList<>();

        if (inputStr.startsWith(prefix)) stringBuilder = null;
        String str;
        if (stringBuilder == null) {
            str = inputStr;
        } else {
            stringBuilder.append(inputStr);
            str = stringBuilder.toString();
            stringBuilder = null;
        }

        int lastIndex = str.length() - 1;
        int i = 0;
        while (i <= lastIndex) {
            int prefixIndex = str.indexOf(prefix, i);
            int suffixIndex = str.indexOf(suffix, i);

            if (prefixIndex != -1 && suffixIndex == -1) {

                // 有包头，无包尾

                if (prefixIndex > i) {
                    if (stringBuilder != null) {
                        String part = str.substring(i, prefixIndex);
                        stringBuilder.append(part);
                        String s = stringBuilder.toString();
                        if (s.endsWith(suffix)) {
                            stringBuilder = null;
                            List<String> strings = dataHandle(s);
                            for (String string : strings) result.add(string);
                        }
                    }
                }
                String part = str.substring(prefixIndex, lastIndex + 1);
                stringBuilder = new StringBuilder();
                stringBuilder.append(part);
                i = lastIndex + 1;
            } else if (prefixIndex == -1 && suffixIndex != -1) {

                // 无包头，有包尾

                if (stringBuilder != null) {
                    String part = str.substring(i, prefixIndex);
                    stringBuilder.append(part);
                    String s = stringBuilder.toString();
                    if (s.endsWith(suffix)) {
                        stringBuilder = null;
                        List<String> strings = dataHandle(s);
                        for (String string : strings) result.add(string);
                    }
                }
                i = suffixIndex + suffixLength;
            } else if (prefixIndex != -1 && suffixIndex != -1) {
                if (prefixIndex < suffixIndex) {
                    String part = str.substring(prefixIndex + prefixLength, suffixIndex);
                    result.add(part);
                    stringBuilder = null;
                    i = suffixIndex + suffixLength;
                } else {
                    if (stringBuilder != null) {
                        String part = str.substring(i, prefixIndex);
                        stringBuilder.append(part);
                        String s = stringBuilder.toString();
                        if (s.endsWith(suffix)) {
                            stringBuilder = null;
                            List<String> strings = dataHandle(s);
                            for (String string : strings) result.add(string);
                        }
                    }
                    i = suffixIndex + suffixLength;
                }
            } else {
                String part = str.substring(i, lastIndex + 1);
                if (stringBuilder == null) stringBuilder = new StringBuilder();
                stringBuilder.append(part);
                i = lastIndex + 1;
            }
        }

        return result;
    }
}
