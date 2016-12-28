package com.chaatz.live.app;

import android.os.Handler;
import android.os.Message;

import com.chaatz.live.BuildConfig;
import com.chaatz.live.debug.Log;

import java.lang.ref.WeakReference;

/**
 * @see http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
 */
public class MyHandler extends Handler
{
	/**
	 * Callback interface.
	 */
	public interface Callback
	{
		 void handleMessage( Message msg );
	}

	private final WeakReference<Callback> mCallback;
	// only used for debug purpose.
	private String mCallbackClassName;

	public MyHandler( Callback callback )
	{
		mCallback = new WeakReference<>( callback );
		if ( BuildConfig.DEBUG ) {
			mCallbackClassName = callback.getClass().getName();
		}
	}

	@Override
	public void handleMessage( Message msg )
	{
		Callback callback = mCallback.get();
		if ( null != callback ) {
			callback.handleMessage( msg );
		}
		else if ( BuildConfig.DEBUG ) {
			Log.w( "MyHandler", "Callback is missing. Original class is: " + mCallbackClassName );
		}
	}

}
