package com.chaatz.live.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.view.Window;

import com.chaatz.live.debug.Log;

public class DialogHelper
{

	/**
	 * <p>
	 * This Function call will return an AlertDialog.Builder. if you wants to add some element in dialog you can
	 * AlertDialog.Builder to add elements. eg: if you wants add button
	 * builder.setPositiveButton("text",onclickListener); call builder.show(); to display the dialog.
	 * @param context
	 * @param title (example: R.string.xxx)
	 * @param message (example: R.layout.xxx)
	 * @return builder
	 */

	public static AlertDialog.Builder createAlertDialogOnlyOkButton( Context context, int title, int message )
	{

		return createAlertDialogOnlyOkButton( context, title, message, new OnClickListener()
		{
			public void onClick( DialogInterface dialog, int which )
			{
				dialog.dismiss();
			}
		} );
	}

	/**
	 * <p>
	 * This Function call will return an AlertDialog.Builder. if you wants to add some element in dialog you can
	 * AlertDialog.Builder to add elements. eg: if you wants add button
	 * builder.setPositiveButton("text",onclickListener); call builder.show(); to display the dialog.
	 *
	 * @param title (example: R.string.xxx)
	 * @return builder
	 */

	public static AlertDialog.Builder createAlertDialogOnlyOkButton( Context context, int title, int message, OnClickListener listener, boolean cancaelable )
	{
		Builder builder = createAlertDialogOnlyOkButton( context, title, message, listener );
		builder.setCancelable( cancaelable );
		return builder;
	}

	/**
	 *
	 * @param context
	 * @param title
	 * @param listener
	 * @return
	 */
	public static AlertDialog.Builder createAlertDialogOnlyOkButton( Context context, int title, int message, OnClickListener listener )
	{
		Builder builder = new AlertDialog.Builder( context );
		if ( title != 0 ) {
			builder.setTitle( title );
		}
		builder.setMessage( message );
		builder.setNeutralButton(android.R.string.ok, listener );
		return builder;
	}

	/**
	 * <p>
	 * This Function call will return an AlertDialog.Builder. if you wants to add some element in dialog you can
	 * AlertDialog.Builder to add elements. eg: if you wants add button
	 * builder.setPositiveButton("text",onclickListener); call builder.show(); to display the dialog.
	 *
	 * @return builder
	 */

	public static AlertDialog.Builder createAlertDialogOnlyOkButton( Context context, String title, String message )
	{
		return createAlertDialogOnlyOkButton( context, title, message, new OnClickListener()
		{
			public void onClick( DialogInterface dialog, int which )
			{
				dialog.dismiss();
			}
		} );
	}

	/**
	 *
	 * @param context
	 * @param title
	 * @param listener
	 * @return
	 */
	public static AlertDialog.Builder createAlertDialogOnlyOkButton( Context context, String title, String message, OnClickListener listener )
	{
		Builder builder = new AlertDialog.Builder( context );
		if ( !TextUtils.isEmpty( title ) ) {
			builder.setTitle( title );
		}
		builder.setMessage( message );
		builder.setNeutralButton( android.R.string.ok, listener );
		return builder;
	}

	/**
	 * <p>
	 * This Function call will return a Dialog without title bar. if you wants to add some element in layout you can
	 * call findViewById(R.id.xxx) to get the widget. eg: dialog.findViewById(R.id.textView); if you wants add button
	 * dialog.setPositiveButton("text",onclickListener); At last you need to call dialog.show();
	 *
	 * @return dialog
	 */

	public static Dialog createCustomeAlertDialog( Context context, int customView )
	{

		Dialog dialog = new Dialog( context );
		dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
		dialog.setContentView( customView );

		return dialog;
	}

	/**
	 * <p>
	 * This Function call will return a Dialog with title. if you wants to add some element in layout you can call
	 * findViewById(R.id.xxx) to get the widget. eg: dialog.findViewById(R.id.textView); if you wants add button
	 * dialog.setPositiveButton("text",onclickListener); At last you need to call dialog.show();
	 *
	 * @param  context
	 * @param  title example: R.string.xxx
	 * @param  customView example: R.layout.xxx
	 * @return dialog
	 */

