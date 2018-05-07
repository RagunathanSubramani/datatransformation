package com.sellinall.listinglookup.lazada;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.HttpsURLConnectionUtil;

public class RocketEcomConnectionUtil {
	static Logger log = Logger.getLogger(RocketEcomConnectionUtil.class.getName());
	private static boolean ignoreSSLCheck = true; // ignore SSL check when
    private static boolean ignoreHostCheck = true; // ignore HOST check when
    protected static int connectTimeout = 15000; // default connection timeout
    protected static int readTimeout = 30000; // default read timeout
    private static String signMethod = "hmac";

	private static final String HASH_ALGORITHM = "HmacSHA256";
	private static final String CHAR_UTF_8 = "UTF-8";
	private static final String CHAR_ASCII = "ASCII";

	public static String getCategorySpecificsFromNewApi(String categoryId, String host) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("primary_category_id", categoryId);
		String apiName = "/category/attributes/get";
		final String out = getSellercenterNewApiResponse(params, host, "GET", apiName, categoryId);
		log.debug(out);
		return out;
	}

	public static String getCategorySpecifics(String countryCode, String categoryId, String userID, String apiKey,
			String host) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("Action", "GetCategoryAttributes");
		params.put("PrimaryCategory", categoryId);

		String scApiHost = host;
		params.put("UserID", userID);
		final String out = getSellercenterApiResponse(params, scApiHost, apiKey, "", "GET");
		log.debug(out);
		return out;
	}

	private static String getSellercenterNewApiResponse(Map<String, String> params, String url, String method,
			String apiName, String categoryId) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		String response = "";
		params.put("sign_method", signMethod);
		params.put("app_key", Config.getConfig().getLazadaClientID());
		Long timestamp = System.currentTimeMillis();
		params.put("timestamp", "" + timestamp);
		String sign = signApiRequest(apiName, params, null, Config.getConfig().getLazadaClientSecret(), signMethod);
		url = url + apiName;
		url += "?app_key=" + Config.getConfig().getLazadaClientID();
		url += "&primary_category_id=" + categoryId;
		url += "&sign_method=hmac&sign=" + sign + "&timestamp=" + timestamp;
		response = _doGet(url, method);
		return response;
	}

	private static String _doGet(String url, String method) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		HttpURLConnection conn = null;
		OutputStream out = null;
		StringBuilder sb = new StringBuilder();
		try {
			conn = getConnection(new URL(url), method);
			Reader inn = new BufferedReader(new InputStreamReader(new GZIPInputStream(conn.getInputStream()), "UTF-8"));
			for (int c; (c = inn.read()) >= 0;)
				sb.append((char) c);
		} finally {
			if (out != null) {
				out.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return sb.toString();
	}

	private static HttpURLConnection getConnection(URL url, String method) throws IOException, NoSuchAlgorithmException, KeyManagementException {
		HttpURLConnection conn = null;

		conn = (HttpURLConnection) url.openConnection();

		if (conn instanceof HttpsURLConnection) {
			HttpsURLConnection connHttps = (HttpsURLConnection) conn;
			if (ignoreSSLCheck) {
				try {
					SSLContext ctx = SSLContext.getInstance("TLS");
					ctx.init(null, new TrustManager[] { new TrustAllTrustManager() }, new SecureRandom());
					connHttps.setSSLSocketFactory(ctx.getSocketFactory());
					connHttps.setHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					});
			} catch (Exception e) {
					throw new IOException(e.toString());
				}
			} else {
				if (ignoreHostCheck) {
					connHttps.setHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					});
				}
			}
			conn = connHttps;
		}

		conn.setRequestMethod(method);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Host", "https://auth.lazada.com");
		conn.setRequestProperty("Accept", "text/xml,text/javascript");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		conn.setRequestProperty("Accept-Encoding", "gzip");
		return conn;
	}

	public static String signApiRequest(String apiName, Map<String, String> params, String body, String appSecret,
			String signMethod) throws IOException {
		// first: sort all text parameters
		String[] keys = params.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		// second: connect all text parameters with key and value
		StringBuilder query = new StringBuilder();
		query.append(apiName);

		for (String key : keys) {
			String value = params.get(key);
			if (areNotEmpty(key, value)) {
				query.append(key).append(value);
			}
		}

		// third：put the body to the end
		if (body != null) {
			query.append(body);
		}

		// next : sign the whole request
		byte[] bytes = null;

		bytes = encryptWithHmac(query.toString(), appSecret);

		// finally : transfer sign result from binary to upper hex string
		return byte2hex(bytes);
	}

	/**
	 * Check whether the given string list are null or blank.
	 */
	public static boolean areNotEmpty(String... values) {
		boolean result = true;
		if (values == null || values.length == 0) {
			result = false;
		} else {
			for (String value : values) {
				result &= !isEmpty(value);
			}
		}
		return result;
	}

	public static byte[] encryptWithHmac(String data, String secret) throws IOException {
		byte[] bytes = null;
		try {
			SecretKey secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacMD5");
			Mac mac = Mac.getInstance(secretKey.getAlgorithm());
			mac.init(secretKey);
			bytes = mac.doFinal(data.getBytes("UTF-8"));
		} catch (GeneralSecurityException gse) {
			throw new IOException(gse.toString());
		}
		return bytes;
	}

	/**
	 * Transfer binary array to HEX string.
	 */
	public static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}

	public static boolean isEmpty(String value) {
		int strLen;
		if (value == null || (strLen = value.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(value.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	public static class TrustAllTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	}

	/**
	 * calculates the signature and sends the request
	 *
	 * @param params
	 *            Map - request parameters
	 * @param apiKey
	 *            String - user's API Key
	 * @param JSON
	 *            String - Request Body
	 * 
	 *            final String out = getSellercenterApiResponse(params,
	 *            ScApiHost, apiKey, JSON, "GET"); // provide XML as an empty
	 *            string when not needed
	 */
	public static String getSellercenterApiResponse(Map<String, String> params, String ScApiHost, String apiKey,
			String xml, String method) {
		String queryString = "";
		String output = "";
		JSONObject response = new JSONObject();
		params.put("Timestamp", getCurrentTimestamp());
		params.put("Version", "1.0");
		params.put("Format", "JSON");
		Map<String, String> sortedParams = new TreeMap<String, String>(params);
		queryString = toQueryString(sortedParams);
		final String signature = hmacDigest(queryString, apiKey, HASH_ALGORITHM);
		queryString = queryString.concat("&Signature=".concat(signature));
		log.debug(queryString);

		final String request = ScApiHost.concat("?".concat(queryString));
		try {
			Map<String, String> config = new HashMap<String, String>();
			config.put("Content-Type", "application/x-www-form-urlencoded");
			config.put("charset", CHAR_UTF_8);
			Thread.sleep(1000);
			if (method.equals("GET")) {
				response = HttpsURLConnectionUtil.doGet(request, config);
			} else if (method.equals("POST")) {
				response = HttpsURLConnectionUtil.doPost(request, xml, config);
			}
			if (response.has("payload")) {
				output = response.getString("payload");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * generates hash key
	 *
	 * @param msg
	 * @param keyString
	 * @param algo
	 * @return string
	 */
	private static String hmacDigest(String msg, String keyString, String algo) {
		String digest = null;
		try {
			SecretKeySpec key = new SecretKeySpec((keyString).getBytes(CHAR_UTF_8), algo);
			Mac mac = Mac.getInstance(algo);
			mac.init(key);
			final byte[] bytes = mac.doFinal(msg.getBytes(CHAR_ASCII));
			StringBuffer hash = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			digest = hash.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return digest;
	}

	/**
	 * build querystring out of params map
	 *
	 * @param data
	 *            map of params
	 * @return string
	 * @throws UnsupportedEncodingException
	 */
	private static String toQueryString(Map<String, String> data) {
		String queryString = "";
		try {
			StringBuffer params = new StringBuffer();
			for (Map.Entry<String, String> pair : data.entrySet()) {
				params.append(URLEncoder.encode((String) pair.getKey(), CHAR_UTF_8) + "=");
				params.append(URLEncoder.encode((String) pair.getValue(), CHAR_UTF_8) + "&");
			}
			if (params.length() > 0) {
				params.deleteCharAt(params.length() - 1);
			}
			queryString = params.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return queryString;
	}

	/**
	 * returns the current timestamp
	 * 
	 * @return current timestamp in ISO 8601 format
	 */
	private static String getCurrentTimestamp() {
		final TimeZone tz = TimeZone.getTimeZone("UTC");
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		df.setTimeZone(tz);
		final String nowAsISO = df.format(new Date());
		return nowAsISO;
	}
}