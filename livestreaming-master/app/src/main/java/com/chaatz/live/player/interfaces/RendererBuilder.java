package com.chaatz.live.player.interfaces;

import com.chaatz.live.player.builders.CustomExoPlayer;

/**
 * Created by Ravic on 29/12/2015.
 */
public interface RendererBuilder {
    /**
     * Builds renderers for playback.
     *
     * @param player The player for which renderers are being built. {@link CustomExoPlayer#onRenderers}
     *     should be invoked once the renderers have been built. If building fails,
     *     {@link CustomExoPlayer#onRenderersError} should be invoked.
     */
    void buildRenderers(CustomExoPlayer player);
    /**
     * Cancels the current build operation, if there is one. Else does nothing.
     * <p>
     * A canceled build operation must not invoke {@link CustomExoPlayer#onRenderers} or
     * {@link CustomExoPlayer#onRenderersError} on the player, which may have been released.
     */
    void cancel();
}