	public static Dialog createCustomeAlertDialog( Context context, String title, int customView )
	{
		Dialog dialog = new Dialog( context );
		if ( null != title ) {
			dialog.setTitle( title );
		}
		dialog.setContentView( customView );
		return dialog;
	}

	/**
	 * <p>
	 * This Function call will return a Dialog with title. if you wants to add some element in layout you can
	 * builder.setXX(). eg: dialog.findViewById(R.id.textView); if you wants add button
	 * dialog.setPositiveButton("text",onclickListener); At last you need to call dialog.show();
	 *
	 * @param  context
	 * @param  title example: R.string.xxx
	 * @param  customView example: R.layout.xxx
	 * @return dialog
	 */

	public static Dialog createCustomeAlertDialog( Context context, int title, int customView )
	{
		Dialog dialog = new Dialog( context );
		if ( 0 != title ) {
			dialog.setTitle( title );
		}
		dialog.setContentView( customView );
		return dialog;
	}

	/**
	 * <p>
	 * This Function call will return a Dialog with title. if you wants to add some element in layout you can
	 * builder.setXX(). eg: if you wants add button builder.setPositiveButton("text",onclickListener); At last call
	 * builder.show(); to display the dialog.
	 *
	 * @param  context
	 * @param  title example: R.string.xxx
	 * @param  message example: R.string.xxx
	 * @param  icon example: R.drwable.xxx
	 * @return
	 */

	public static AlertDialog.Builder createBaseAlertDialog( Context context, int title, int message, int icon )
	{
		Builder builder = new AlertDialog.Builder( context );
		if ( 0 != icon ) {
			builder.setIcon( icon );
		}
		if ( 0 != title ) {
			builder.setTitle( title );
		}
		if ( 0 != message ) {
			builder.setMessage( message );
		}
		return builder;
	}

	/**
	 * <p>
	 * This Function call will return the AlertDialog with title. if you wants to add some element in layout you can
	 * builder.setXX(). eg: if you wants add button builder.setPositiveButton("text",onclickListener); At last call
	 * builder.show(); to display the dialog.
	 *
	 * @param  context
	 * @param  title
	 * @param  message
	 * @param  icon example: R.drwable.xxx
	 * @return
	 */

	public static AlertDialog.Builder createBaseAlertDialog( Context context, String title, String message, int icon )
	{
		Builder builder = new AlertDialog.Builder( context );
		if ( 0 != icon ) {
			builder.setIcon( icon );
		}
		if ( null != title ) {
			builder.setTitle( title );
		}
		if ( null != message ) {
			builder.setMessage( message );
		}
		return builder;
	}

//	public static Dialog createTmeOutDialog( Context context, int btnText, OnClickListener listener )
//	{
//		Dialog dialog = new AlertDialog.Builder( context ) //
//				.setIcon( R.drawable.ic_action_warning ) //
//				.setTitle( R.string.dialog_request_time_out_title ) //
//				.setMessage( R.string.dialog_request_time_out_content ) //
//				.setCancelable( false )//
//				.setPositiveButton( btnText, listener ).create();
//		return dialog;
//	}
//
//	public static Dialog onCreateNoNetWorkDialog( Context context, int btnText, OnClickListener listener )
//	{
//		Dialog dialog = new AlertDialog.Builder( context ) //
//				.setIcon( R.drawable.ic_action_warning ) //
//				.setTitle( R.string.reg_dlg_cannot_connect_title ) //
//				.setMessage( R.string.reg_dlg_no_internet ) //
//				.setPositiveButton( btnText, listener ) //
//				.create();
//		return dialog;
//	}

	public static void tryDismissDialog( Dialog dialog )
	{
		if ( null != dialog ) {
			try {
				dialog.dismiss();
				Log.d( "DialogHelper", "dialog = ", dialog.getClass().getName(), " , dialog memory address = ", dialog );
			}
			catch ( IllegalArgumentException e ) {
				e.printStackTrace();
			}
		}
		else {
			Log.d( "DialogHelper", "dialog is null " );
		}
	}
}
