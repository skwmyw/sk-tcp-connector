package com.my.tcpmanager.observer;

public interface TcpStateObserver {
    void onConnect();
    void onDisconnect();
}
