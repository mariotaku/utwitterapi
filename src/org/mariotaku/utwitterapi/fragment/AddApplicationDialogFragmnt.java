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
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

public class AddApplicationDialogFragmnt extends DialogFragment implements Constants,
		LoaderCallbacks<List<ApplicationInfo>>, TextWatcher, OnItemClickListener {

	private ApplicationsListAdapter mAdapter;
	private ListView mListView;
	private ProgressBar mProgressBar;
	private EditText mEditSearchQuery;

	@Override
	public void afterTextChanged(final Editable s) {
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Context context = getActivity();
		final View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_app, null);
		mAdapter = new ApplicationsListAdapter(context);
		mListView = (ListView) view.findViewById(android.R.id.list);
		mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);
		mEditSearchQuery = (EditText) view.findViewById(R.id.edit_search_query);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mEditSearchQuery.addTextChangedListener(this);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.select_app);
		builder.setView(view);
		getLoaderManager().initLoader(0, null, this);
		return builder.create();
	}

	@Override
	public Loader<List<ApplicationInfo>> onCreateLoader(final int id, final Bundle args) {
		mListView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
		return new ApplicationsLoader(getActivity(), mEditSearchQuery.getText());
	}

	@SuppressLint("WorldReadableFiles")
	@Override
	public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
		final Context context = getActivity();
		@SuppressWarnings("deprecation")
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME_CLIENTS,
				Context.MODE_WORLD_READABLE);
		final SharedPreferences.Editor editor = prefs.edit();
		final ApplicationInfo app = mAdapter.getItem(position);
		editor.putBoolean(app.packageName, true);
		editor.apply();
		dismiss();
	}

	@Override
	public void onLoaderReset(final Loader<List<ApplicationInfo>> loader) {
		mAdapter.setData(null);
	}

	@Override
	public void onLoadFinished(final Loader<List<ApplicationInfo>> loader, final List<ApplicationInfo> data) {
		mAdapter.setData(data);
		mListView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		getLoaderManager().restartLoader(0, null, this);
	}

}
