package org.mariotaku.utwitterapi;

public interface Constants {

	public static final String PACKAGE_NAME = "org.mariotaku.utwitterapi";

	public static final String HOST_TWITTER = "twitter.com";
	public static final String HOST_TWITTER_API = "api.twitter.com";

	public static final String LOGTAG = "UTwitterAPI";
	
	public static final String PATH_OAUTH_REQUEST_TOKEN = "/oauth/request_token";

	public static final String SHARED_PREFERENCE_NAME_PREFERENCES = "preferences";
	public static final String SHARED_PREFERENCE_NAME_CLIENTS = "clients";

	public static final String KEY_API_ADDRESS = "api_address";
	public static final String KEY_IP_ADDRESS = "ip_address";
	public static final String KEY_USE_API_TO_SIGN_IN = "use_api_to_sign_in";

	public static final String QUERY_PARAM_USERNAME = "username";
	public static final String QUERY_PARAM_PASSWORD = "password";
	public static final String QUERY_PARAM_OAUTH_TOKEN = "oauth_token";

	public static final String VIRTUAL_URL_GET_CALLBACK = "http://org.mariotaku.utwitterapi/get_callback";

	public static final String SIGN_IN_HTML = "<html>\n"
			+ "<head>\n"
			+ "  <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n"
			+ "  <title>UTwitterAPI Emulate OAuth login</title>\n"
			+ "</head>\n"
			+ "<body>\n"
			+ "<h2>UTwitterAPI Emulate OAuth login</h2>\n"
			+ "<hr>\n"
			+ "<p><form action=\"http://org.mariotaku.utwitterapi/get_callback\" method=\"get\">\n"
			+ "Username: <input type=\"text\" name=\"username\">\n"
			+ "<br/>\n"
			+ "Password: <input type=\"password\" name=\"password\">\n"
			+ "<br/>\n"
			+ "<input type=\"text\" name=\"oauth_token\" value=\"OAUTH_TOKEN\" style=\"visibility:visible\" readonly=\"true\"/>\n"
			+ "<br/>\n" + "<input type=\"submit\" value=\"Login\">\n" + "</form></p>\n" + "</body>\n" + "</html>";
	public static final String CALLBACK_HTML = "<html>\n" + "<head>\n"
			+ "  <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">\n"
			+ "  <meta http-equiv=\"refresh\" content=\"1;url=CALLBACK_URL\" />\n" + "</head>\n" + "<body>\n"
			+ "You are being <a href=\"CALLBACK_URL\">redirected</a>.\n" + "</body>\n" + "</html>";

}
