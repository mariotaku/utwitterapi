package org.mariotaku.utwitterapi.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class ApplicationsLoader extends AbsApplicationsLoader {

	public ApplicationsLoader(final Context context) {
		super(context);
	}

	public ApplicationsLoader(final Context context, final PackageManager pm) {
		super(context, pm);
	}

	@Override
	protected boolean isFiltered(final ApplicationInfo info) {
		return false;
	}

}