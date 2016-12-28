package com.chaatz.live.app.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.chaatz.live.debug.Log;


/**
 * Created by ericshe on 12/1/2016.
 */
public abstract class IncomingCallReceiver extends AbstractBroadcastReceiver {

    public static final String ACTION_PHONE_STATE_CHANGED = "android.intent.action.PHONE_STATE";
    public static final String TAG = "IncomingCallReceiver";
    private String mLastPhoneState;

    public IncomingCallReceiver(){
        super( ACTION_PHONE_STATE_CHANGED );
    }

    @Override
    public void onReceive( Context context, Intent intent ) {

        Bundle extras = intent.getExtras();
        if ( null != extras ) {
            String state = extras.getString( TelephonyManager.EXTRA_STATE );

            if ( TelephonyManager.EXTRA_STATE_RINGING.equals( state ) ) {
                // This code will execute when the phone has an incoming call
//            String incomingNumber = intent.getStringExtra( TelephonyManager.EXTRA_INCOMING_NUMBER );
                Log.d( TAG, "onReceivePhoneCall" );
                onReceivePhoneCall();

            } else if ( TelephonyManager.EXTRA_STATE_OFFHOOK.equals( state ) ) {
                Log.d( TAG, "onOffHook" );
                onOffHook();
            } else if ( TelephonyManager.EXTRA_STATE_IDLE.equals( state ) ) {
                if ( TelephonyManager.EXTRA_STATE_RINGING.equals( mLastPhoneState ) ||
                        TelephonyManager.EXTRA_STATE_OFFHOOK.equals( mLastPhoneState ) ) {
                    Log.d( TAG, "in coming call end" );
                    onPhoneCallEnd();
                }
            }
            mLastPhoneState = state;
        }
    }

    @Override
    public void registerReceiver( Activity activity ) {
        IntentFilter filter = new IntentFilter( ACTION_PHONE_STATE_CHANGED );
        filter.setPriority( IntentFilter.SYSTEM_HIGH_PRIORITY + 1 );
        activity.registerReceiver( this, filter );
    }

    /**
     * a call back when receive a phone call
     */
    public abstract void onReceivePhoneCall();

    /**
     * a call back when user off hook a phone call
     */
    public abstract void onOffHook();

    /**
     * a call back when user end a phone call
     */
    public abstract void onPhoneCallEnd();

}
