package org.mariotaku.utwitterapi.hook;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.mariotaku.utwitterapi.Constants;
import org.mariotaku.utwitterapi.util.AllowAllHostnameVerifierImpl;
import org.mariotaku.utwitterapi.util.TrustAllSSLSocketFactory;
import org.mariotaku.utwitterapi.util.Utils;

import android.text.TextUtils;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class URLConnectionModifyRequestCallback extends XC_MethodReplacement implements Constants {

	@Override
	protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
		final URL url = (URL) param.thisObject;
		final String origUriString = url.toString();
		final String host = url.getHost();
		if (HOST_TWITTER_API.equals(host) && Utils.isUsingCustomAPI()) {
			final String replacedUriString = Utils.replaceAPIUri(origUriString);
			final String hostHeaderValue = Utils.getCustomAPIHostHeader(origUriString);
			final Object result = XposedBridge.invokeOriginalMethod(param.method, new URL(replacedUriString),
					param.args);
			if (result instanceof URLConnection && !TextUtils.isEmpty(hostHeaderValue)) {
				final URLConnection conn = (URLConnection) result;
				conn.setRequestProperty("Host", hostHeaderValue);
			}
			if (result instanceof HttpsURLConnection) {
				final HttpsURLConnection conn = (HttpsURLConnection) result;
				conn.setHostnameVerifier(new AllowAllHostnameVerifierImpl());
				final SSLSocketFactory factory = TrustAllSSLSocketFactory.getSocketFactory();
				if (factory != null) {
					conn.setSSLSocketFactory(factory);
				}
			}
			return result;
		}
		try {
			return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
		} catch (final InvocationTargetException e) {
			final Throwable cause = e.getCause();
			if (cause != null) throw cause;
			throw e;
		}
	}

}
