package com.sellinall.listinglookup.shopclues;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mudra.sellinall.util.HttpsURLConnectionUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ShopcluesUtil {

	private static final Map<String, String> ScApiHost = Collections.unmodifiableMap(new HashMap<String, String>() {

		private static final long serialVersionUID = 1L;

		{
			put("IN", "https://sandbox.shopclues.com/api/feature?");

		}
	});

	public static JSONArray getCategorySpecifics(String countryCode, String categoryId) throws JSONException,
			IOException, TemplateException {
		Configuration cfg = new Configuration();
		Template template = cfg.getTemplate("src/main/resources/Template/default/shopclues/itemSpecifications.ftl");

		Map<String, String> data = new HashMap<String, String>();
		data.put("Content-Type", "application/json");
		data.put("Authorization", "Bearer " + getAccessToken());

		String url = ScApiHost.get(countryCode);
		String urlCat = url + "category_id=" + categoryId;
		StringWriter out = new StringWriter();
		String responseString = HttpsURLConnectionUtil.doGet(urlCat, data);
		Map<String, Object> map = new Gson().fromJson(responseString, new TypeToken<HashMap<String, Object>>() {
		}.getType());

		template.process(map, out);
		return new JSONArray(out.toString());
	}

	private static String getAccessToken() throws JSONException {
		String url = "https://sandbox.shopclues.com/oauth/loginToken.php";
		Map<String, String> mapHeader = new HashMap<String, String>();
		mapHeader.put("Content-Type", "application/json");
		String responseString = HttpsURLConnectionUtil.doPost(url, getPayLoadForAuthtoken(), mapHeader);
		return parseResponse(responseString);
	}

	private static JSONObject getPayLoadForAuthtoken() throws JSONException {
		JSONObject payLoad = new JSONObject();
		payLoad.put("username", "55@sc.com");
		payLoad.put("password", "e10adc3949ba59abbe56e057f20f883e");
		payLoad.put("client_id", "10945358");
		payLoad.put("client_secret", "KM1LCWRT8E6P007");
		payLoad.put("grant_type", "password");
		return payLoad;
	}

	private static String parseResponse(String responseString) {
		try {
			JSONObject response = new JSONObject(responseString);
			String accessToken = response.getString("access_token");
			return accessToken;
		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}
}
