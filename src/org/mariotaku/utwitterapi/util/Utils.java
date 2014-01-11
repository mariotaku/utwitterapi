package org.mariotaku.utwitterapi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.mariotaku.utwitterapi.BuildConfig;
import org.mariotaku.utwitterapi.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;

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

	public static Intent createExcludingChooserIntent(final Context context, final String packageNameToExclude,
			final Intent intent) {
		final ArrayList<Intent> intents = new ArrayList<Intent>();
		final List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
		if (resInfo.isEmpty()) return null;
		for (final ResolveInfo info : resInfo) {
			final Intent viewIntent = new Intent(intent);
			if (!info.activityInfo.packageName.equalsIgnoreCase(packageNameToExclude)) {
				viewIntent.setPackage(info.activityInfo.packageName);
				intents.add(viewIntent);
			}
		}
		final Intent chooserIntent = Intent.createChooser(intents.remove(intents.size() - 1), null);
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
		return chooserIntent;
	}

	public static String getCustomAPIHostHeader(final SharedPreferences prefs, final String origUriString) {
		if (!isTwitterAPI(origUriString) || !isUsingCustomAPI(prefs)) return null;
		final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
		final Uri apiUri = Uri.parse(apiAddress), origUri = Uri.parse(origUriString);
		final int port = apiUri.getPort();
		final String host = apiUri.getHost(), origHost = origUri.getHost();
		if (port != -1) return String.format("%s:%d", origHost.replace(HOST_TWITTER, host), port);
		return origHost.replace(HOST_TWITTER, host);
	}

	public static boolean hasXposedFramework(final Context context) {
		final PackageManager pm = context.getPackageManager();
		try {
			return pm.getApplicationInfo("de.robv.android.xposed.installer", 0) != null;
		} catch (final PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	public static boolean isCustomAPIHttps(final SharedPreferences prefs) {
		final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
		if (TextUtils.isEmpty(apiAddress)) return false;
		final Uri apiUri = Uri.parse(apiAddress);
		return "https".equals(apiUri.getScheme());
	}

	public static boolean isDebugBuild() {
		return BuildConfig.DEBUG;
	}

	public static boolean isTwitterAPI(final String origUriString) {
		return isTwitterAPI(Uri.parse(origUriString));
	}

	public static boolean isTwitterAPI(final Uri uri) {
		final String host = uri.getHost();
		return isTwitterAPIHost(host);
	}

	public static boolean isTwitterAPIHost(final String host) {
		return host.endsWith(HOST_TWITTER);
	}

	public static boolean isUsingCustomAPI(final SharedPreferences prefs) {
		final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
		if (TextUtils.isEmpty(apiAddress)) return false;
		final Uri apiUri = Uri.parse(apiAddress);
		return apiUri.isAbsolute() && !isTwitterAPIHost(apiUri.getHost());
	}

	public static String replaceAPIUri(final SharedPreferences prefs, final String origUriString) {
		final Uri uri = Uri.parse(origUriString);
		final String host = uri.getHost();
		if (!isTwitterAPIHost(host)) return origUriString;
		final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
		final String ipAddress = prefs.getString(KEY_IP_ADDRESS, null);
		if (TextUtils.isEmpty(apiAddress)) return origUriString;
		final Uri apiUri = Uri.parse(apiAddress);
		if (!apiUri.isHierarchical()) return origUriString;
		final String apiHost = apiUri.getHost(), apiAuthority = apiUri.getAuthority();
		final Uri.Builder builder = uri.buildUpon();
		builder.scheme(apiUri.getScheme());
		if (TextUtils.isEmpty(ipAddress)) {
			final String origAuthority = uri.getAuthority();
			builder.authority(origAuthority.replace(HOST_TWITTER, apiAuthority));
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
