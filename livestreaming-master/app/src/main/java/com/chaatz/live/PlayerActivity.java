package com.chaatz.live;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.chaatz.live.app.MyHandler;
import com.chaatz.live.debug.Log;
import com.chaatz.live.app.widget.FastVideoView;

/**
 * Created by Ravic on 17/12/2015.
 */

public class PlayerActivity extends AppCompatActivity implements MyHandler.Callback {

    private final String TAG = "PlayerActivity";
    private final int EVENT_RECOVER_PLAY_BACK = 1000;
    //    private final float PLAYER_LANDSCAPE_MODE = 180.0f;
    private final float PLAYER_PORTRAIT_MODE = 90.0f;
    private MyHandler myHandler = new MyHandler( this );
    private static ProgressDialog progressDialog;
    FastVideoView fastVideoView;
    private NetWorkStateReceiver mNetWorkReciver;
    private String hostUrl = "";
    private StreamingErrorListener errorListener = new StreamingErrorListener();

    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        this.setContentView( R.layout.player_main );

        fastVideoView = ( FastVideoView ) findViewById( R.id.fast_video_view_layout );
        progressDialog = ProgressDialog.show( PlayerActivity.this, "Please wait", "The live stream is preparing.", true );
        progressDialog.setCancelable( false );

        //start video player
        hostUrl = getIntent().getStringExtra( "host_url" );
        Log.i( TAG, hostUrl );
        videoBuilder( hostUrl );
    }

    protected void videoBuilder( final String hostUrl ) {
        try {
            getWindow().setFormat( PixelFormat.TRANSLUCENT );
            // attach a media controller here.
            fastVideoView.setVideoURI( Uri.parse( hostUrl ) );
            fastVideoView.requestFocus();
            fastVideoView.setOnErrorListener( new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError( MediaPlayer mp, int what, int extra ) {
                    Log.e( "Exception", "error code :" + extra );
                    return false;
                }
            } );
            fastVideoView.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared( MediaPlayer mp ) {
                    if ( null != progressDialog ) {
                        progressDialog.dismiss();
                    }
                    fastVideoView.start();
                }
            } );
        }
        catch ( Exception e ) {
            if ( null != progressDialog ) {
                progressDialog.dismiss();
            }
            Log.e( TAG, "Video builder error : " + e.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( null == mNetWorkReciver ) {
            mNetWorkReciver = new NetWorkStateReceiver();
            registerReceiver( mNetWorkReciver, new IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION ) );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fastVideoView.stopPlayback();
        unregisterReceiver( mNetWorkReciver );
        mNetWorkReciver = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private class StreamingErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError( MediaPlayer mp, int what, int extra ) {
            Log.i( TAG, "Streaming Error : " + what );
            return false;
        }
    }

    private class NetWorkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive( Context context, Intent intent ) {
            String action = intent.getAction();
            Log.i( TAG, "MyBroadcastReceiver.onReceive( ", action, " )" );
            if ( action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) ) {
                if ( isNetworkAvailable() ) {
                    myHandler.sendEmptyMessageDelayed( EVENT_RECOVER_PLAY_BACK, 500 );
                } else {
                    if ( null != fastVideoView ) {
                        fastVideoView.suspend();
                    }
                }
            }
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = ( ConnectivityManager ) getSystemService( Context.CONNECTIVITY_SERVICE );
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    @Override
    public void handleMessage( Message msg ) {
        switch ( msg.what ) {
            case EVENT_RECOVER_PLAY_BACK: {
                if ( null != fastVideoView && !fastVideoView.isPlaying() ) {
                    fastVideoView.resume();
                    Log.d( TAG, "Resuming live stream." );
                }
                break;
            }
            default:
                break;
        }
    }
}
