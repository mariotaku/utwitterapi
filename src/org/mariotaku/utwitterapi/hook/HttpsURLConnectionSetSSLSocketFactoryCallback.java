package org.mariotaku.utwitterapi.hook;

import org.mariotaku.utwitterapi.util.TrustAllSSLSocketFactory;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class HttpsURLConnectionSetSSLSocketFactoryCallback extends XC_MethodReplacement {

	@Override
	protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
		if (param.args != null && param.args.length == 1) {
			param.args[0] = TrustAllSSLSocketFactory.getSocketFactory();
		}
		return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
	}

}
