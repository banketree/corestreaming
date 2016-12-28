package com.eotu.corestreaming.demo6.elonen;

import com.eotu.corestreaming.demo6.elonen.NanoHTTPD.IHTTPSession;

public interface IWebSocketFactory {
    WebSocket openWebSocket(IHTTPSession handshake);
}
