package com.chaatz.live.player.interfaces;

/**
 * Created by Ravic on 29/12/2015.
 */
public interface Listener {
    void onStateChanged(boolean playWhenReady, int playbackState);
    void onError(Exception e);
    void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                            float pixelWidthHeightRatio);
}
