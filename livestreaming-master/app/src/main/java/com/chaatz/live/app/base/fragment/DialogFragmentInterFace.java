package com.chaatz.live.app.base.fragment;

import android.view.View;

public class DialogFragmentInterFace
{
	public interface OnDismissListener
	{
		void onDismiss();
	}

	public interface OnButtonClickListener
	{
		void onButtonClick( View view );
	}

	public interface onInputCompleteListener
	{
		void onInputComplete( String input );
	}

}
