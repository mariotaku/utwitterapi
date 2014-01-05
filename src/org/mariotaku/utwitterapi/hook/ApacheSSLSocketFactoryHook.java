package org.mariotaku.utwitterapi.hook;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.mariotaku.utwitterapi.util.TrustAllApacheSSLSocketFactory;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class ApacheSSLSocketFactoryHook extends XC_MethodReplacement {

	private final SSLSocketFactory socketFactory = TrustAllApacheSSLSocketFactory.getSocketFactory();

	@Override
	protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
		return XposedBridge.invokeOriginalMethod(param.method, socketFactory, param.args);
	}

}
