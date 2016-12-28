package com.chaatz.live.app.base;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.chaatz.live.BuildConfig;
import com.chaatz.live.debug.ViewServer;

public class BaseActivity extends ActionBarActivity
{
	protected final int DIALOG_ID_IN_PROGRESS = 1100;
	protected final int DIALOG_ID_NO_NETWORK = 1101;
	protected final int DIALOG_ID_NO_NETWORK_AND_RETURN_MAIN = 1102;
	protected final int DIALOG_ID_REQUEST_TIME_OUT_AND_RETURN_MAIN = 1103;

	public boolean mIsRunning = false;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		if ( BuildConfig.DEBUG ) {
			ViewServer.get( this ).addWindow( this );
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if ( BuildConfig.DEBUG ) {
			ViewServer.get( this ).removeWindow( this );
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if ( BuildConfig.DEBUG ) {
			ViewServer.get( this ).setFocusedWindow( this );
		}
		mIsRunning = true;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mIsRunning = false;
	}

	protected Dialog onCreateProgressDialog( Bundle bd )
	{
		ProgressDialog dialog = new ProgressDialog( this );
		dialog.setCancelable( false );
		return dialog;
	}

//	protected void onPrepareProgressDialog( Dialog dialog, Bundle bundle )
//	{
//		CharSequence msg = getString( R.string.loading_ellipsis );
//		if ( null != bundle ) {
//			msg = bundle.getCharSequence( SharedConsts.MESSAGE );
//		}
//		( (ProgressDialog ) dialog ).setMessage( msg );
//	}
//
//	@SuppressWarnings("deprecation")
//	protected void tryDismissDialog( int id )
//	{
//		try {
//			dismissDialog( id );
//		}
//		catch ( IllegalArgumentException e ) {
//		}
//	}
//
//	protected Dialog onCreateNoNetWorkDialog()
//	{
//		Dialog dialog = new AlertDialog.Builder( this ) //
//				.setIcon( R.drawable.ic_action_warning ) //
//				.setTitle( R.string.reg_dlg_cannot_connect_title ) //
//				.setMessage( R.string.reg_dlg_no_internet ) //
//				.setPositiveButton( android.R.string.ok, null ) //
//				.create();
//		return dialog;
//	}
//
//	protected Dialog onCreateNoNetWorkAndReturnMainDialog()
//	{
//		Dialog dialog = new AlertDialog.Builder( this ) //
//				.setIcon( R.drawable.ic_action_warning ) //
//				.setTitle( R.string.reg_dlg_cannot_connect_title ) //
//				.setMessage( R.string.reg_dlg_no_internet ) //
//				.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick( DialogInterface dialog, int which )
//					{
//						Intent intent = new Intent( BaseActivity.this, MainActivity.class );
//						startActivity( intent );
//						finish();
//					}
//				} ).create();
//		return dialog;
//	}
//
//	protected Dialog onCreateTmeOutAndReturnMainDialog()
//	{
//		Dialog dialog = new AlertDialog.Builder( this ) //
//				.setIcon( R.drawable.ic_action_warning ) //
//				.setTitle( R.string.dialog_request_time_out_title ) //
//				.setMessage( R.string.dialog_request_time_out_content ) //
//				.setCancelable( false )//
//				.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick( DialogInterface dialog, int which )
//					{
//						Intent intent = new Intent( BaseActivity.this, MainActivity.class );
//						startActivity( intent );
//						finish();
//					}
//				} ).create();
//		return dialog;
//	}

}

