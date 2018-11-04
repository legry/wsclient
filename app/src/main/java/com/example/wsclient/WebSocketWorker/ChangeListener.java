package com.example.wsclient.WebSocketWorker;

public interface ChangeListener {
    void OnChangeListener(boolean isConnect);
    void OnDataReadListener(String data);
}
