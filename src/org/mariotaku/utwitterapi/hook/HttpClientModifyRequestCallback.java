package org.mariotaku.utwitterapi.hook;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.RequestWrapper;
import org.mariotaku.utwitterapi.Constants;
import org.mariotaku.utwitterapi.util.Utils;
import org.mariotaku.utwitterapi.util.XposedPluginUtils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public final class HttpClientModifyRequestCallback extends XC_MethodReplacement implements Constants {

	@Override
	protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
		final Object[] args = param.args;
		processArguments(args);
		try {
			return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, args);
		} catch (final InvocationTargetException e) {
			final Throwable cause = e.getCause();
			if (cause != null) throw cause;
			throw e;
		}
	}

	private static int indexOfHttpHost(final Object[] args) {
		if (args == null) return -1;
		for (int i = 0, j = args.length; i < j; i++) {
			if (args[i] instanceof HttpHost) return i;
		}
		return -1;
	}

	private static int indexOfHttpRequest(final Object[] args) {
		if (args == null) return -1;
		for (int i = 0, j = args.length; i < j; i++) {
			if (args[i] instanceof HttpRequest) return i;
		}
		return -1;
	}

	private static int indexOfHttpUriRequest(final Object[] args) {
		if (args == null) return -1;
		for (int i = 0, j = args.length; i < j; i++) {
			if (args[i] instanceof HttpUriRequest) return i;
		}
		return -1;
	}

	private static void modifyAbstractHttpMessage(final HttpRequestBase req) throws URISyntaxException {
		final URI origURI = req.getURI();
		final String origUriString = origURI.toString();
		if (!Utils.isTwitterAPI(origUriString) || !XposedPluginUtils.isUsingCustomAPI()) return;
		final String replacedUriString = XposedPluginUtils.replaceAPIUri(origUriString);
		final String hostHeaderValue = XposedPluginUtils.getCustomAPIHostHeader(origUriString);
		req.setURI(new URI(replacedUriString));
		if (!TextUtils.isEmpty(hostHeaderValue)) {
			req.setHeader("Host", hostHeaderValue);
		}
	}

	private static void processArguments(final Object[] args) throws URISyntaxException, ProtocolException {
		if (args == null) return;
		final int indexOfHttpUriRequest = indexOfHttpUriRequest(args);
		final int indexOfHttpHost = indexOfHttpHost(args);
		final int indexOfHttpRequest = indexOfHttpRequest(args);
		if (indexOfHttpUriRequest != -1) {
			final HttpUriRequest req = (HttpUriRequest) args[indexOfHttpUriRequest];
			final URI uri = req.getURI();
			if (HOST_TWITTER_API.equals(uri.getHost())) {
				if (Utils.isDebugBuild()) {
					Log.d(LOGTAG, String.format("Modifying HTTP request for %s", uri));
				}
				if (req instanceof HttpRequestBase) {
					modifyAbstractHttpMessage((HttpRequestBase) req);
				} else {
					final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME,
							SHARED_PREFERENCE_NAME_PREFERENCES);
					final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
					final String ipAddress = prefs.getString(KEY_IP_ADDRESS, null);
					final Uri.Builder uriBuilder = Uri.parse(uri.toString()).buildUpon();
					if (!TextUtils.isEmpty(ipAddress)) {
						uriBuilder.authority(ipAddress);
					} else if (!TextUtils.isEmpty(apiAddress)) {
						uriBuilder.authority(apiAddress);
					}
					final String uriString = uriBuilder.build().toString();
					final RequestWrapper wrapper = new RequestWrapper(req);
					wrapper.setMethod(req.getMethod());
					wrapper.setHeaders(req.getAllHeaders());
					wrapper.setParams(req.getParams());
					wrapper.setProtocolVersion(req.getProtocolVersion());
					wrapper.setURI(new URI(uriString));
					if (!TextUtils.isEmpty(apiAddress)) {
						wrapper.setHeader("Host", apiAddress);
					}
					args[indexOfHttpUriRequest] = wrapper;
				}
			}
		} else if (indexOfHttpHost != -1 && indexOfHttpRequest != -1) {
			final HttpHost host = (HttpHost) args[indexOfHttpHost];
			final HttpRequest req = (HttpRequest) args[indexOfHttpRequest];
			if (HOST_TWITTER_API.equals(host.getHostName())) {
				if (Utils.isDebugBuild()) {
					Log.d(LOGTAG, String.format("Modifying HTTP request for %s %s", host, req));
				}
				final XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME,
						SHARED_PREFERENCE_NAME_PREFERENCES);
				final String apiAddress = prefs.getString(KEY_API_ADDRESS, null);
				final String ipAddress = prefs.getString(KEY_IP_ADDRESS, null);
				if (!TextUtils.isEmpty(ipAddress)) {
					args[indexOfHttpHost] = new HttpHost(ipAddress, host.getPort(), host.getSchemeName());
				} else if (!TextUtils.isEmpty(apiAddress)) {
					args[indexOfHttpHost] = new HttpHost(apiAddress, host.getPort(), host.getSchemeName());
				}
				req.setHeader("Host", apiAddress);
			}
		}
	}

}