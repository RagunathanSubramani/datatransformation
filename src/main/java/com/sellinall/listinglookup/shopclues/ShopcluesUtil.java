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
import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.HttpsURLConnectionUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ShopcluesUtil {

	private static final Map<String, String> ScApiHost = Collections.unmodifiableMap(new HashMap<String, String>() {

		private static final long serialVersionUID = 1L;

		{
			put("IN", Config.getConfig().getShopcluesItemSpecificationUrl());

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
		JSONObject responseJson = HttpsURLConnectionUtil.doGet(urlCat, data);
		String responseString = responseJson.getString("payload");
		Map<String, Object> map = new Gson().fromJson(responseString, new TypeToken<HashMap<String, Object>>() {
		}.getType());

		template.process(map, out);
		return new JSONArray(out.toString());
	}

	private static String getAccessToken() throws JSONException, IOException {
		String url = Config.getConfig().getShopcluesAuthUrl();
		Map<String, String> mapHeader = new HashMap<String, String>();
		mapHeader.put("Content-Type", "application/json");
		JSONObject responseString = HttpsURLConnectionUtil.doPost(url, getPayLoadForAuthtoken().toString(), mapHeader);
		return parseResponse(responseString.getString("payload"));
	}

	private static JSONObject getPayLoadForAuthtoken() throws JSONException {
		JSONObject payLoad = new JSONObject();
		payLoad.put("username", Config.getConfig().getUsername());
		payLoad.put("password", Config.getConfig().getPassword());
		payLoad.put("client_id", Config.getConfig().getClient_id());
		payLoad.put("client_secret", Config.getConfig().getClient_secret());
		payLoad.put("grant_type", Config.getConfig().getGrant_type());
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
