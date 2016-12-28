package com.chaatz.live;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.chaatz.live.debug.Log;
import com.chaatz.live.player.builders.CustomExoPlayer;
import com.chaatz.live.player.builders.DashRendererBuilder;
import com.chaatz.live.player.builders.HlsRendererBuilder;
import com.chaatz.live.player.callbacks.WidevineTestMediaDrmCallback;
import com.chaatz.live.player.interfaces.Listener;
import com.chaatz.live.player.interfaces.RendererBuilder;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.util.Util;

/**
 * Created by Ravic on 29/12/2015.
 */
public class DASHExoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Listener {
    private static final String TAG = "DASHExoPlayerActivity";

    public static final int TYPE_DASH = 0;
    public static final int TYPE_SS = 1;
    public static final int TYPE_HLS = 2;
    public static final int TYPE_OTHER = 3;


    public static final String CONTENT_TYPE_EXTRA = "content_type";
    public static final String CONTENT_ID_EXTRA = "content_id";

    private String contentUri = "";

    private int contentType = TYPE_DASH;
    private String contentId = "fox";
    private String provider = "provider";

    private CustomExoPlayer player;
    private boolean playerNeedsPrepare;
    private long playerPosition;
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_custom_exo_player );

        contentUri = getIntent().getStringExtra( "contentUri" );
        Log.d( TAG, contentUri );
        videoFrame = ( AspectRatioFrameLayout ) findViewById( R.id.video_frame );
        surfaceView = ( SurfaceView ) findViewById( R.id.surface_view );
        surfaceView.getHolder().addCallback( this );


    }

    @Override
    protected void onResume() {
        super.onResume();

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

    /**
     * SurfaceView Callback functions
     */
    @Override
    public void surfaceCreated( SurfaceHolder surfaceHolder ) {

    }

    @Override
    public void surfaceChanged( SurfaceHolder surfaceHolder, int i, int i1, int i2 ) {

    }

    @Override
    public void surfaceDestroyed( SurfaceHolder surfaceHolder ) {

    }

    /**
     * Setup player
     */
    private RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent( this, "ExoPlayerDemo" );
        switch ( contentType ) {
//            case TYPE_SS:
//                return new SmoothStreamingRendererBuilder( this, userAgent, contentUri.toString(),
//                        new SmoothStreamingTestMediaDrmCallback() );
            case TYPE_DASH:
                return new DashRendererBuilder( this, userAgent, contentUri.toString(),
                        new WidevineTestMediaDrmCallback( contentId, provider ) );
            case TYPE_HLS:
                return new HlsRendererBuilder( this, userAgent, contentUri.toString() );
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


        Log.e( TAG, "onStateChange : playWhenReady=" + playWhenReady + " playbackState=" + playbackState + " text=" + text );
    }

    @Override
    public void onError( Exception e ) {
        if ( e instanceof UnsupportedDrmException ) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = ( UnsupportedDrmException ) e;
            int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
            Toast.makeText( getApplicationContext(), stringId, Toast.LENGTH_LONG ).show();
        }
        playerNeedsPrepare = true;
    }

    @Override
    public void onVideoSizeChanged( int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio ) {
        Log.i( TAG, "onVideoSizeChanged: " + width + " " + height );
        videoFrame.setAspectRatio(
                height == 0 ? 1 : ( width * pixelWidthHeightRatio ) / height );
    }
}
