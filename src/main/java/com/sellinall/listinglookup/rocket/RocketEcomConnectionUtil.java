package com.sellinall.listinglookup.rocket;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.sellinall.util.HttpsURLConnectionUtil;

public class RocketEcomConnectionUtil {
	static Logger log = Logger.getLogger(RocketEcomConnectionUtil.class.getName());

	private static final Map<String, String> ScApiHost = Collections.unmodifiableMap(new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("SG", "https://sellercenter-api.lazada.sg");
			put("TH", "https://sellercenter-api.lazada.co.th");

		}
	});
	private static final String HASH_ALGORITHM = "HmacSHA256";
	private static final String CHAR_UTF_8 = "UTF-8";
	private static final String CHAR_ASCII = "ASCII";

	public static String getCategorySpecifics(String countryCode, String categoryId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("Action", "GetCategoryAttributes");
		params.put("PrimaryCategory", categoryId);

		String scApiHost = getScApiHost(countryCode);
		String apiKey = "3802dac6a310f90583ddabb0f0f446fb7692423b";
		params.put("UserID", "integrate@sellinall.com");
		if ("TH".equals(countryCode)) {
			apiKey = "98a0090266075e3094674b08783a1e7ee063aff2";
			params.put("UserID", "r.atchariya@gmail.com");
		}
		// provide XML as an empty string when not needed
		final String out = getSellercenterApiResponse(params, scApiHost, apiKey, "", "GET");
		log.debug(out);
		return out;
	}

	public static String getScApiHost(String countryCode) {
		return ScApiHost.get(countryCode);
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
				JSONObject response = HttpsURLConnectionUtil.doGet(request);
				output = response.getString("payload");
			} else if (method.equals("POST")) {
				output = HttpsURLConnectionUtil.doPostWithXML(request, xml);
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