package com.example.wsclient;

import android.net.NetworkInfo;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.util.Timer;

public class WebSocketClient extends WebSocketAdapter implements NetworkInfoListener {

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) {
        switch (newState) {
            case OPEN:
                timer = new Timer();
                task = new MainActivity.PingTask();
                timer.schedule(task, 1000, 1000);
                task.setPongFlag(true);
                myWBSock.isConn.set(true);
                break;
            case CLOSING:
                myWBSock.isConn.set(false);
                break;
        }
        Log.i("myws", newState.name());
    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) {
        task.setPongFlag(true);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) {
        myWBSock.rx.set(text);
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) {
        myWBSock.onReconn();
    }

    @Override
    public void niListener(NetworkInfo.State state) {

    }
}
