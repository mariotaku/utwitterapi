package org.mariotaku.utwitterapi.hook;

import org.mariotaku.utwitterapi.Constants;
import org.mariotaku.utwitterapi.util.Utils;
import org.mariotaku.utwitterapi.util.XposedPluginUtils;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

public class Twitter4JFixURLCallback extends XC_MethodReplacement implements Constants {

	@Override
	protected Object replaceHookedMethod(final MethodHookParam param) throws Throwable {
		final Object[] args = param.args;
		if (args != null && args.length == 2 && args[0] instanceof Boolean && args[1] instanceof String) {
			final String url = (String) args[1];
			if (Utils.isTwitterAPI(url) && XposedPluginUtils.isUsingCustomAPI()) {
				args[0] = XposedPluginUtils.isCustomAPIHttps();
			}
		}
		return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, args);
	}

}
