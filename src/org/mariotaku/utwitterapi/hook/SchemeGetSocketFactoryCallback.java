package org.mariotaku.utwitterapi.hook;

import org.apache.http.conn.scheme.Scheme;
import org.mariotaku.utwitterapi.util.TrustAllApacheSSLSocketFactory;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class SchemeGetSocketFactoryCallback extends XC_MethodReplacement {

	@Override
	protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
		final Scheme scheme = (Scheme) param.thisObject;
		if ("https".equals(scheme.getName()) || scheme.getDefaultPort() == 443)
			return TrustAllApacheSSLSocketFactory.getSocketFactory();
		return XposedBridge.invokeOriginalMethod(param.method, scheme, param.args);
	}

}