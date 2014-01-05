package org.mariotaku.utwitterapi.hook;

import org.mariotaku.utwitterapi.util.AllowAllHostnameVerifierImpl;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class HttpsURLConnectionSetHostnameVerifierCallback extends XC_MethodReplacement {

	@Override
	protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
		if (param.args != null && param.args.length == 1) {
			param.args[0] = new AllowAllHostnameVerifierImpl();
		}
		return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
	}

}
