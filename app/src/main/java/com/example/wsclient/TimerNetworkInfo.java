package com.example.wsclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.util.Timer;
import java.util.TimerTask;

class TimerNetworkInfo extends BroadcastReceiver {

    private Timer timer;
    private MyTask mytask;
    private Intent intent;
    private NetworkInfoListener infoListener;
    private Context context;

    TimerNetworkInfo(Context context, NetworkInfoListener infoListener) {
        this.context = context;
        this.infoListener = infoListener;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.context.registerReceiver(this, intentFilter);
        mytask = new MyTask(infoListener);
        setMyTimer();
    }

    void unreg() {
        context.unregisterReceiver(this);
    }

    private void setMyTimer() {
        timer = new Timer();
        mytask = new MyTask(infoListener);
        timer.schedule(mytask, 500);
    }

    private NetworkInfo.State getState() {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        return info.getState();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.intent = intent;
        mytask.cancel();
        timer.cancel();
        setMyTimer();
    }

    class MyTask extends TimerTask {

        private NetworkInfoListener infoListener;

        MyTask(NetworkInfoListener infoListener) {
            this.infoListener = infoListener;
        }

        @Override
        public void run() {
            infoListener.niListener(getState());
        }
    }
}
