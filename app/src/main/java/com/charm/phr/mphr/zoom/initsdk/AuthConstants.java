package com.charm.phr.mphr.zoom.initsdk;

public interface AuthConstants {

    // TODO Change it to your web domain
    public final static String WEB_DOMAIN = "zoom.us";//No I18N

    String SDK_KEY = ""; //No I18N

    String SDK_SECRET = ""; //No I18N

    /**
     * We recommend that, you can generate jwttoken on your own server instead of hardcore in the code.
     * We hardcore it here, just to run the demo.
     *
     * You can generate a jwttoken on the https://jwt.io/
     * with this payload:
     * {
     *
     *     "appKey": "string", // app key
	 *     "iat": long, // access token issue timestamp
	 *     "exp": long, // access token expire time
	 *     "tokenExp": long // token expire time
	 * }
	 */
	/*public final static String SDK_JWTTOKEN = JWT_TOKEN;*/

}
