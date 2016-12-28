package com.eotu.corestreaming.demo7.spydroid.api;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

public class CustomRtspServer extends RtspServer {
	public CustomRtspServer() {
		super();
		// RTSP server disabled by default
		mEnabled = false;
	}
}

