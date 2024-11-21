package com.my.tcpmanagertest.tcpmanagertest;

import com.my.tcpmanager.observer.TcpStateObserver;
import com.my.tcpmanager.tcpmanager.TcpClientManager;

import java.util.Scanner;

public class TcpClientManagerTest {

    private static String ip = "127.0.0.1";
    private static int port = 9999;

    public static void main(String[] args) {
        TcpClientManager tcpClientManager = new TcpClientManager(ip, port);

        tcpClientManager.open(new TcpStateObserver() {
            @Override
            public void onConnect() {
                System.out.println("Client 建立连接");
            }

            @Override
            public void onDisconnect() {
                System.out.println("Client 断开连接");
            }
        }, result -> {
            System.out.println("Client 接收：" + result);
        });

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String str = scanner.next();
            if (str.equals("close")) {
                tcpClientManager.close();
                break;
            }
            System.out.println("Client 发送：" + str);
            tcpClientManager.send(str);
        }
    }
}