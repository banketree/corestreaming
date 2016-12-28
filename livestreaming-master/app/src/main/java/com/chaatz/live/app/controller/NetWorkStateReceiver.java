package com.chaatz.live.app.controller;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.chaatz.live.debug.Log;

/**
 * Created by ericshe on 13/1/2016.
 */
public abstract class NetWorkStateReceiver extends AbstractBroadcastReceiver {

    private final String TAG = "NetWorkStateReceiver";

    public NetWorkStateReceiver(){
        super( ConnectivityManager.CONNECTIVITY_ACTION );
    }

    @Override
    public void onReceive( Context context, Intent intent ) {
        String action = intent.getAction();
        Log.d( TAG, "onReceive( ", action, " )" );
        if ( action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) ) {
            NetworkInfo info = getNetworkInfo( context );
            if ( isNetworkAvailable( info ) ) {
                Log.d( TAG, "isNetworkAvailable() = " + isNetworkAvailable( info ) + ", type = "+ getNetworkType( info ) );
                onNetWorkAvailable( getNetworkType( info ) );
            } else {
                Log.d(TAG, "isNetworkAvailable() = " + isNetworkAvailable( info )  );
                onNetWorkUnavailable();
            }
        }
    }

    public NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private boolean isNetworkAvailable( NetworkInfo info ) {
        return info != null && info.isConnected();
    }

    private int getNetworkType( NetworkInfo info ) {
        return info.getType();
    }

    /**
     * a call back for device net work available
     *
     * @param netWorkType {@link ConnectivityManager#TYPE_MOBILE}, {@link
     * ConnectivityManager#TYPE_WIFI}, {@link ConnectivityManager#TYPE_WIMAX}, {@link
     * ConnectivityManager#TYPE_ETHERNET},  {@link ConnectivityManager#TYPE_BLUETOOTH}, or other
     * types defined by {@link ConnectivityManager}
     */
    public abstract void onNetWorkAvailable( int netWorkType );

    /**
     * a call back for detect the device network unavailable.
     */
    public abstract void onNetWorkUnavailable();
}
