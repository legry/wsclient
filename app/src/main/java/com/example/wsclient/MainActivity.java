package com.example.wsclient;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.wsclient.databinding.ActivityMainBinding;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

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
    private WifiManager mainWifiObj = null;

    private NetworkInfoListener infoListener = new NetworkInfoListener() {
        @Override
        public void niListener(NetworkInfo.State state) {
            switch (state) {
                case CONNECTED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!myWBSock.on_off.get())
                                binding.onoff.setChecked(true);
                            else wsConnect();
                        }
                    });
                    break;
                case DISCONNECTED:
                    myWBSock.isConn.set(false);
                    mainWifiObj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (mainWifiObj != null) {
                        if (!mainWifiObj.isWifiEnabled())
                            mainWifiObj.setWifiEnabled(true);
                    }
                    break;
            }
        }
    };
    private TimerNetworkInfo info;

    public class MyWBSock {
        public ObservableField<String> rx = new ObservableField<>();
        public String tx = "";
        public ObservableBoolean on_off = new ObservableBoolean();
        public ObservableBoolean isConn = new ObservableBoolean();
        public ObservableField<String> info = new ObservableField<>();

        public void onSend(String txt) {
            ws.sendText(txt);
        }

        public void onOnOff(View view, boolean isChecked) {
            if (isChecked) wsConnect();
            else wsDisconnect();
        }

        public void onReconn() {
            mainWifiObj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (mainWifiObj != null) {
                if (mainWifiObj.isWifiEnabled())
                    mainWifiObj.setWifiEnabled(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myWBSock = new MyWBSock();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMyobj(myWBSock);
        info = new TimerNetworkInfo(MainActivity.this, infoListener);
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
                wsDisconnect();
            }
        }
    }

    private class MyClient extends WebSocketAdapter {

        @Override
        public void onStateChanged(WebSocket websocket, WebSocketState newState) {
            switch (newState) {
                case OPEN:
                    timer = new Timer();
                    task = new PingTask();
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
    }

    private void wsConnect() {
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

    @Override
    protected void onDestroy() {
        if (ws != null) ws.clearListeners();
        info.unreg();
        super.onDestroy();
    }

    private void wsDisconnect() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            task = null;
        }
        if (ws != null) if (ws.isOpen()) ws.disconnect();
    }

    @Override
    protected void onStop() {
        if (myWBSock.on_off.get()) {
            binding.onoff.setChecked(false);
        }
        super.onStop();
    }

}
