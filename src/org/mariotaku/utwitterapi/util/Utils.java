package org.mariotaku.utwitterapi.util;

import java.util.regex.Pattern;

import org.mariotaku.utwitterapi.BuildConfig;
import org.mariotaku.utwitterapi.Constants;

import android.net.Uri;
import android.text.TextUtils;
import de.robv.android.xposed.XSharedPreferences;

public class Utils implements Constants {

	public static boolean contains(final Object[] array, final Object... values) {
		if (array == null || values == null) return false;
		for (final Object item : array) {
			for (final Object value : values) {
				if (item == null || value == null) {
					if (item == value) return true;
					continue;
				}
				if (item.equals(value)) return true;
			}
		}
		return false;
	}

	public static String getCustomAPIHostHeader(final String origUriString) {
		if (!isTwitterAPI(origUriString) || !isUsingCustomAPI()) return null;
		final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME, SHARED_PREFERENCE_NAME_PREFERENCES);
		final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
		final Uri apiUri = Uri.parse(apiAddress);
		final int port = apiUri.getPort();
		final String host = apiUri.getHost();
		if (port != -1) return String.format("%s:%d", host, port);
		return host;
	}

	public static boolean isCustomAPIHttps() {
		final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME, SHARED_PREFERENCE_NAME_PREFERENCES);
		final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
		if (TextUtils.isEmpty(apiAddress)) return false;
		final Uri apiUri = Uri.parse(apiAddress);
		return "https".equals(apiUri.getScheme());
	}

	public static boolean isDebugBuild() {
		return BuildConfig.DEBUG;
	}

	public static boolean isTwitterAPI(final String origUriString) {
		final Uri uri = Uri.parse(origUriString);
		return HOST_TWITTER_API.equals(uri.getHost());
	}

	public static boolean isUsingCustomAPI() {
		final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME, SHARED_PREFERENCE_NAME_PREFERENCES);
		final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
		if (TextUtils.isEmpty(apiAddress)) return false;
		final Uri apiUri = Uri.parse(apiAddress);
		return apiUri.isAbsolute() && !HOST_TWITTER_API.equals(apiUri.getHost());
	}

	public static String replaceAPIUri(final String origUriString) {
		final Uri uri = Uri.parse(origUriString);
		if (!HOST_TWITTER_API.equals(uri.getHost())) return origUriString;
		final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME, SHARED_PREFERENCE_NAME_PREFERENCES);
		final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
		final String ipAddress = prefs.getString(KEY_IP_ADDRESS, null);
		if (TextUtils.isEmpty(apiAddress)) return origUriString;
		final Uri apiUri = Uri.parse(apiAddress);
		if (!apiUri.isHierarchical()) return origUriString;
		final String apiHost = apiUri.getHost(), apiAuthority = apiUri.getAuthority();
		final Uri.Builder builder = uri.buildUpon();
		builder.scheme(apiUri.getScheme());
		if (TextUtils.isEmpty(ipAddress)) {
			builder.authority(apiAuthority);
		} else {
			builder.authority(apiAuthority.replaceFirst(Pattern.quote(apiHost), ipAddress));
		}
		builder.path(mergePath(apiUri.getPath(), uri.getPath()));
		return builder.toString();
	}

	private static String mergePath(final String apiPath, final String requestPath) {
		if (apiPath == null || requestPath == null) throw new NullPointerException();
		final String replacedApiPath = apiPath.endsWith("/") ? apiPath.substring(0, apiPath.length() - 1) : apiPath;
		final String replacedRequestPath = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
		return replacedApiPath + "/" + replacedRequestPath;
	}
}
