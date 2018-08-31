package com.sellinall.listinglookup.shopee;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.sellinall.listinglookup.CategoryLookup;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.HttpsURLConnectionUtil;

import freemarker.template.TemplateException;

/**
 * 
 * @author Raguvaran
 *
 */

public class ShopeeUtil {
	static Logger log = Logger.getLogger(CategoryLookup.class.getName());

	public static JSONObject getCategorySpecifics(BasicDBObject credential, String countryCode, String categoryId)
			throws JSONException, IOException, TemplateException {
		String url = Config.getConfig().getShopeeUrl() + "item/attributes/get";
		JSONObject payload = new JSONObject();
		payload.put("partner_id", Config.getConfig().getShopeeClientID());
		payload.put("shopid", credential.getLong("shopID"));
		payload.put("timestamp", System.currentTimeMillis() / 1000);
		payload.put("category_id", Integer.parseInt(categoryId));
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", getSignature(Config.getConfig().getShopeeClientSecret(), url, payload.toString()));
		return HttpsURLConnectionUtil.doPost(url, payload.toString(), headers);
	}

	public static JSONObject getCategoryList(BasicDBObject credential) throws JSONException, IOException {
		String url = Config.getConfig().getShopeeUrl() + "item/categories/get";
		JSONObject payload = new JSONObject();
		payload.put("partner_id", Config.getConfig().getShopeeClientID());
		payload.put("shopid", credential.getLong("shopID"));
		payload.put("timestamp", System.currentTimeMillis() / 1000);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", getSignature(Config.getConfig().getShopeeClientSecret(), url, payload.toString()));
		return new JSONObject(HttpsURLConnectionUtil.doPost(url, payload.toString(), headers).getString("payload"));
	}

	public static String getSignature(String credentialKey, String url, String payload) {
		String baseString = url + "|" + payload;
		return hmacDigest(credentialKey, baseString, "HmacSHA256");
	}

	public static String hmacDigest(String credentialKey, String baseString, String algorithm) {
		String result = null;
		try {
			SecretKeySpec key = new SecretKeySpec((credentialKey).getBytes("UTF-8"), algorithm);
			Mac mac = Mac.getInstance(algorithm);
			mac.init(key);
			final byte[] bytes = mac.doFinal(baseString.getBytes("ASCII"));
			StringBuffer hash = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			result = hash.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}

}