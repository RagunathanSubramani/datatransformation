package com.sellinall.listinglookup.bukalapak;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

public class BuildCategory {
	static Logger log = Logger.getLogger(BuildCategory.class.getName());
	static String categoryName = "";

	public static Object buildNewCategoryList(String countryCode) throws JSONException, IOException {
		JSONArray parsedCategorylist = new JSONArray();
		JSONArray categoryList = getCategoryList(countryCode);
		for (int i = 0; i < categoryList.length(); i++) {
			JSONObject category = categoryList.getJSONObject(i);
			categoryName = category.getString("name");
			if (category.has("children") && category.getJSONArray("children").length() > 0) {
				parseCategory("", category.getJSONArray("children"), parsedCategorylist);
			} else {
				parsedCategorylist.put(categoryName + " ## " + category.getString("id"));
			}
		}
		return parsedCategorylist;
	}

	private static void parseCategory(String previousChild, JSONArray categories, JSONArray parsedCategorylist)
			throws JSONException {
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			categoryName += (categoryName.isEmpty() ? "" : "/") + category.getString("name");
			if (category.has("children") && category.getJSONArray("children").length() > 0) {
				parseCategory(category.getString("name"), category.getJSONArray("children"), parsedCategorylist);
			} else {
				parsedCategorylist.put(categoryName + " ## " + category.getString("id"));
				categoryName = categoryName.replace("/" + category.getString("name"), "");
			}
			if (i == categories.length() - 1) {
				categoryName = categoryName.replace("/" + previousChild, "");
			}
		}
	}

	private static JSONArray getCategoryList(String countryCode) throws JSONException, IOException {
		String accountNumber = Config.getConfig().getBukalapakAccountDetails(countryCode);
		String nickNameId = Config.getConfig().getBukalapakNickNameID(countryCode);
		return getCategoryListFromBukalapak(accountNumber, nickNameId);
	}

	public static JSONArray getCategoryListFromBukalapak(String accountNumber, String nickNameID)
			throws JSONException, IOException {
		JSONArray categories = new JSONArray();
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil
				.doGet(Config.getConfig().getSiaBukalapakUrl() + "/categories?nickNameID=" + nickNameID, header);
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject payload = new JSONObject(serviceResponse.getString("payload"));
			categories = payload.getJSONArray("data");
		}
		return categories;
	}
}
