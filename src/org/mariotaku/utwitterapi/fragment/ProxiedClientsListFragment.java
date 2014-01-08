package org.mariotaku.utwitterapi.fragment;

import java.util.List;

import org.mariotaku.utwitterapi.Constants;
import org.mariotaku.utwitterapi.R;
import org.mariotaku.utwitterapi.adapter.ApplicationsListAdapter;
import org.mariotaku.utwitterapi.loader.ProxiedApplicationsLoader;

import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

public class ProxiedClientsListFragment extends ListFragment implements Constants,
		LoaderCallbacks<List<ApplicationInfo>>, OnSharedPreferenceChangeListener, MultiChoiceModeListener {

	private ApplicationsListAdapter mAdapter;
	private SharedPreferences mClientPreferences;

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.delete: {
				final SharedPreferences.Editor editor = mClientPreferences.edit();
				final SparseBooleanArray checked = getListView().getCheckedItemPositions();
				for (int i = 0, j = checked.size(); i < j; i++) {
					if (checked.valueAt(i)) {
						editor.remove(mAdapter.getItem(checked.keyAt(i)).packageName);
					}
				}
				editor.apply();
				break;
			}
		}
		mode.finish();
		return true;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Context context = getActivity();
		mClientPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME_CLIENTS, Context.MODE_WORLD_READABLE);
		mClientPreferences.registerOnSharedPreferenceChangeListener(this);
		mAdapter = new ApplicationsListAdapter(context, context.getPackageManager());
		setListAdapter(mAdapter);
		setListShownNoAnimation(false);
		final ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		mode.setTitle(R.string.select_apps);
		new MenuInflater(getActivity()).inflate(R.menu.action_multiselect, menu);
		return true;
	}

	@Override
	public Loader<List<ApplicationInfo>> onCreateLoader(final int id, final Bundle args) {
		return new ProxiedApplicationsLoader(getActivity());
	}

	@Override
	public void onDestroyActionMode(final ActionMode mode) {

	}

	@Override
	public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
			final boolean checked) {

	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final PackageManager pm = getActivity().getPackageManager();
		final ApplicationInfo info = mAdapter.getItem(position);
		final Intent intent = pm.getLaunchIntentForPackage(info.packageName);
		if (intent != null) {
			startActivity(intent);
		}
	}

	@Override
	public void onLoaderReset(final Loader<List<ApplicationInfo>> loader) {
		mAdapter.setData(null);
	}

	@Override
	public void onLoadFinished(final Loader<List<ApplicationInfo>> loader, final List<ApplicationInfo> data) {
		mAdapter.setData(data);
		setListShown(true);
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (getActivity() == null || isDetached()) return;
		getLoaderManager().restartLoader(0, null, this);
	}

}
