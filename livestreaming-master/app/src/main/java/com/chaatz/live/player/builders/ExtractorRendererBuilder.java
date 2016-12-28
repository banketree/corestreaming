package com.chaatz.live.player.builders;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.chaatz.live.player.interfaces.RendererBuilder;

/**
 * Created by Ravic on 29/12/2015.
 */
public class ExtractorRendererBuilder implements RendererBuilder {

    private static String TAG = "ExtractorRendererBuilder";
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    private final Context context;
    private final String userAgent;
    private final Uri uri;

    public ExtractorRendererBuilder(Context context, String userAgent, Uri uri) {
        this.context = context;
        this.userAgent = userAgent;
        this.uri = uri;

        Log.i(TAG, "userAgent: " + userAgent + " uri: " + uri);
    }

    @Override
    public void buildRenderers(CustomExoPlayer player) {
        Log.d(TAG, "buildRenders");
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);

        // Build the video and audio renderers.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(player.getMainHandler(),
                null);
        DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 2000
                , null, true, player.getMainHandler(), player, 50);

        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
                null, true, player.getMainHandler(), player, AudioCapabilities.getCapabilities(context));

        // Invoke the callback.
        TrackRenderer[] renderers = new TrackRenderer[CustomExoPlayer.RENDERER_COUNT];
        renderers[CustomExoPlayer.TYPE_VIDEO] = videoRenderer;
        renderers[CustomExoPlayer.TYPE_AUDIO] = audioRenderer;
        player.onRenderers(renderers, bandwidthMeter);
    }

    @Override
    public void cancel() {

    }
}
