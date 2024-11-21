package com.my.tcpmanagertest.tcpkeepmanagertest;

import com.my.tcpmanager.observer.TcpStateObserver;
import com.my.tcpmanager.tcpkeepmanager.TcpServerKeepManager;

import java.util.Scanner;

public class TcpServerKeepManagerTestServer {

    private static int port = 9999;

    public static void main(String[] args) {
        TcpServerKeepManager tcpServerKeepManager = new TcpServerKeepManager(port, new TcpStateObserver() {
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
            System.out.println("Server 发送：" + str);
            tcpServerKeepManager.send(str);
        }
    }
}
