package org.mariotaku.utwitterapi;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.invokeOriginalMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.mariotaku.utwitterapi.hook.ApacheSSLSocketFactoryHook;
import org.mariotaku.utwitterapi.hook.HttpClientModifyRequestCallback;
import org.mariotaku.utwitterapi.hook.HttpsURLConnectionSetHostnameVerifierCallback;
import org.mariotaku.utwitterapi.hook.HttpsURLConnectionSetSSLSocketFactoryCallback;
import org.mariotaku.utwitterapi.hook.OkHttpClientModifyRequestCallback;
import org.mariotaku.utwitterapi.hook.SchemeGetSocketFactoryCallback;
import org.mariotaku.utwitterapi.hook.Twitter4JFixURLCallback;
import org.mariotaku.utwitterapi.hook.Twitter4JForceHttpsCallback;
import org.mariotaku.utwitterapi.hook.URLConnectionModifyRequestCallback;
import org.mariotaku.utwitterapi.util.AllowAllHostnameVerifierImpl;
import org.mariotaku.utwitterapi.util.TrustAllSSLSocketFactory;
import org.mariotaku.utwitterapi.util.Utils;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class UTwitterAPIPlugin implements Constants, IXposedHookLoadPackage, IXposedHookZygoteInit {

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		final XSharedPreferences clientsPreferences = new XSharedPreferences(PACKAGE_NAME,
				SHARED_PREFERENCE_NAME_CLIENTS);
		final String pname = lpparam.packageName;
		if (pname == null || !clientsPreferences.getBoolean(pname, false)) return;
		Log.d(LOGTAG, String.format("Loading package %s with Twitter API support", pname));
		hookLoadClass(lpparam);
		hookURL();
		hookScheme(lpparam);
		changeT4JFixURL(lpparam);
		changeT4JAPIAddress(lpparam);
		// twitterHack(lpparam);
	}

	@Override
	public void initZygote(final StartupParam startupParam) throws Throwable {
		// final Method loadClassMethod =
		// XposedHelpers.findMethodBestMatch(ClassLoader.getSystemClassLoader().getClass(),
		// "loadClass", String.class);
		// hookMethod(loadClassMethod, new
		// HookLoadClassCallback());
	}

	private void changeT4JAPIAddress(final LoadPackageParam lpparam) {
		final Class<?> cbClass;
		try {
			cbClass = findClass("twitter4j.conf.ConfigurationBase", lpparam.classLoader);
		} catch (final Throwable t) {
			return;
		}
		final Twitter4JForceHttpsCallback callback = new Twitter4JForceHttpsCallback();
		hookAllMethods(cbClass, "getRestBaseURL", callback);
		hookAllMethods(cbClass, "getOAuthRequestTokenURL", callback);
		hookAllMethods(cbClass, "getOAuthAuthorizationURL", callback);
		hookAllMethods(cbClass, "getOAuthAccessTokenURL", callback);
		hookAllMethods(cbClass, "getOAuthAuthenticationURL", callback);
		hookAllMethods(cbClass, "getOAuth2TokenURL", callback);
		hookAllMethods(cbClass, "getOAuth2InvalidateTokenURL", callback);
	}

	private void changeT4JFixURL(final LoadPackageParam lpparam) {
		final Class<?> cbClass;
		try {
			cbClass = findClass("twitter4j.conf.ConfigurationBase", lpparam.classLoader);
		} catch (final Throwable t) {
			return;
		}
		hookAllMethods(cbClass, "fixURL", new Twitter4JFixURLCallback());
	}

	private void hookLoadClass(final LoadPackageParam lpparam) {
		final Class<? extends ClassLoader> loaderClass = lpparam.classLoader.getClass();
		final Method loadClassMethod = findMethodBestMatch(loaderClass, "loadClass", String.class);
		hookMethod(loadClassMethod, new HookLoadClassCallback());
	}

	private void hookScheme(final LoadPackageParam lpparam) {
		hookAllMethods(Scheme.class, "getSocketFactory", new SchemeGetSocketFactoryCallback());
	}

	private static void hookApacheSSLSocketFactory(final Class<?> cls) {
		if (cls == null || !org.apache.http.conn.ssl.SSLSocketFactory.class.isAssignableFrom(cls)) return;
		hookAllMethods(cls, "createSocket", new ApacheSSLSocketFactoryHook());
	}

	private static void hookHostnameVerifier(final Class<?> cls) {
		if (cls == null || !HostnameVerifier.class.isAssignableFrom(cls)) return;
		final XC_MethodReplacement verifyReplacement = XC_MethodReplacement.returnConstant(true);
		hookAllMethods(cls, "verify", verifyReplacement);
	}

	private static void hookHttpClient(final Class<?> cls) {
		if (cls == null || !HttpClient.class.isAssignableFrom(cls)) return;
		final HttpClientModifyRequestCallback requestCallback = new HttpClientModifyRequestCallback();
		hookAllMethods(cls, "execute", requestCallback);
	}

	private static void hookHttpsURLConnection(final Class<?> cls) {
		if (cls == null || !HttpsURLConnection.class.isAssignableFrom(cls)) return;
		hookAllMethods(cls, "setSSLSocketFactory", new HttpsURLConnectionSetSSLSocketFactoryCallback());
		hookAllMethods(cls, "setHostnameVerifier", new HttpsURLConnectionSetHostnameVerifierCallback());
		hookAllMethods(cls, "getSSLSocketFactory",
				XC_MethodReplacement.returnConstant(TrustAllSSLSocketFactory.getSocketFactory()));
		hookAllMethods(cls, "getHostnameVerifier",
				XC_MethodReplacement.returnConstant(new AllowAllHostnameVerifierImpl()));
		hookAllMethods(cls, "getDefaultSSLSocketFactory",
				XC_MethodReplacement.returnConstant(TrustAllSSLSocketFactory.getSocketFactory()));
		hookAllMethods(cls, "getDefaultHostnameVerifier",
				XC_MethodReplacement.returnConstant(new AllowAllHostnameVerifierImpl()));
	}

	private static void hookSSLSocketFactory(final Class<?> cls) {
		if (cls == null || !javax.net.ssl.SSLSocketFactory.class.isAssignableFrom(cls)) return;
	}

	private static void hookURL() {
		hookAllMethods(URL.class, "openConnection", new URLConnectionModifyRequestCallback());
	}

	private static void hookURLStreamHandlerFactory(final Class<?> cls) {
		if (cls == null || !URLStreamHandlerFactory.class.isAssignableFrom(cls)) return;
		for (final Method method : cls.getMethods()) {
			// method.setAccessible(true);
			final Class<?> returnType = method.getReturnType();
			if (HostnameVerifier.class.isAssignableFrom(returnType)) {
				hookMethod(method, XC_MethodReplacement.returnConstant(new AllowAllHostnameVerifierImpl()));
			} else if (javax.net.ssl.SSLSocketFactory.class.isAssignableFrom(returnType)) {
				hookMethod(method, XC_MethodReplacement.returnConstant(TrustAllSSLSocketFactory.getSocketFactory()));
			} else if (URLStreamHandler.class.isAssignableFrom(returnType)) {
				hookMethod(method, new CreateURLStreamHandlerHook());
			} else if (HttpURLConnection.class.isAssignableFrom(returnType)) {
				hookMethod(method, new OkHttpClientModifyRequestCallback());
			}
		}
	}

	private static void hookX509TrustManager(final Class<?> cls) {
		if (cls == null || !X509TrustManager.class.isAssignableFrom(cls)) return;
		hookAllMethods(cls, "checkClientTrusted", XC_MethodReplacement.DO_NOTHING);
		hookAllMethods(cls, "checkServerTrusted", XC_MethodReplacement.DO_NOTHING);
	}

	private static class CreateURLStreamHandlerHook extends XC_MethodReplacement {

		@Override
		protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
			final Object result = invokeOriginalMethod(param.method, param.thisObject, param.args);
			Log.d(LOGTAG, String.format("Returned URLStreamHandler %s", result));
			return result;
		}

	}

	private static class HookLoadClassCallback extends XC_MethodHook {

		@Override
		protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
			super.afterHookedMethod(param);
			final Object result = param.getResult();
			if (!(result instanceof Class)) return;
			final Class<?> cls = (Class<?>) result;
			if (cls.isInterface()) return;
			if (HttpClient.class.isAssignableFrom(cls)) {
				if (Utils.isDebugBuild()) {
					Log.d(LOGTAG, String.format("Found HttpClient implemention %s", result));
				}
				hookHttpClient(cls);
			} else if (HostnameVerifier.class.isAssignableFrom(cls)) {
				if (Utils.isDebugBuild()) {
					Log.d(LOGTAG, String.format("Found HostnameVerifier implemention %s", result));
				}
				hookHostnameVerifier(cls);
			} else if (HttpURLConnection.class.isAssignableFrom(cls)) {
				if (HttpsURLConnection.class.isAssignableFrom(cls)) {
					if (Utils.isDebugBuild()) {
						Log.d(LOGTAG, String.format("Found HttpsURLConnection implemention %s", result));
					}
					hookHttpsURLConnection(cls);
				} else {
					if (Utils.isDebugBuild()) {
						Log.d(LOGTAG, String.format("Found HttpURLConnection implemention %s", result));
					}
				}
			} else if (TrustManager.class.isAssignableFrom(cls)) {
				if (X509TrustManager.class.isAssignableFrom(cls)) {
					if (Utils.isDebugBuild()) {
						Log.d(LOGTAG, String.format("Found X509TrustManager implemention %s", result));
					}
					hookX509TrustManager(cls);
				} else {
					if (Utils.isDebugBuild()) {
						Log.d(LOGTAG, String.format("Found TrustManager implemention %s", result));
					}
				}
			} else if (org.apache.http.conn.ssl.SSLSocketFactory.class.isAssignableFrom(cls)) {
				if (Utils.isDebugBuild()) {
					Log.d(LOGTAG, String.format("Found Apache SSLSocketFactory implemention %s", result));
				}
				hookApacheSSLSocketFactory(cls);
			} else if (javax.net.ssl.SSLSocketFactory.class.isAssignableFrom(cls)) {
				if (Utils.isDebugBuild()) {
					Log.d(LOGTAG, String.format("Found SSLSocketFactory implemention %s", result));
				}
				hookSSLSocketFactory(cls);
			} else if (URLStreamHandlerFactory.class.isAssignableFrom(cls)) {
				if (Utils.isDebugBuild()) {
					Log.d(LOGTAG, String.format("Found URLStreamHandlerFactory implemention %s", result));
				}
				hookURLStreamHandlerFactory(cls);
			}
		}

	}

}
