package com.chaatz.live;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chaatz.live.app.base.RequestPermissionActivity;
import com.chaatz.live.app.controller.IncomingCallReceiver;
import com.chaatz.live.app.controller.NetWorkStateReceiver;
import com.chaatz.live.debug.Log;

import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 17/12/2015.
 */
public class Example3Activity extends RequestPermissionActivity implements
		OnClickListener,
		RtspClient.Callback,
		Session.Callback,
		SurfaceHolder.Callback
//		RadioGroup.OnCheckedChangeListener
{

	public final static String TAG = "Example3Activity";

	public final AudioQuality mAudioQuality = new AudioQuality( 8000, 16 * 1000 );
	public final VideoQuality mVideoQuality = new VideoQuality( 480, 640, 24, 600 * 1000 );

	private Button mButtonSave;
//	private Button mButtonVideo;
	private ImageButton mButtonStart;
	private ImageButton mButtonFlash;
	private ImageButton mButtonCamera;
	private ImageButton mButtonSettings;
//	private RadioGroup mRadioGroup;
//	private FrameLayout mLayoutVideoSettings;
	private FrameLayout mLayoutServerSettings;
	private SurfaceView mSurfaceView;
	private TextView mTextBitrate;
	private EditText mEditTextURI;
	private EditText mEditTextPassword;
	private EditText mEditTextUsername;
	private ProgressBar mProgressBar;
	private Session mSession;
	private RtspClient mClient;
	private String mUsername ="user" ;
	private String mPassword ="bu_nee9d";

    private IncomingCallReceiver mIncomingCallReceiver = new IncomingCallReceiver() {

        @Override
        public void onReceivePhoneCall() {
			stopStreaming( "incoming call." );
        }

        @Override
        public void onOffHook() {
        }

        @Override
        public void onPhoneCallEnd() {
            //TODO phone caller drop the call or user drop a phone call
        }
    };

	private NetWorkStateReceiver mNetWorkStateReceiver = new NetWorkStateReceiver() {
		@Override
		public void onNetWorkAvailable( int netWorkType ) {

		}

		@Override
		public void onNetWorkUnavailable() {
			stopStreaming( "network unavailable." );
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		Log.d( TAG, "onCreate()" );
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.example3_main);

//		mButtonVideo = (Button) findViewById(R.id.video);
		mButtonSave = (Button) findViewById(R.id.save);
		mButtonStart = (ImageButton) findViewById(R.id.start);
		mButtonFlash = (ImageButton) findViewById(R.id.flash);
		mButtonCamera = (ImageButton) findViewById(R.id.camera);
		mButtonSettings = (ImageButton) findViewById(R.id.settings);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		mEditTextURI = (EditText) findViewById(R.id.uri);
//		mEditTextUsername = (EditText) findViewById(R.id.username);
//		mEditTextPassword = (EditText) findViewById(R.id.password);
		mTextBitrate = (TextView) findViewById(R.id.bitrate);
//		mLayoutVideoSettings = (FrameLayout) findViewById(R.id.video_layout);
		mLayoutServerSettings = (FrameLayout) findViewById(R.id.server_layout);
//		mRadioGroup =  (RadioGroup) findViewById(R.id.radio);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

//		mRadioGroup.setOnCheckedChangeListener(this);
//		mRadioGroup.setOnClickListener(this);

		mButtonStart.setOnClickListener(this);
		mButtonSave.setOnClickListener(this);
		mButtonFlash.setOnClickListener(this);
		mButtonCamera.setOnClickListener(this);
//		mButtonVideo.setOnClickListener(this);
		mButtonSettings.setOnClickListener(this);
		mButtonFlash.setTag( "off" );
		mButtonCamera.setVisibility( View.GONE );
		mButtonFlash.setVisibility( View.GONE );
		final int cameraCount = Camera.getNumberOfCameras();
		if (cameraCount >1 ){
			mButtonCamera.setVisibility( View.VISIBLE );
		}
		boolean hasFlash = getApplicationContext().getPackageManager()
				.hasSystemFeature( PackageManager.FEATURE_CAMERA_FLASH);
		if ( hasFlash ){
			mButtonFlash.setVisibility( View.VISIBLE );
		}

//		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//		if (mPrefs.getString("uri", null) != null) mLayoutServerSettings.setVisibility(View.GONE);
//		mEditTextURI.setText(mPrefs.getString("uri", getString(R.string.default_stream)));
//		mEditTextPassword.setText(mPrefs.getString("password", ""));
//		mEditTextUsername.setText(mPrefs.getString("username", ""));

		//Modified: find the video resolution in QUALITY_480P level
		int qualityLevel = CamcorderProfile.QUALITY_480P;
		CamcorderProfile profile = CamcorderProfile.get(qualityLevel);
		mVideoQuality.resX = profile.videoFrameHeight;
		mVideoQuality.resY = profile.videoFrameWidth;

		// Configures the SessionBuilder
		mSession = SessionBuilder.getInstance()
				.setContext( getApplicationContext() )
				.setAudioEncoder( SessionBuilder.AUDIO_AAC )
				.setAudioQuality( mAudioQuality )
				.setVideoEncoder( SessionBuilder.VIDEO_H264 )
				.setVideoQuality( mVideoQuality )
				.setSurfaceView( mSurfaceView )
				.setPreviewOrientation( 90 )
				.setCallback( this )
				.build();

		// Configures the RTSP client
		mClient = new RtspClient();
		mClient.setSession(mSession);
		mClient.setCallback(this);

		// Use this to force streaming with the MediaRecorder API
		//mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIARECORDER_API);
		byte mode = MediaStream.MODE_MEDIACODEC_API;
		if (isSurfaceToBufferSupported()) {
			mode = MediaStream.MODE_MEDIACODEC_API_2;
		}
		mSession.getVideoTrack().setStreamingMethod(mode);
		mSession.getVideoTrack().setPortrait(true);

		// Use this to stream over TCP, EXPERIMENTAL!
		mClient.setTransportMode(RtspClient.TRANSPORT_TCP);

		// Use this if you want the aspect ratio of the surface view to
		// respect the aspect ratio of the camera preview
		mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);
		mSurfaceView.setFrameRate(mVideoQuality.framerate);

		mSurfaceView.getHolder().addCallback(this);

//		selectQuality();

	}

