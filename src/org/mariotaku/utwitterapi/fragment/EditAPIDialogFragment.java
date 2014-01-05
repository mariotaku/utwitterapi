package org.mariotaku.utwitterapi.fragment;

import org.mariotaku.utwitterapi.Constants;
import org.mariotaku.utwitterapi.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditAPIDialogFragment extends DialogFragment implements Constants, OnShowListener, TextWatcher,
		OnClickListener {

	private EditText mEditAPIAddress, mEditIPAddress;

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@SuppressLint("WorldReadableFiles")
	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		if (mEditAPIAddress == null || mEditIPAddress == null || TextUtils.isEmpty(mEditAPIAddress.getText())) return;
		final CharSequence apiAddress = mEditAPIAddress.getText();
		final CharSequence ipAddress = mEditIPAddress.getText();
		final Context context = getActivity();
		@SuppressWarnings("deprecation")
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME_PREFERENCES,
				Context.MODE_WORLD_READABLE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(KEY_API_ADDRESS, apiAddress != null ? apiAddress.toString() : null);
		editor.putString(KEY_IP_ADDRESS, ipAddress != null ? ipAddress.toString() : null);
		editor.apply();
	}

	@SuppressLint("WorldReadableFiles")
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Context context = getActivity();
		@SuppressWarnings("deprecation")
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME_PREFERENCES,
				Context.MODE_WORLD_READABLE);
		final View view = LayoutInflater.from(context).inflate(R.layout.edit_api_dialog, null);
		mEditAPIAddress = (EditText) view.findViewById(R.id.edit_api_address);
		mEditIPAddress = (EditText) view.findViewById(R.id.edit_ip_address);
		mEditAPIAddress.addTextChangedListener(this);
		mEditIPAddress.addTextChangedListener(this);
		mEditAPIAddress.setText(prefs.getString(KEY_API_ADDRESS, "https://api.twitter.com/"));
		mEditIPAddress.setText(prefs.getString(KEY_IP_ADDRESS, null));
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.set_api);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(this);
		return dialog;
	}

	@Override
	public void onShow(final DialogInterface dialog) {
		updatePositiveButton();
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		updatePositiveButton();
	}

	private void updatePositiveButton() {
		if (mEditAPIAddress == null || mEditIPAddress == null) return;
		final Dialog dialog = getDialog();
		if (!(dialog instanceof AlertDialog)) return;
		final Button button = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
		button.setEnabled(!TextUtils.isEmpty(mEditAPIAddress.getText()));
	}

}
