package org.mariotaku.utwitterapi.hook;

import org.mariotaku.utwitterapi.Constants;
import org.mariotaku.utwitterapi.util.Utils;
import org.mariotaku.utwitterapi.util.XposedPluginUtils;

import android.net.Uri;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class Twitter4JForceHttpsCallback extends XC_MethodReplacement implements Constants {

	@Override
	protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
		final Object result = XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
		if (result instanceof String) {
			final String resultString = (String) result;
			if (Utils.isTwitterAPI(resultString) && XposedPluginUtils.isUsingCustomAPI()) {
				final Uri.Builder builder = Uri.parse(resultString).buildUpon();
				builder.scheme(XposedPluginUtils.isCustomAPIHttps() ? "https" : "http");
				return builder.build().toString();
			}
		}
		return result;
	}

}
