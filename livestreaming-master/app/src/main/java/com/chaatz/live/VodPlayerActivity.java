package com.chaatz.live;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.MediaController;

import com.chaatz.live.app.MyHandler;
import com.chaatz.live.app.base.BaseAppCompactActivity;
import com.chaatz.live.app.controller.IncomingCallReceiver;
import com.chaatz.live.app.controller.NetWorkStateReceiver;
import com.chaatz.live.debug.Log;
import com.chaatz.live.player.builders.CustomExoPlayer;
import com.chaatz.live.player.builders.HlsRendererBuilder;
import com.chaatz.live.player.interfaces.Listener;
import com.chaatz.live.player.interfaces.RendererBuilder;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.upstream.HttpDataSource;
import com.google.android.exoplayer.util.Util;

/**
 * Created by Ravic on 14/1/2016.
 */
public class VodPlayerActivity extends BaseAppCompactActivity implements
        SurfaceHolder.Callback,
        MyHandler.Callback,
        Listener,
        CustomExoPlayer.InfoListener,
        AudioCapabilitiesReceiver.Listener {

    private static final String TAG = "VodPlayerActivity";

    private final int EVENT_RECOVER_PLAY_BACK = 1000;
    public static final int TYPE_HLS = 2;

    private int contentType = TYPE_HLS;
    private CustomExoPlayer player;
    private boolean playerNeedsPrepare;
    private long playerPosition = 0;
    private String contentUri = "";
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;

    //Media controller
    private MediaController mMediaController;

    private AudioCapabilitiesReceiver mAudioCapabilitiesReceiver;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Log.d( TAG, "onCreate()" );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        setContentView( R.layout.activity_custom_vod_player );
        View root = findViewById( R.id.root );

        //Register touch listener for media controller.
        root.setOnTouchListener( new OnTouchListener() {
            @Override
            public boolean onTouch( View view, MotionEvent motionEvent ) {
                if ( motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                    toggleControlsVisibility();
                } else if ( motionEvent.getAction() == MotionEvent.ACTION_UP ) {
                    view.performClick();
                }
                return true;
            }
        } );
        root.setOnKeyListener( new OnKeyListener() {
            @Override
            public boolean onKey( View view, int keyCode, KeyEvent keyEvent ) {
                if ( keyCode == keyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_MENU ) {
                    return false;
                }
                return mMediaController.dispatchKeyEvent( keyEvent );
            }
        } );

        mMediaController = new KeyCompatibleMediaController( this );
        mMediaController.setAnchorView( root );

        mAudioCapabilitiesReceiver = new AudioCapabilitiesReceiver( this, this );
        mAudioCapabilitiesReceiver.register();

        contentUri = getIntent().getStringExtra( "contentUri" );
        Log.d( TAG, contentUri );
        videoFrame = ( AspectRatioFrameLayout ) findViewById( R.id.video_frame );
        surfaceView = ( SurfaceView ) findViewById( R.id.surface_view );
        surfaceView.getHolder().addCallback( this );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkReceiver.registerReceiver( this );
        mIncomingCallReceiver.registerReceiver( this );
        if ( player == null ) {
            preparePlayer( true );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if ( player != null ) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( player != null ) {
            releasePlayer();
        }
        mAudioCapabilitiesReceiver.unregister();
        mNetworkReceiver.unregisterReceiver( this );
        mIncomingCallReceiver.unregisterReceiver( this );
        mIncomingCallReceiver = null;
        mNetworkReceiver = null;
    }

    /**
     * SurfaceView Callback functions
     */
    @Override
    public void surfaceCreated( SurfaceHolder surfaceHolder ) {
        if ( player != null ) {
            player.setSurface( surfaceHolder.getSurface() );
        }
    }

    @Override
    public void surfaceChanged( SurfaceHolder surfaceHolder, int i, int i1, int i2 ) {

    }

    @Override
    public void surfaceDestroyed( SurfaceHolder surfaceHolder ) {
        if ( player != null ) {
            player.blockingClearSurface();
        }
    }

    /**
     * Setup player
     */
    private RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent( this, "CustomExoPlayer" );
        if ( contentType == TYPE_HLS ) {
            return new HlsRendererBuilder( this, userAgent, contentUri.toString() );
        } else {
            throw new IllegalStateException( "Unsupported type: " + contentType );
        }
    }

    private void preparePlayer( boolean playWhenReady ) {
        if ( player == null ) {
            player = new CustomExoPlayer( getRendererBuilder() );
            player.addListener( this );
            player.setInfoListener( this );
            player.seekTo( playerPosition );
            mMediaController.setMediaPlayer( player.getPlayerControl() );
            mMediaController.setEnabled( true );
            playerNeedsPrepare = true;
        }
        if ( playerNeedsPrepare ) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface( surfaceView.getHolder().getSurface() );
        player.setPlayWhenReady( playWhenReady );
    }

    private void releasePlayer() {
        if ( player != null ) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    @Override
    public void onStateChanged( boolean playWhenReady, int playbackState ) {
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch ( playbackState ) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }

        Log.i( TAG, "onStateChange : playWhenReady=" + playWhenReady + " playbackState=" + playbackState + " text=" + text );
    }

    @Override
    public void onError( Exception e ) {
        if ( e instanceof HttpDataSource.HttpDataSourceException ) {
            Log.d( TAG, "HttpDataSourceException = " + e );
        }
        playerNeedsPrepare = true;
    }

    /**
     * Exoplayer Listener Callback functions
     */
    @Override
    public void onVideoSizeChanged( int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio ) {
        Log.i( TAG, "onVideoSizeChanged: w=" + width + " h=" + height );
//        videoFrame.setAspectRatio( height == 0 ? 1 : ( width * pixelWidthHeightRatio ) / height );
    }

    /**
     * Exoplayer InfoListener Callback functions
     */
    public void onVideoFormatEnabled( Format format, int trigger, long mediaTimeMs ) {
        Log.d( TAG, "videoFormat:" + format.toString() );
    }

    public void onAudioFormatEnabled( Format format, int trigger, long mediaTimeMs ) {
        Log.d( TAG, "audioFormat:" + format.toString() );
    }

    public void onDroppedFrames( int count, long elapsed ) {
        Log.d( TAG, "droppedFrames:" + count + ":elapsed:" + elapsed );
    }

    public void onBandwidthSample( int elapsedMs, long bytes, long bitrateEstimate ) {
        Log.d( TAG, "onBandwidthSample:" + bytes + " bytes, bitrateEstimate:" + bitrateEstimate );
    }

    public void onLoadStarted( int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs ) {
        Log.d( TAG, "onLoadStarted: " + sourceId + ":" + ( format != null ? format.bitrate : null ) + " bps." );
    }

    public void onLoadCompleted( int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs ) {
        Log.d( TAG, "onLoadCompleted: " + sourceId + ":" + ( format != null ? format.bitrate : null ) + " bps." );
    }

    public void onDecoderInitialized( String decoderName, long elapsedRealtimeMs, long initializationDurationMs ) {
        Log.d( TAG, "onDecoderInitialized: " + decoderName + ", elapsedRealTime: " + elapsedRealtimeMs + ", initDurationMs: " + initializationDurationMs );
    }

    public void onAvailableRangeChanged( TimeRange availableRange ) {

    }

    @Override
    public void handleMessage( Message msg ) {
        switch ( msg.what ) {
            case EVENT_RECOVER_PLAY_BACK: {
                if ( player == null ) {
                    releasePlayer();
                    preparePlayer( true );
                } else {
                    Log.w( TAG, "player is not released yet." );
                }
                break;
            }
            default:
                break;
        }
    }

    private void toggleControlsVisibility() {
        if ( mMediaController.isShowing() ) {
            mMediaController.hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mMediaController.show( 0 );
    }

    /**
     * AudioCapabilitiesReceiver.Listener callback
     */

    @Override
    public void onAudioCapabilitiesChanged( AudioCapabilities audioCapabilities ) {
        if ( player == null ) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
        releasePlayer();
        preparePlayer( playWhenReady );
        player.setBackgrounded( backgrounded );
    }

    /**
     * Implementation of MediaController
     */
    private static final class KeyCompatibleMediaController extends MediaController {

        private MediaController.MediaPlayerControl playerControl;

        public KeyCompatibleMediaController( Context context ) {
            super( context );
        }

        @Override
        public void setMediaPlayer( MediaController.MediaPlayerControl playerControl ) {
            super.setMediaPlayer( playerControl );
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent( KeyEvent event ) {
            int keyCode = event.getKeyCode();
            if ( playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD ) {
                if ( event.getAction() == KeyEvent.ACTION_DOWN ) {
                    playerControl.seekTo( playerControl.getCurrentPosition() + 15000 ); // milliseconds
                    show();
                }
                return true;
            } else if ( playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND ) {
                if ( event.getAction() == KeyEvent.ACTION_DOWN ) {
                    playerControl.seekTo( playerControl.getCurrentPosition() - 5000 ); // milliseconds
                    show();
                }
                return true;
            }
            return super.dispatchKeyEvent( event );
        }
    }

    /**
     * Network state handling
     */
    // Network connectivity manager
    private MyHandler mHandler = new MyHandler( this );
    private NetWorkStateReceiver mNetworkReceiver = new NetWorkStateReceiver() {
        @Override
        public void onNetWorkAvailable( int netWorkType ) {
            mHandler.sendEmptyMessageDelayed( EVENT_RECOVER_PLAY_BACK, 100 );
        }

        @Override
        public void onNetWorkUnavailable() {
            preparePlayer( false );
        }
    };

    private IncomingCallReceiver mIncomingCallReceiver = new IncomingCallReceiver() {
        @Override
        public void onReceivePhoneCall() {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                Log.d( TAG, "onReceivePhoneCall => mIsRunning =", mIsRunning );
            }
            preparePlayer( false );
        }

        @Override
        public void onOffHook() {

        }

        @Override
        public void onPhoneCallEnd() {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                Log.d( TAG, "onPhoneCallEnd => mIsRunning =", mIsRunning );
                if ( mIsRunning ) {
                    preparePlayer( true );
                }
            }
        }
    };
}
