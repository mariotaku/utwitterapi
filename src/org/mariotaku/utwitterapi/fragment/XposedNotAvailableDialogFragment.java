package org.mariotaku.utwitterapi.fragment;

import org.mariotaku.utwitterapi.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class XposedNotAvailableDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.xposed_not_available);
		builder.setMessage(R.string.xposed_not_available_message);
		return builder.create();
	}

}
