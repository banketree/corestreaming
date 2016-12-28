package com.eotu.corestreaming.demo6.http;

import java.io.IOException;

import com.eotu.corestreaming.demo6.elonen.RtspServer;
import com.eotu.corestreaming.demo6.elonen.NanoHTTPD;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WebServerService extends Service {
	private NanoHTTPD httpd;

	public WebServerService() {
		super();
		httpd = new RtspServer(8080);
		// httpd = new RtspServer(8080, null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			((RtspServer) httpd).setmSharedPreference(getSharedPreferences(
					"SDP", Context.MODE_PRIVATE));
			httpd.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		Log.i("HTTPSERVICE", "Creating and starting httpService");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		httpd.stop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}