/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.utwitterapi.util;

import static android.text.TextUtils.isEmpty;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.mariotaku.utwitterapi.Constants;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

public class OAuthPasswordAuthenticator implements Constants {

	private static final String REFRESH_URL_PREFIX = "url=";
	private static final String TWITTER_OAUTH_AUTHORIZATION_URL = "https://api.twitter.com/oauth/authorize";

	private final DefaultHttpClient httpClient;

	private final SharedPreferences sharedPrefs;

	public OAuthPasswordAuthenticator(final SharedPreferences prefs) {
		final SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", TrustAllApacheSSLSocketFactory.getSocketFactory(), 443));
		final HttpParams params = new BasicHttpParams();
		final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, registry);
		httpClient = new DefaultHttpClient(cm, params);
		sharedPrefs = prefs;
	}

	public SignInResult getSignInResult(final String origOAuthOrizationUrlString, final String username,
			final String password) throws IOException {
		if (isEmpty(origOAuthOrizationUrlString) || isEmpty(username) || isEmpty(password))
			throw new IllegalArgumentException();
		try {
			final String oauthOrizationUrlString = Utils.replaceAPIUri(sharedPrefs, origOAuthOrizationUrlString);
			final String hostHeader = Utils.getCustomAPIHostHeader(sharedPrefs, origOAuthOrizationUrlString);
			final Uri authorizationUri = Uri.parse(oauthOrizationUrlString);
			final String oauthToken = authorizationUri.getQueryParameter(QUERY_PARAM_OAUTH_TOKEN);
			if (isEmpty(oauthToken)) throw new InvalidOAuthTokenException();
			final HttpGet getAuthenticityToken = new HttpGet(oauthOrizationUrlString);
			if (hostHeader != null) {
				getAuthenticityToken.setHeader("Host", hostHeader);
			}
			if (Utils.isDebugBuild()) {
				Log.d(LOGTAG, String.format("Read authenticity token from %s", oauthOrizationUrlString));
			}
			final String authenticityToken = httpClient
					.execute(getAuthenticityToken, new GetAuthenticityTokenHandler());
			if (isEmpty(authenticityToken)) throw new AuthenticityTokenException();
			final ArrayList<NameValuePair> getOAuthResultParams = new ArrayList<NameValuePair>();
			getOAuthResultParams.add(new BasicNameValuePair("authenticity_token", authenticityToken));
			getOAuthResultParams.add(new BasicNameValuePair("oauth_token", oauthToken));
			getOAuthResultParams.add(new BasicNameValuePair("session[username_or_email]", username));
			getOAuthResultParams.add(new BasicNameValuePair("session[password]", password));
			final UrlEncodedFormEntity getOAuthResultEntity = new UrlEncodedFormEntity(getOAuthResultParams, HTTP.UTF_8);
			final HttpPost getOAuthAuthorization = new HttpPost(Utils.replaceAPIUri(sharedPrefs,
					TWITTER_OAUTH_AUTHORIZATION_URL));
			getOAuthAuthorization.setEntity(getOAuthResultEntity);
			if (hostHeader != null) {
				getOAuthAuthorization.setHeader("Host", hostHeader);
			}
			return httpClient.execute(getOAuthAuthorization, new GetOAuthAuthorizationHandler());
		} catch (final NullPointerException e) {
			throw new AuthenticationException(e);
		}
	}

	public static String readAuthenticityTokenFromHtml(final Reader in) throws IOException, XmlPullParserException {
		final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
		final XmlPullParser parser = f.newPullParser();
		parser.setFeature(Xml.FEATURE_RELAXED, true);
		parser.setInput(in);
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			final String tag = parser.getName();
			switch (parser.getEventType()) {
				case XmlPullParser.START_TAG: {
					if ("input".equals(tag) && "authenticity_token".equals(parser.getAttributeValue(null, "name")))
						return parser.getAttributeValue(null, "value");
				}
			}
		}
		return null;
	}

	public static String readCallbackUrlFromHtml(final Reader in) throws IOException, XmlPullParserException {
		final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
		final XmlPullParser parser = f.newPullParser();
		parser.setFeature(Xml.FEATURE_RELAXED, true);
		parser.setInput(in);
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			final String tag = parser.getName();
			switch (parser.getEventType()) {
				case XmlPullParser.START_TAG: {
					if ("meta".equals(tag) && "refresh".equals(parser.getAttributeValue(null, "http-equiv"))) {
						final String content = parser.getAttributeValue(null, "content");
						int idx;
						if (!TextUtils.isEmpty(content) && (idx = content.indexOf(REFRESH_URL_PREFIX)) != -1) {
							final String url = content.substring(idx + REFRESH_URL_PREFIX.length());
							if (!TextUtils.isEmpty(url)) return url;
						}
					}
				}
			}
		}
		return null;
	}

	public static String readOAuthPINFromHtml(final Reader in) throws XmlPullParserException, IOException {
		boolean start_div = false, start_code = false;
		final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
		final XmlPullParser parser = f.newPullParser();
		parser.setFeature(Xml.FEATURE_RELAXED, true);
		parser.setInput(in);
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			final String tag = parser.getName();
			final int type = parser.getEventType();
			if (type == XmlPullParser.START_TAG) {
				if ("div".equalsIgnoreCase(tag)) {
					start_div = "oauth_pin".equals(parser.getAttributeValue(null, "id"));
				} else if ("code".equalsIgnoreCase(tag)) {
					if (start_div) {
						start_code = true;
					}
				}
			} else if (type == XmlPullParser.END_TAG) {
				if ("div".equalsIgnoreCase(tag)) {
					start_div = false;
				} else if ("code".equalsIgnoreCase(tag)) {
					start_code = false;
				}
			} else if (type == XmlPullParser.TEXT) {
				final String text = parser.getText();
				if (start_code && !TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text)) return text;
			}
		}
		return null;
	}

	public static class AuthenticationException extends IOException {

		private static final long serialVersionUID = -5629194721838256378L;

		AuthenticationException() {
		}

		AuthenticationException(final Exception cause) {
			super(cause);
		}

		AuthenticationException(final String message) {
			super(message);
		}
	}

	public static final class AuthenticityTokenException extends AuthenticationException {

		private static final long serialVersionUID = -1840298989316218380L;

		AuthenticityTokenException() {
			super("Can't get authenticity token.");
		}
	}

	public static final class InvalidOAuthTokenException extends AuthenticationException {

		private static final long serialVersionUID = -8310692454665711004L;

		InvalidOAuthTokenException() {
			super("Invalid OAuth token.");
		}

	}

	public static final class InvalidRequestException extends AuthenticationException {

		private static final long serialVersionUID = 3211724930239331108L;

	}

	public static final class SignInResult {

		private final String callbackUrl, pinCode;

		private SignInResult(final String callbackUrl, final String pinCode) {
			this.callbackUrl = callbackUrl;
			this.pinCode = pinCode;
		}

		public String getCallbackUrl() {
			return callbackUrl;
		}

		public String getPinCode() {
			return pinCode;
		}

		public boolean isCallbackUrl() {
			return !isEmpty(callbackUrl);
		}

		public boolean isPinCode() {
			return !isEmpty(pinCode);
		}

		public static SignInResult callbackUrl(final String callbackUrl) {
			return new SignInResult(callbackUrl, null);
		}

		public static SignInResult pinCode(final String pinCode) {
			return new SignInResult(null, pinCode);
		}
	}

	public static final class WrongUserPassException extends AuthenticationException {

		private static final long serialVersionUID = -4880737459768513029L;

		WrongUserPassException() {
			super("Wrong username/password.");
		}
	}

	private static class GetAuthenticityTokenHandler implements ResponseHandler<String> {

		@Override
		public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
			final HttpEntity entity = response.getEntity();
			final String contentCharset = EntityUtils.getContentCharSet(entity);
			final InputStreamReader reader;
			if (contentCharset != null) {
				reader = new InputStreamReader(entity.getContent(), contentCharset);
			} else {
				reader = new InputStreamReader(entity.getContent(), HTTP.UTF_8);
			}
			try {
				return readAuthenticityTokenFromHtml(reader);
			} catch (final XmlPullParserException e) {
				throw new AuthenticityTokenException();
			} finally {
				reader.close();
			}
		}

	}

	private static class GetOAuthAuthorizationHandler implements ResponseHandler<SignInResult> {

		@Override
		public SignInResult handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
			final HttpEntity entity = response.getEntity();
			final String contentCharset = EntityUtils.getContentCharSet(entity);
			final String entityString;
			if (contentCharset != null) {
				entityString = EntityUtils.toString(entity, contentCharset);
			} else {
				entityString = EntityUtils.toString(entity, HTTP.UTF_8);
			}
			try {
				final String callbackUrl = readCallbackUrlFromHtml(new StringReader(entityString));
				if (!isEmpty(callbackUrl)) return SignInResult.callbackUrl(callbackUrl);
			} catch (final XmlPullParserException e) {

			}
			try {
				final String pinCode = readOAuthPINFromHtml(new StringReader(entityString));
				if (!isEmpty(pinCode)) return SignInResult.pinCode(pinCode);
			} catch (final XmlPullParserException e) {

			}
			throw new WrongUserPassException();
		}

	}

}
