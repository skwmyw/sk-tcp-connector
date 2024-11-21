package com.my.tcpmanagertest.tcpkeepmanagertest;

import com.my.tcpmanager.observer.TcpStateObserver;
import com.my.tcpmanager.tcpmanager.TcpServerManager;

import java.util.Scanner;

public class TcpClientKeepManagerTestServer {

    private static int port = 9999;

    public static void main(String[] args) {
        TcpServerManager tcpServerManager = new TcpServerManager(port);

        tcpServerManager.open(new TcpStateObserver() {
            @Override
            public void onConnect() {
                System.out.println("Server 得到连接");
            }

            @Override
            public void onDisconnect() {
                System.out.println("Server 失去连接");
            }
        }, result -> {
            System.out.println("Server 接收：" + result);
        });

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String str = scanner.next();
            if (str.equals("close")) {
                tcpServerManager.close();
                break;
            }
            System.out.println("Server 发送：" + str);
            tcpServerManager.send(str);
        }
    }
}