//	@Override
//	public void onCheckedChanged(RadioGroup group, int checkedId) {
//		mLayoutVideoSettings.setVisibility(View.GONE);
//		mLayoutServerSettings.setVisibility( View.VISIBLE );
//		selectQuality();
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.start:
				mLayoutServerSettings.setVisibility(View.GONE);
				toggleStream();
				break;
			case R.id.flash:
				if (mButtonFlash.getTag().equals("on")) {
					mButtonFlash.setTag("off");
					mButtonFlash.setImageResource(R.mipmap.ic_flash_on_holo_light);
				} else {
					mButtonFlash.setImageResource(R.mipmap.ic_flash_off_holo_light);
					mButtonFlash.setTag("on");
				}
				mSession.toggleFlash();
				break;
			case R.id.camera:
				mSession.switchCamera();
				break;
			case R.id.settings:
//				if (mLayoutVideoSettings.getVisibility() == View.GONE &&
//						mLayoutServerSettings.getVisibility() == View.GONE) {
//					mLayoutServerSettings.setVisibility(View.VISIBLE);
//				} else {
//					mLayoutServerSettings.setVisibility(View.GONE);
//					mLayoutVideoSettings.setVisibility(View.GONE);
//				}
				mLayoutServerSettings.setVisibility(mLayoutServerSettings.getVisibility() == View.GONE ? View.VISIBLE: View.GONE);
				break;
