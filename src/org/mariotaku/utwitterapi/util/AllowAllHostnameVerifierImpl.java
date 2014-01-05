package org.mariotaku.utwitterapi.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public final class AllowAllHostnameVerifierImpl implements HostnameVerifier {

	@Override
	public boolean verify(final String hostname, final SSLSession session) {
		return true;
	}

}