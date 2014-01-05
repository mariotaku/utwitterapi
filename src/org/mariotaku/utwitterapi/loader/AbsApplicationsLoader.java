package org.mariotaku.utwitterapi.loader;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

public abstract class AbsApplicationsLoader extends AsyncTaskLoader<List<ApplicationInfo>> {

	private PackageIntentReceiver mPackageObserver;

	private final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
	private final PackageManager mPackageManager;

	public AbsApplicationsLoader(final Context context) {
		this(context, context.getPackageManager());
	}

	public AbsApplicationsLoader(final Context context, final PackageManager pm) {
		super(context);
		mPackageManager = pm;
	}

	@Override
	public List<ApplicationInfo> loadInBackground() {
		final List<ApplicationInfo> result = new ArrayList<ApplicationInfo>();
		for (final ApplicationInfo info : mPackageManager.getInstalledApplications(0)) {
			if (!isFiltered(info)) {
				result.add(info);
			}
		}
		Collections.sort(result, new ApplicationNameComparator(mPackageManager));
		return result;
	}

	protected abstract boolean isFiltered(ApplicationInfo info);

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// Stop monitoring for changes.
		if (mPackageObserver != null) {
			getContext().unregisterReceiver(mPackageObserver);
			mPackageObserver = null;
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {

		// Start watching for changes in the app data.
		if (mPackageObserver == null) {
			mPackageObserver = new PackageIntentReceiver(this);
		}

		// Has something interesting in the configuration changed since we
		// last built the app list?
		final boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

		if (takeContentChanged() || configChange) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	public static final class ApplicationNameComparator implements Comparator<ApplicationInfo> {

		private final PackageManager packageManager;
		private final Collator collator;

		public ApplicationNameComparator(final PackageManager pm) {
			packageManager = pm;
			collator = Collator.getInstance(Locale.getDefault());
		}

		@Override
		public int compare(final ApplicationInfo lhs, final ApplicationInfo rhs) {
			final CharSequence lLabel = lhs.loadLabel(packageManager);
			final CharSequence rLabel = rhs.loadLabel(packageManager);
			return collator.compare(String.valueOf(lLabel), String.valueOf(rLabel));
		}

	}

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {

		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(final Resources res) {
			final int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
			final boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE | ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}

	/**
	 * Helper class to look for interesting changes to the installed apps so
	 * that the loader can be updated.
	 */
	public static class PackageIntentReceiver extends BroadcastReceiver {

		final AbsApplicationsLoader mLoader;

		public PackageIntentReceiver(final AbsApplicationsLoader loader) {
			mLoader = loader;
			final IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);
			// Register for events related to sdcard installation.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				final IntentFilter sdFilter = new IntentFilter();
				sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
				sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
				mLoader.getContext().registerReceiver(this, sdFilter);
			}
		}

		@Override
		public void onReceive(final Context context, final Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

}