//			case R.id.video:
//				mRadioGroup.clearCheck();
//				mLayoutServerSettings.setVisibility(View.GONE);
//				mLayoutVideoSettings.setVisibility(View.VISIBLE);
//				break;
			case R.id.save:
				View view = this.getCurrentFocus();
				if (view != null) {
					InputMethodManager imm = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
				mLayoutServerSettings.setVisibility(View.GONE);
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIncomingCallReceiver.registerReceiver( this );
		mNetWorkStateReceiver.registerReceiver( this );
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy(){
		Log.d( TAG, "onDestroy()" );
		super.onDestroy();
		mClient.release();
		mSession.release();
		mSurfaceView.getHolder().removeCallback( this );
		mIncomingCallReceiver.unregisterReceiver( this );
		mNetWorkStateReceiver.unregisterReceiver( this );
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

//	private void selectQuality() {
//		int id = mRadioGroup.getCheckedRadioButtonId();
//		RadioButton button = (RadioButton) findViewById(id);
//		if (button == null) return;
//
//		String text = button.getText().toString();
//		Pattern pattern = Pattern.compile("(\\d+)x(\\d+)\\D+(\\d+)\\D+(\\d+)");
//		Matcher matcher = pattern.matcher(text);
//
//		matcher.find();
//		int width = Integer.parseInt(matcher.group(1));
//		int height = Integer.parseInt(matcher.group(2));
//		int framerate = Integer.parseInt(matcher.group(3));
//		int bitrate = Integer.parseInt(matcher.group(4))*1000;
//
//		mSession.setVideoQuality(new VideoQuality(width, height, framerate, bitrate));
//		Toast.makeText(this, ((RadioButton)findViewById(id)).getText(), Toast.LENGTH_SHORT).show();
//
//		Log.d(TAG, "Selected resolution: "+width+"x"+height);
//	}

	private void enableUI() {
		Log.d( TAG, "enableUI()" );
		mButtonStart.setEnabled(true);
		mButtonCamera.setEnabled(true);
	}

	// Connects/disconnects to the RTSP server and starts/stops the stream
	public void toggleStream() {
		Log.d( TAG, "toggleStream()" );
		mProgressBar.setVisibility( View.VISIBLE );
		if (!mClient.isStreaming()) {
			String ip,port,path;

			// We save the content user inputs in Shared Preferences
//			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//			SharedPreferences.Editor editor = mPrefs.edit();
//			editor.putString("uri", mEditTextURI.getText().toString());
//			editor.putString("password", mEditTextPassword.getText().toString());
//			editor.putString("username", mEditTextUsername.getText().toString());
//			editor.commit();

			// We parse the URI written in the Editext
			Pattern uri = Pattern.compile("rtsp://(.+):(\\d*)/(.+)");
			String streamRom = mEditTextURI.getText().toString();
			Matcher m = uri.matcher(getResources().getString( R.string.rstp_default_stream ) + streamRom); m.find();
			Log.d( TAG, "StreamLink=" +m );
			ip = m.group(1);
			port = m.group(2);
			path = m.group(3);

			mClient.setCredentials(mUsername, mPassword);
			mClient.setServerAddress(ip, Integer.parseInt(port));
			mClient.setStreamPath("/"+path);
			mClient.startStream();

		} else {
			// Stops the stream and disconnects from the RTSP server
			mClient.stopStream();
		}
	}

	private void logError(final String msg) {
		Log.d( TAG, "logError() msg=" + msg );
		final String error = (msg == null) ? "Error unknown" : msg;
		// Displays a popup to report the eror to the user
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg).setPositiveButton( "OK", new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int id ) {
			}
		} );
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onBitrateUpdate(long bitrate) {
//		Log.d( TAG, "onBitrateUpdate() bitrate=" + bitrate );
		mTextBitrate.setText( "" + bitrate / 1000 + " kbps" );
	}

	@Override
	public void onPreviewStarted() {
		Log.d( TAG, "onPreviewStarted()" );
		if (mSession.getCamera() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			mButtonFlash.setEnabled(false);
			mButtonFlash.setTag( "off" );
			mButtonFlash.setVisibility( View.GONE );
			mButtonFlash.setImageResource(R.mipmap.ic_flash_on_holo_light);
		}
		else {
			mButtonFlash.setEnabled( true );
			mButtonFlash.setVisibility( View.VISIBLE);
		}
	}

	@Override
	public void onSessionConfigured() {
		Log.d(TAG, "onSessionConfigured()");
	}

	@Override
	public void onSessionStarted() {
		Log.d(TAG, "onSessionStarted()");
		enableUI();
		mButtonStart.setImageResource(R.mipmap.ic_switch_video_active);
		mProgressBar.setVisibility( View.GONE );
	}

	@Override
	public void onSessionStopped() {
		Log.d( TAG, "onSessionStopped()" );
		enableUI();
		mButtonStart.setImageResource(R.mipmap.ic_switch_video);
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e) {
		Log.d( TAG, "onSessionError() reason=" + reason + " streamType=" + streamType );
		mProgressBar.setVisibility( View.GONE );
		switch (reason) {
			case Session.ERROR_CAMERA_ALREADY_IN_USE:
				break;
			case Session.ERROR_CAMERA_HAS_NO_FLASH:
				mButtonFlash.setImageResource(R.mipmap.ic_flash_on_holo_light);
				mButtonFlash.setTag("off");
				break;
			case Session.ERROR_INVALID_SURFACE:
				break;
			case Session.ERROR_STORAGE_NOT_READY:
				break;
			case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
				VideoQuality quality = mSession.getVideoTrack().getVideoQuality();
				logError("The following settings are not supported on this phone: "+
						quality.toString()+" "+
						"("+e.getMessage()+")");
				e.printStackTrace();
				return;
			case Session.ERROR_OTHER:
				break;
		}

		if (e != null) {
			logError(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void onRtspUpdate(int message, Exception e) {
		Log.d( TAG, "onRtspUpdate() message=" + message );
		switch (message) {
			case RtspClient.ERROR_CONNECTION_FAILED:
			case RtspClient.ERROR_WRONG_CREDENTIALS:
				mProgressBar.setVisibility(View.GONE);
				enableUI();
				logError( e.getMessage() );
				e.printStackTrace();
				break;
		}
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d( TAG, "surfaceChanged() format="+format+" width="+width+" height="+height );
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d( TAG, "surfaceCreated()" );
		mSession.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d( TAG, "surfaceDestroyed()" );
		mClient.stopStream();
	}

	@Override
	protected void onPermissionGrated( int requestCode ) {

	}

	@Override
	protected void onRequestPermissionFail( int requestCode ) {

	}

	protected void stopStreaming( String reason ){
		if ( null != mClient && mClient.isStreaming() ) {
			Log.d( TAG, "stop live streaming reason : " + reason );
			mClient.stopStream();
		}
	}

	private boolean isSurfaceToBufferSupported() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return false;
		}
		for (int i=0; i< MediaCodecList.getCodecCount(); i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			if (!info.isEncoder()) {
				continue;
			}
			String[] types = info.getSupportedTypes();
			for (String type: types) {
				if (!"video/avc".equalsIgnoreCase(type)) {
					continue;
				}
				for (int color: info.getCapabilitiesForType(type).colorFormats) {
					if (color == MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
