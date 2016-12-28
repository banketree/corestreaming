package com.chaatz.live.app.base.fragment;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class BaseDialogFragment extends DialogFragment
{
	public static final String TITLE = "title";
	public static final String CANCELABLE = "cancelable";

	protected static boolean mIsShowing = false;
	protected boolean mCancelable = false;
	protected String mTitle;
	
	protected DialogFragmentInterFace.OnDismissListener mOnDismissListener;

	@Override
	public void show( FragmentManager manager, String tag )
	{
		super.show( manager, tag );
		mIsShowing = true;
	}

	@Override
	public void onDismiss( DialogInterface dialog )
	{
		mIsShowing = false;
		if ( null != mOnDismissListener ) {
			mOnDismissListener.onDismiss();
		}
		super.onDismiss( dialog );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mOnDismissListener = null;
	}

	public void setOnDismissListener( DialogFragmentInterFace.OnDismissListener listener )
	{
		mOnDismissListener = listener;
	}

	public boolean isShowing()
	{
		return mIsShowing;
	}

}
