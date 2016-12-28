package com.chaatz.live;

import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.chaatz.live.app.base.BaseAppCompactActivity;
import com.chaatz.live.app.controller.IncomingCallReceiver;
import com.chaatz.live.app.controller.NetWorkStateReceiver;
import com.chaatz.live.debug.Log;
import com.chaatz.live.player.animation.FloatingHeartsLayout;
import com.chaatz.live.player.builders.CustomExoPlayer;
import com.chaatz.live.player.builders.HlsRendererBuilder;
import com.chaatz.live.player.interfaces.Listener;
import com.chaatz.live.player.interfaces.RendererBuilder;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.upstream.HttpDataSource;
import com.google.android.exoplayer.util.Util;

/**
 * Created by Ravic on 29/12/2015.
 */
public class HLSExoPlayerActivity extends BaseAppCompactActivity implements
        SurfaceHolder.Callback,
        Listener,
        View.OnClickListener,
        CustomExoPlayer.InfoListener {
    private static final String TAG = "HLSExoPlayerActivity";
    public static final int TYPE_HLS = 2;
    private String contentUri = "";

    private int contentType = TYPE_HLS;

    private CustomExoPlayer player;
    private boolean playerNeedsPrepare;
    private long playerPosition = 0;
    private AspectRatioFrameLayout videoFrame;
    private FloatingHeartsLayout floatingHeartsLayout;
    private SurfaceView surfaceView;

    // Network connectivity manager
    private NetWorkStateReceiver mNetworkReceiver = new NetWorkStateReceiver() {
        @Override
        public void onNetWorkAvailable( int netWorkType ) {
            if ( player == null ) {
                preparePlayer( true );
                Log.d( TAG, "onNetworkAvailable()= player prepared." );
            } else {
                player.setBackgrounded( false );
            }
        }

        @Override
        public void onNetWorkUnavailable() {
            if ( player != null ) {
                //preparePlayer( false );
                releasePlayer();
                Log.d( TAG, "onNetworkUnavailable()= player released." );
            }
        }
    };

    private IncomingCallReceiver mIncomingCallReceiver = new IncomingCallReceiver() {
        @Override
        public void onReceivePhoneCall() {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                Log.d( TAG, "onReceivePhoneCall => mIsRunning =", mIsRunning );
            }
            if ( player != null ) {
                //preparePlayer( false );
                releasePlayer();
                Log.d( TAG, "onReceivePhoneCall()= player released." );
            }
        }

        @Override
        public void onOffHook() {

        }

        @Override
        public void onPhoneCallEnd() {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                Log.d( TAG, "onPhoneCallEnd => mIsRunning =", mIsRunning );
                if ( mIsRunning && player != null ) {
                    preparePlayer( true );
                } else {
                    player.setBackgrounded( false );
                }
            }
        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Log.d( TAG, "onCreate()" );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        setContentView( R.layout.activity_custom_exo_player );

        contentUri = getIntent().getStringExtra( "contentUri" );
        Log.d( TAG, contentUri );
        videoFrame = ( AspectRatioFrameLayout ) findViewById( R.id.video_frame );
        surfaceView = ( SurfaceView ) findViewById( R.id.surface_view );
        floatingHeartsLayout = ( FloatingHeartsLayout ) findViewById( R.id.emitter_spawn );
        surfaceView.getHolder().addCallback( this );
        surfaceView.setOnClickListener( this );
    }

    public void onClick( View view ) {
        floatingHeartsLayout.addHeart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkReceiver.registerReceiver( this );
        mIncomingCallReceiver.registerReceiver( this );
        if ( player == null ) {
            preparePlayer( true );
        } else {
            player.setBackgrounded( false );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
        Log.d( TAG, "onPause()= player released." );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        Log.d( TAG, "onDestroy()= player released." );
        mNetworkReceiver.unregisterReceiver( this );
        mIncomingCallReceiver.unregisterReceiver( this );
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
        //Do nothing. Invoke when screen rotation is called.
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
        String userAgent = Util.getUserAgent( this, "ExoPlayerDemo" );
        switch ( contentType ) {
            case TYPE_HLS:
                return new HlsRendererBuilder( this, userAgent, contentUri.toString() );
//            case TYPE_SS:
//                return new SmoothStreamingRendererBuilder( this, userAgent, contentUri.toString(),
//                        new SmoothStreamingTestMediaDrmCallback() );
//            case TYPE_OTHER:
//                return new ExtractorRendererBuilder( this, userAgent, contentUri );
            default:
                throw new IllegalStateException( "Unsupported type: " + contentType );
        }
    }

    private void preparePlayer( boolean playWhenReady ) {
        if ( player == null ) {
            player = new CustomExoPlayer( getRendererBuilder() );
            player.addListener( this );
            player.setInfoListener( this );
            player.seekTo( playerPosition );
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
            getRendererBuilder().cancel();
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        } else {
            Log.d(TAG, "releasePlayer()= player already released.");
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

    @Override
    public void onVideoSizeChanged( int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio ) {
        Log.i( TAG, "onVideoSizeChanged: w=" + width + " h=" + height );
//        videoFrame.setAspectRatio( height == 0 ? 1 : ( width * pixelWidthHeightRatio ) / height );
    }

    // Custom Exoplayer Info Listener Implementation
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
        Log.d( TAG, "onLoadStarted sourceId " + sourceId + " length " + length + " type " + type + " format " + format + " mediaStartTimeMs " + mediaStartTimeMs + " mediaEndTimeMs " + mediaEndTimeMs );
    }

    public void onLoadCompleted( int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs ) {
        Log.d( TAG, "onLoadCompleted: " + sourceId + ":" + ( format != null ? format.bitrate : null ) + " bps." );
    }

    public void onDecoderInitialized( String decoderName, long elapsedRealtimeMs, long initializationDurationMs ) {
        Log.d( TAG, "onDecoderInitialized: " + decoderName + ", elapsedRealTime: " + elapsedRealtimeMs + ", initDurationMs: " + initializationDurationMs );
    }

    public void onAvailableRangeChanged( TimeRange availableRange ) {

    }
    // End of Custom Exoplayer Implementation

}
