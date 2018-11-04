package com.example.wsclient.WebSocketWorker;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketClient extends WebSocketAdapter implements NetworkInfoListener {

    private Timer timer = null;
    private PingTask task = null;
    private WebSocket ws;
    private ChangeListener changeListener;
    private TimerNetworkInfo info;
    private Context context;
    private NetworkInfo.State state;

    public WebSocketClient(Context context, ChangeListener changeListener) {
        this.changeListener = changeListener;
        this.context = context;
        info = new  TimerNetworkInfo(context, this);
    }

    public void WriteData(String data) {
        if (ws.isOpen()) ws.sendText(data);
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

    public void unreg() {
        info.unreg();
    }

    public void wsConnect() {
        if (state == NetworkInfo.State.CONNECTED) {
            try {
                ws = new WebSocketFactory()
                        .createSocket("ws://192.168.4.1:81/")
                        .addListener(this)
                        .connectAsynchronously();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void wsDisconnect() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            task = null;
        }
        if (ws != null) if (ws.isOpen()) ws.disconnect();
    }

    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) {
        switch (newState) {
            case OPEN:
                timer = new Timer();
                task = new PingTask();
                timer.schedule(task, 1000, 1000);
                task.setPongFlag(true);
                changeListener.OnChangeListener(true);
                break;
            case CLOSING:
                changeListener.OnChangeListener(false);
                break;
        }
    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) {
        task.setPongFlag(true);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) {
        changeListener.OnDataReadListener(text);
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) {
        wifi_OnOff(false);
    }

    @Override
    public void niListener(NetworkInfo.State state) {
        this.state = state;
        switch (state) {
            case CONNECTED: wsConnect(); break;
            case DISCONNECTED: wifi_OnOff(true); break;
        }
    }

    private void wifi_OnOff(boolean onoff) {
        WifiManager mainWifiObj = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mainWifiObj != null) {
            if (onoff && !mainWifiObj.isWifiEnabled()) mainWifiObj.setWifiEnabled(true);
            else if (!onoff && mainWifiObj.isWifiEnabled()) mainWifiObj.setWifiEnabled(false);
        }
    }
}
