package com.my.tcpmanagertest.tcpsimplekeepmanagertest;

import com.my.tcpmanager.observer.TcpStateObserver;
import com.my.tcpmanager.tcpsimplemanager.TcpServerSimpleManager;

import java.util.Scanner;

public class TcpClientSimpleKeepManagerTestServer {

    private static int port = 9999;

    public static void main(String[] args) {
        TcpServerSimpleManager tcpServerSimpleManager = new TcpServerSimpleManager(port);

        tcpServerSimpleManager.open(new TcpStateObserver() {
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
                tcpServerSimpleManager.close();
                break;
            }
            System.out.println("Server 发送：" + str);
            tcpServerSimpleManager.send(str);
        }
    }
}
