package com.example.wsclient.WebSocketWorker;

import android.net.NetworkInfo;

interface NetworkInfoListener {
    void niListener(NetworkInfo.State state);
}
