package org.mariotaku.utwitterapi.fragment;

import java.util.List;

import org.mariotaku.utwitterapi.Constants;
import org.mariotaku.utwitterapi.R;
import org.mariotaku.utwitterapi.adapter.ApplicationsListAdapter;
import org.mariotaku.utwitterapi.loader.ApplicationsLoader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

public class AddApplicationDialogFragmnt extends DialogFragment implements Constants, OnClickListener,
		LoaderCallbacks<List<ApplicationInfo>> {

	private ApplicationsListAdapter mAdapter;

	@SuppressLint("WorldReadableFiles")
	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final Context context = getActivity();
		@SuppressWarnings("deprecation")
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME_CLIENTS,
				Context.MODE_WORLD_READABLE);
		final SharedPreferences.Editor editor = prefs.edit();
		final ApplicationInfo app = mAdapter.getItem(which);
		editor.putBoolean(app.packageName, true);
		editor.apply();
		dismiss();
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Context context = getActivity();
		mAdapter = new ApplicationsListAdapter(context);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.select_app);
		builder.setSingleChoiceItems(mAdapter, -1, this);
		getLoaderManager().initLoader(0, null, this);
		return builder.create();
	}

	@Override
	public Loader<List<ApplicationInfo>> onCreateLoader(final int id, final Bundle args) {
		return new ApplicationsLoader(getActivity());
	}

	@Override
	public void onLoaderReset(final Loader<List<ApplicationInfo>> loader) {
		mAdapter.setData(null);
	}

	@Override
	public void onLoadFinished(final Loader<List<ApplicationInfo>> loader, final List<ApplicationInfo> data) {
		mAdapter.setData(data);
	}

}
