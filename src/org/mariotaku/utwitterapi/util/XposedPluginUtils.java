package org.mariotaku.utwitterapi.util;

import org.mariotaku.utwitterapi.Constants;

import de.robv.android.xposed.XSharedPreferences;

public class XposedPluginUtils implements Constants {

	public static String getCustomAPIHostHeader(final String origUriString) {
		final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME, SHARED_PREFERENCE_NAME_PREFERENCES);
		return Utils.getCustomAPIHostHeader(prefs, origUriString);
	}

	public static boolean isCustomAPIHttps() {
		final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME, SHARED_PREFERENCE_NAME_PREFERENCES);
		return Utils.isCustomAPIHttps(prefs);
	}

	public static boolean isUsingCustomAPI() {
		final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME, SHARED_PREFERENCE_NAME_PREFERENCES);
		return Utils.isUsingCustomAPI(prefs);
	}

	public static String replaceAPIUri(final String origUriString) {
		final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME, SHARED_PREFERENCE_NAME_PREFERENCES);
		return Utils.replaceAPIUri(prefs, origUriString);
	}

}
