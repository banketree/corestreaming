package com.chaatz.live.player.builders;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Handler;

import com.chaatz.live.player.interfaces.RendererBuilder;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.chunk.VideoFormatSelectorUtil;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsMasterPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.metadata.Id3Parser;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.text.eia608.Eia608TrackRenderer;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Edmund on 1/5/16.
 */
public class HlsRendererBuilder implements RendererBuilder {
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENTS = 256;

    private final Context context;
    private final String userAgent;
    private final String url;

    private AsyncRendererBuilder currentAsyncBuilder;

    public HlsRendererBuilder( Context context, String userAgent, String url ) {
        this.context = context;
        this.userAgent = userAgent;
        this.url = url;
    }

    @Override
    public void buildRenderers( CustomExoPlayer player ) {
        currentAsyncBuilder = new AsyncRendererBuilder( context, userAgent, url, player );
        currentAsyncBuilder.init();
    }

    @Override
    public void cancel() {
        if ( currentAsyncBuilder != null ) {
            currentAsyncBuilder.cancel();
            currentAsyncBuilder = null;
        }
    }

    private static final class AsyncRendererBuilder implements ManifestCallback< HlsPlaylist > {

        private final Context context;
        private final String userAgent;
        private final String url;
        private final CustomExoPlayer player;
        private final ManifestFetcher< HlsPlaylist > playlistFetcher;

        private boolean canceled;

        public AsyncRendererBuilder( Context context, String userAgent, String url, CustomExoPlayer player ) {
            this.context = context;
            this.userAgent = userAgent;
            this.url = url;
            this.player = player;
            HlsPlaylistParser parser = new HlsPlaylistParser();
            playlistFetcher = new ManifestFetcher<>( url, new DefaultUriDataSource( context, userAgent ),
                    parser );
        }

        public void init() {
            playlistFetcher.singleLoad( player.getMainHandler().getLooper(), this );
        }

        public void cancel() {
            canceled = true;
        }

        @Override
        public void onSingleManifestError( IOException e ) {
            if ( canceled ) {
                return;
            }

            player.onRenderersError( e );
        }

        @Override
        public void onSingleManifest( HlsPlaylist manifest ) {
            if ( canceled ) {
                return;
            }

            Handler mainHandler = player.getMainHandler();
            LoadControl loadControl = new DefaultLoadControl( new DefaultAllocator( BUFFER_SEGMENT_SIZE ) );
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

            int[] variantIndices = null;
            if ( manifest instanceof HlsMasterPlaylist ) {
                HlsMasterPlaylist masterPlaylist = ( HlsMasterPlaylist ) manifest;
                try {
                    variantIndices = VideoFormatSelectorUtil.selectVideoFormatsForDefaultDisplay(
                            context, masterPlaylist.variants, null, false );
                }
                catch ( DecoderQueryException e ) {
                    player.onRenderersError( e );
                    return;
                }
                if ( variantIndices.length == 0 ) {
                    player.onRenderersError( new IllegalStateException( "No variants selected." ) );
                    return;
                }
            }

            DataSource dataSource = new DefaultUriDataSource( context, bandwidthMeter, userAgent );
            HlsChunkSource chunkSource = new HlsChunkSource( dataSource, url, manifest, bandwidthMeter,
                    variantIndices, HlsChunkSource.ADAPTIVE_MODE_SPLICE );
            HlsSampleSource sampleSource = new HlsSampleSource( chunkSource, loadControl,
                    BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, mainHandler, player, CustomExoPlayer.TYPE_VIDEO );
            MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer( context,
                    sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000, mainHandler, player, 50 );
            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer( sampleSource,
                    null, true, player.getMainHandler(), player, AudioCapabilities.getCapabilities( context ) );
            MetadataTrackRenderer< Map< String, Object > > id3Renderer = new MetadataTrackRenderer<>(
                    sampleSource, new Id3Parser(), player, mainHandler.getLooper() );
            Eia608TrackRenderer closedCaptionRenderer = new Eia608TrackRenderer( sampleSource, player,
                    mainHandler.getLooper() );

            TrackRenderer[] renderers = new TrackRenderer[ CustomExoPlayer.RENDERER_COUNT ];
            renderers[ CustomExoPlayer.TYPE_VIDEO ] = videoRenderer;
            renderers[ CustomExoPlayer.TYPE_AUDIO ] = audioRenderer;
//            renderers[CustomExoPlayer.TYPE_METADATA] = id3Renderer;
//            renderers[CustomExoPlayer.TYPE_TEXT] = closedCaptionRenderer;
            player.onRenderers( renderers, bandwidthMeter );
        }

    }


}
