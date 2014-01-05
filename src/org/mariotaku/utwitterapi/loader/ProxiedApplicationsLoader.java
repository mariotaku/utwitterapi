package org.mariotaku.utwitterapi.loader;

import org.mariotaku.utwitterapi.Constants;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class ProxiedApplicationsLoader extends AbsApplicationsLoader implements Constants {

	private final SharedPreferences mClientPreferences;

	public ProxiedApplicationsLoader(final Context context) {
		this(context, context.getPackageManager());
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	public ProxiedApplicationsLoader(final Context context, final PackageManager pm) {
		super(context, pm);
		mClientPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME_CLIENTS, Context.MODE_WORLD_READABLE);
	}

	@Override
	protected boolean isFiltered(final ApplicationInfo info) {
		return !mClientPreferences.getBoolean(info.packageName, false);
	}

}