package com.example.wsclient;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.view.View;

import com.example.wsclient.databinding.ActivityMainBinding;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private MyWBSock myWBSock;
    private WebSocket ws;
    Timer timer = null;
    PingTask task = null;
    MyClient myClient = null;
    private ActivityMainBinding binding;

    public class MyWBSock {
        public ObservableField<String> rx = new ObservableField<>();
        public String tx = "";
        public ObservableBoolean on_off = new ObservableBoolean();
        public ObservableBoolean isConn = new ObservableBoolean();
        public ObservableField<String> info = new ObservableField<>();
        public void onSend(String txt) {
            ws.sendText(txt);
        }
        public void onReconn(View view, boolean isChecked) {
            if (isChecked) wsConnect(); else wsDisconnect();
        }
    }

    private class PingTask extends TimerTask {

        private boolean pongFlag = false;

        void setPongFlag(boolean pongFlag) {
            this.pongFlag = pongFlag;
        }

        boolean isPongFlag() {
            return pongFlag;
        }

        @Override
        public void run() {
                if (isPongFlag()) {
                    ws.sendPing();
                    setPongFlag(false);
                } else {
                    myWBSock.info.set("reconnect");
                    wsDisconnect();
                    if (myWBSock.on_off.get()) {
                        wsConnect();
                    }
                }
        }
    }

    private class MyClient extends WebSocketAdapter {

        private int cn = 0, png = 0;

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            timer = new Timer();
            task = new PingTask();
            timer.schedule(task, 1000, 1000);
            task.setPongFlag(true);
            myWBSock.isConn.set(true);
            myWBSock.info.set("is Connected" + String.valueOf(cn++));
        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) {
            task.setPongFlag(true);
            myWBSock.info.set("pong"+ String.valueOf(png++));
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) {
            myWBSock.rx.set(text);
        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception) {
            myWBSock.info.set(exception.getError().name());
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myWBSock = new MyWBSock();
         binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMyobj(myWBSock);
    }

    private void wsConnect() {
        myWBSock.info.set("try connect");
        if (myClient == null)
        myClient = new MyClient();
        try {
            ws = new WebSocketFactory()
                    .createSocket("ws://192.168.4.1:81/")
                    .addListener(myClient)
                    .connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void wsDisconnect() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            task = null;
        }
        if (ws != null) if (ws.isOpen()) ws.disconnect();
        ws = null;
    }

    @Override
    protected void onStop() {
        if (myWBSock.on_off.get()) {
            binding.onoff.setChecked(false);
        }
        super.onStop();
    }
}
