package com.chaatz.live.app.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by ericshe on 13/1/2016.
 */
public abstract class AbstractBroadcastReceiver extends BroadcastReceiver {

    private String mFilterAction = "";
    protected final String TAG = "";

    public AbstractBroadcastReceiver(String filterAction ){
        this.mFilterAction = filterAction;
    }


    public void registerReceiver( Activity activity ) {
        IntentFilter filter = new IntentFilter( mFilterAction );
        activity.registerReceiver( this, filter );
    }

    public void unregisterReceiver( Activity activity ) {
        activity.unregisterReceiver( this );
    }
}
