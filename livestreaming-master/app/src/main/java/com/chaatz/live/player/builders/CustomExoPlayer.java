package com.chaatz.live.player.builders;

import android.media.MediaCodec;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.chaatz.live.player.interfaces.Listener;
import com.chaatz.live.player.interfaces.RendererBuilder;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.TextRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.util.PlayerControl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Ravic on 29/12/2015.
 */
public class CustomExoPlayer implements ExoPlayer.Listener,
        DefaultBandwidthMeter.EventListener,
        MediaCodecVideoTrackRenderer.EventListener,
        MediaCodecAudioTrackRenderer.EventListener,
        ChunkSampleSource.EventListener,
        DashChunkSource.EventListener,
        StreamingDrmSessionManager.EventListener, HlsSampleSource.EventListener, MetadataTrackRenderer.MetadataRenderer< Map< String, Object > >, TextRenderer {

    // Constants pulled into this class for convenience.
    public static final int STATE_IDLE = ExoPlayer.STATE_IDLE;
    public static final int STATE_PREPARING = ExoPlayer.STATE_PREPARING;
    public static final int TRACK_DISABLED = ExoPlayer.TRACK_DISABLED;
    public static final int STATE_BUFFERING = ExoPlayer.STATE_BUFFERING;
    public static final int STATE_READY = ExoPlayer.STATE_READY;
    public static final int STATE_ENDED = ExoPlayer.STATE_ENDED;
    public static final int TRACK_DEFAULT = ExoPlayer.TRACK_DEFAULT;
    public static final int RENDERER_COUNT = 2;
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_TEXT = 2;
    public static final int TYPE_METADATA = 3;

    private final static String TAG = "CustomExoPlayer";
    private static final int RENDERER_BUILDING_STATE_IDLE = 1;
    private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
    private static final int RENDERER_BUILDING_STATE_BUILT = 3;

    private final RendererBuilder rendererBuilder;
    private final ExoPlayer player;
    private final PlayerControl playerControl;
    private final Handler mainHandler;
    private final CopyOnWriteArrayList< Listener > listeners;

    private int rendererBuildingState;
    private int lastReportedPlaybackState;
    private boolean lastReportedPlayWhenReady;
    private boolean backgrounded;
    private int videoTrackToRestore;
    private long bitrateEstimate;

    private Surface surface;
    private TrackRenderer videoRenderer;
    private Format videoFormat;

    private CaptionListener captionListener;
    private Id3MetadataListener id3MetadataListener;
    private InternalErrorListener internalErrorListener;
    private InfoListener infoListener;


    public CustomExoPlayer( RendererBuilder rendererBuilder ) {
        Log.d( TAG, "Constructor" );
        this.rendererBuilder = rendererBuilder;
        this.player = ExoPlayer.Factory.newInstance( RENDERER_COUNT, 1000, 1000 );
        this.player.addListener( this );
        this.playerControl = new PlayerControl( player );
        this.mainHandler = new Handler();
        this.listeners = new CopyOnWriteArrayList<>();
        this.lastReportedPlaybackState = STATE_IDLE;
        this.rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
    }

    /**
     * A listener for internal errors.
     * <p/>
     * These errors are not visible to the user, and hence this listener is provided for
     * informational purposes only. Note however that an internal error may cause a fatal
     * error if the player fails to recover. If this happens, {@link Listener#onError(Exception)}
     * will be invoked.
     */
    public interface InternalErrorListener {
        void onRendererInitializationError( Exception e );

        void onAudioTrackInitializationError( AudioTrack.InitializationException e );

        void onAudioTrackWriteError( AudioTrack.WriteException e );

        void onAudioTrackUnderrun( int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs );

        void onDecoderInitializationError( MediaCodecTrackRenderer.DecoderInitializationException e );

        void onCryptoError( MediaCodec.CryptoException e );

        void onLoadError( int sourceId, IOException e );

        void onDrmSessionManagerError( Exception e );
    }

    /**
     * A listener for debugging information.
     */
    public interface InfoListener {

        void onVideoFormatEnabled( Format format, int trigger, long mediaTimeMs );

        void onAudioFormatEnabled( Format format, int trigger, long mediaTimeMs );

        void onDroppedFrames( int count, long elapsed );

        void onBandwidthSample( int elapsedMs, long bytes, long bitrateEstimate );

        void onLoadStarted( int sourceId, long length, int type, int trigger, Format format,
                            long mediaStartTimeMs, long mediaEndTimeMs );

        void onLoadCompleted( int sourceId, long bytesLoaded, int type, int trigger, Format format,
                              long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs );

        void onDecoderInitialized( String decoderName, long elapsedRealtimeMs,
                                   long initializationDurationMs );

        void onAvailableRangeChanged( TimeRange availableRange );
    }

    public PlayerControl getPlayerControl() {
        return playerControl;
    }

    public void addListener( Listener listener ) {
        Log.d( TAG, "addListener" );
        listeners.add( listener );
    }

    public void setInfoListener( InfoListener listener ) {
        infoListener = listener;
    }

    public void removeListener( Listener listener ) {
        Log.d( TAG, "removeListener" );
        listeners.remove( listener );
    }

    public void setSurface( Surface surface ) {
        Log.i( TAG, "setSurface " + surface.toString() );
        this.surface = surface;
        pushSurface( false );
    }

    public Surface getSurface() {
        return surface;
    }

    public void blockingClearSurface() {
        surface = null;
        pushSurface( true );
    }


    private void pushSurface( boolean blockForSurfacePush ) {
        Log.i( TAG, "pushSurface " + blockForSurfacePush );
        if ( videoRenderer == null ) {
            Log.e( TAG, "pushSurface (videoRenderer == null)" );
            return;
        }

        if ( blockForSurfacePush ) {
            player.blockingSendMessage(
                    videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface );
        } else {
            player.sendMessage(
                    videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface );
        }
    }

    /**
     * Invoked with the results from a {@link RendererBuilder}.
     *
     * @param renderers      Renderers indexed by {@link CustomExoPlayer} TYPE_* constants. An individual
     *                       element may be null if there do not exist tracks of the corresponding type.
     * @param bandwidthMeter Provides an estimate of the currently available bandwidth. May be null.
     */
    public void onRenderers( TrackRenderer[] renderers, BandwidthMeter bandwidthMeter ) {
        Log.d( TAG, "onRenderers" );
        for ( int i = 0; i < RENDERER_COUNT; i++ ) {
            if ( renderers[ i ] == null ) {
                Log.i( TAG, "onRenderers dummy " + i );
                // Convert a null renderer to a dummy renderer.
                renderers[ i ] = new DummyTrackRenderer();
            }
        }
        // Complete preparation.
        this.videoRenderer = renderers[ TYPE_VIDEO ];
//        this.codecCounters = videoRenderer instanceof MediaCodecTrackRenderer
//        ? ((MediaCodecTrackRenderer) videoRenderer).codecCounters
//        : renderers[TYPE_AUDIO] instanceof MediaCodecTrackRenderer
//        ? ((MediaCodecTrackRenderer) renderers[TYPE_AUDIO]).codecCounters : null;
//        this.bandwidthMeter = bandwidthMeter;
        pushSurface( false );
        player.prepare( renderers );
        rendererBuildingState = RENDERER_BUILDING_STATE_BUILT;
    }

    /**
     * Invoked if a {@link RendererBuilder} encounters an error.
     *
     * @param e Describes the error.
     */
    public void onRenderersError( Exception e ) {
        Log.e( TAG, "onRenderersError not implemented\nCheck contentUri in DASHExoPlayerActivity.java" );
        Log.e( TAG, Log.getStackTraceString( e ) );

        if ( internalErrorListener != null ) {
            internalErrorListener.onRendererInitializationError( e );
        }
        for ( Listener listener : listeners ) {
            listener.onError( e );
        }
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
        maybeReportPlayerState();
    }

    public void setPlayWhenReady( boolean playWhenReady ) {
        Log.i( TAG, "setPlayWhenReady " + playWhenReady );
        player.setPlayWhenReady( playWhenReady );
    }

    public void seekTo( long positionMs ) {
        Log.i( TAG, "seekTo " + positionMs );
        player.seekTo( positionMs );
    }

    public long getCurrentPosition() {
        long positionMs = player.getCurrentPosition();
        Log.i( TAG, "getCurrentPosition " + positionMs );
        return player.getCurrentPosition();
    }

//    @Override
//    public Format getFormat() {
//        return null;
//    }
//
//    @Override
//    public BandwidthMeter getBandwidthMeter() {
//        return null;
//    }
//
//    @Override
//    public CodecCounters getCodecCounters() {
//        return null;
//    }

    public void release() {
        Log.d( TAG, "release()" );
        rendererBuilder.cancel();
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
        surface = null;
        player.release();
    }

    public void prepare() {
        Log.d( TAG, "prepare()" );
        if ( rendererBuildingState == RENDERER_BUILDING_STATE_BUILT ) {
            player.stop();
        }
        rendererBuilder.cancel();
//        videoFormat = null;
        videoRenderer = null;
        rendererBuildingState = RENDERER_BUILDING_STATE_BUILDING;
        maybeReportPlayerState();
        rendererBuilder.buildRenderers( this );
    }


    private void maybeReportPlayerState() {
        boolean playWhenReady = player.getPlayWhenReady();
        int playbackState = getPlaybackState();
        if ( lastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState ) {
            for ( Listener listener : listeners ) {
                listener.onStateChanged( playWhenReady, playbackState );
            }
            lastReportedPlayWhenReady = playWhenReady;
            lastReportedPlaybackState = playbackState;
        }
        Log.i( TAG, "maybeReportPlayerState lastReportedPlayWhenReady=" + lastReportedPlayWhenReady + " lastReportedPlaybackState = " + lastReportedPlaybackState );
    }

    public int getPlaybackState() {
        if ( rendererBuildingState == RENDERER_BUILDING_STATE_BUILDING ) {
            return STATE_PREPARING;
        }

        int playerState = player.getPlaybackState();
        if ( rendererBuildingState == RENDERER_BUILDING_STATE_BUILT && playerState == STATE_IDLE ) {
            // This is an edge case where the renderers are built, but are still being passed to the
            // player's playback thread.
            return STATE_PREPARING;
        }
        return playerState;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    public Looper getPlaybackLooper() {
        return player.getPlaybackLooper();
    }

    @Override
    public void onPlayerStateChanged( boolean playWhenReady, int state ) {
        maybeReportPlayerState();
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        // Do nothing.
    }

    @Override
    public void onPlayerError( ExoPlaybackException exception ) {
        rendererBuildingState = RENDERER_BUILDING_STATE_IDLE;
        for ( Listener listener : listeners ) {
            listener.onError( exception );
        }
    }

    @Override
    public void onAudioTrackInitializationError( AudioTrack.InitializationException e ) {
        if ( internalErrorListener != null ) {
            internalErrorListener.onAudioTrackInitializationError( e );
        }
    }

    @Override
    public void onAudioTrackWriteError( AudioTrack.WriteException e ) {
        if ( internalErrorListener != null ) {
            internalErrorListener.onAudioTrackWriteError( e );
        }
    }

    @Override
    public void onAudioTrackUnderrun( int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs ) {
        if ( internalErrorListener != null ) {
            internalErrorListener.onAudioTrackUnderrun( bufferSize, bufferSizeMs, elapsedSinceLastFeedMs );
        }
    }

    // MediaCodecVideoTrackRenderer.EventListener implementation
    @Override
    public void onDroppedFrames( int count, long elapsed ) {
        if ( infoListener != null ) {
            infoListener.onDroppedFrames( count, elapsed );
        }
    }

    @Override
    public void onVideoSizeChanged( int width, int height, int unappliedRotationDegrees,
                                    float pixelWidthHeightRatio ) {
        for ( Listener listener : listeners ) {
            listener.onVideoSizeChanged( width, height, unappliedRotationDegrees, pixelWidthHeightRatio );
        }
    }

    @Override
    public void onDrawnToSurface( Surface surface ) {
        // Do nothing.
    }

    // MediaCodecTrackRenderer.EventListener implementation
    @Override
    public void onDecoderInitializationError( MediaCodecTrackRenderer.DecoderInitializationException e ) {
        if ( internalErrorListener != null ) {
            internalErrorListener.onDecoderInitializationError( e );
        }
    }

    @Override
    public void onCryptoError( MediaCodec.CryptoException e ) {
        if ( internalErrorListener != null ) {
            internalErrorListener.onCryptoError( e );
        }
    }

    @Override
    public void onDecoderInitialized( String decoderName, long elapsedRealtimeMs, long initializationDurationMs ) {
        if ( infoListener != null ) {
            infoListener.onDecoderInitialized( decoderName, elapsedRealtimeMs, initializationDurationMs );
        }
    }

    @Override
    public void onBandwidthSample( int elapsedMs, long bytes, long bitrateEstimate ) {
        if ( infoListener != null ) {
            infoListener.onBandwidthSample( elapsedMs, bytes, bitrateEstimate );
        }
    }

    // DashChunkSource.EventListener implementation
    @Override
    public void onAvailableRangeChanged( TimeRange availableRange ) {
        if ( infoListener != null ) {
            infoListener.onAvailableRangeChanged( availableRange );
        }
    }

    // ChunkSampleSource.EventListener implementation

    @Override
    public void onLoadStarted( int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs ) {
        if ( infoListener != null ) {
            infoListener.onLoadStarted( sourceId, length, type, trigger, format, mediaStartTimeMs,
                    mediaEndTimeMs );
        }
    }

    @Override
    public void onLoadCompleted( int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs ) {
        if ( infoListener != null ) {
            infoListener.onLoadCompleted( sourceId, bytesLoaded, type, trigger, format, mediaStartTimeMs,
                    mediaEndTimeMs, elapsedRealtimeMs, loadDurationMs );
        }
    }

    @Override
    public void onLoadCanceled( int sourceId, long bytesLoaded ) {

    }

    @Override
    public void onLoadError( int sourceId, IOException e ) {
        if ( internalErrorListener != null ) {
            internalErrorListener.onLoadError( sourceId, e );
        }
    }

    @Override
    public void onUpstreamDiscarded( int sourceId, long mediaStartTimeMs, long mediaEndTimeMs ) {

    }

    @Override
    public void onDownstreamFormatChanged( int sourceId, Format format, int trigger, long mediaTimeMs ) {
        if ( infoListener == null ) {
            return;
        }
        if ( sourceId == TYPE_VIDEO ) {
            videoFormat = format;
            infoListener.onVideoFormatEnabled( format, trigger, mediaTimeMs );
        } else if ( sourceId == TYPE_AUDIO ) {
            infoListener.onAudioFormatEnabled( format, trigger, mediaTimeMs );
        }

    }

    // StreamingDrmSessionManager.EventListener implementation

    @Override
    public void onDrmKeysLoaded() {
        // Do nothing.
    }

    @Override
    public void onDrmSessionManagerError( Exception e ) {
        if ( internalErrorListener != null ) {
            internalErrorListener.onDrmSessionManagerError( e );
        }
    }

    @Override
    public void onMetadata( Map< String, Object > metadata ) {
        if ( id3MetadataListener != null && getSelectedTrack( TYPE_METADATA ) != TRACK_DISABLED ) {
            id3MetadataListener.onId3Metadata( metadata );
        }
    }

    @Override
    public void onCues( List< Cue > cues ) {
        if ( captionListener != null && getSelectedTrack( TYPE_TEXT ) != TRACK_DISABLED ) {
            captionListener.onCues( cues );
        }
    }

    public interface CaptionListener {
        void onCues( List< Cue > cues );
    }

    public interface Id3MetadataListener {
        void onId3Metadata( Map< String, Object > metadata );
    }

    public int getSelectedTrack( int type ) {
        return player.getSelectedTrack( type );
    }

    public void setSelectedTrack( int type, int index ) {
        player.setSelectedTrack( type, index );
        if ( type == TYPE_TEXT && index < 0 && captionListener != null ) {
            captionListener.onCues( Collections.< Cue >emptyList() );
        }
    }

    public boolean getBackgrounded() {
        return backgrounded;
    }

    public boolean getPlayWhenReady() {
        boolean playWhenReady = player.getPlayWhenReady();
        return playWhenReady;
    }

    public void setBackgrounded( boolean backgrounded ) {
        if ( this.backgrounded == backgrounded ) {
            return;
        }
        this.backgrounded = backgrounded;
        if ( backgrounded ) {
            videoTrackToRestore = getSelectedTrack( TYPE_VIDEO );
            setSelectedTrack( TYPE_VIDEO, TRACK_DISABLED );
            blockingClearSurface();
        } else {
            setSelectedTrack( TYPE_VIDEO, videoTrackToRestore );
        }
    }

}
