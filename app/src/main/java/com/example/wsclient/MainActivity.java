package com.example.wsclient;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;

import com.example.wsclient.WebSocketWorker.ChangeListener;
import com.example.wsclient.WebSocketWorker.WebSocketClient;
import com.example.wsclient.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements ChangeListener {

    private MyWBSock myWBSock;
    private WebSocketClient client;

    @Override
    public void OnChangeListener(boolean isConnect) {
        myWBSock.isConn.set(isConnect);
    }

    @Override
    public void OnDataReadListener(String data) {
        myWBSock.rx.set(data);
    }

    public class MyWBSock {
        public ObservableField<String> rx = new ObservableField<>();
        public String tx = "";
        public ObservableBoolean isConn = new ObservableBoolean();
        public void onSend(String txt) {
            client.WriteData(txt);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myWBSock = new MyWBSock();
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMyobj(myWBSock);
        client = new WebSocketClient(MainActivity.this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        client.wsConnect();
    }

    @Override
    protected void onDestroy() {
        client.unreg();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        client.wsDisconnect();
        super.onStop();
    }

}
