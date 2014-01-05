package org.mariotaku.utwitterapi.activity;

import org.mariotaku.utwitterapi.R;
import org.mariotaku.utwitterapi.fragment.AddApplicationDialogFragmnt;
import org.mariotaku.utwitterapi.fragment.EditAPIDialogFragment;
import org.mariotaku.utwitterapi.fragment.ProxiedClientsListFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class PluginSettingsActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.set_api: {
				new EditAPIDialogFragment().show(getFragmentManager(), "edit_api");
				return true;
			}
			case R.id.add_app: {
				new AddApplicationDialogFragmnt().show(getFragmentManager(), "add_app");
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final FragmentManager fm = getFragmentManager();
		fm.beginTransaction().replace(android.R.id.content, new ProxiedClientsListFragment()).commit();
	}

}
