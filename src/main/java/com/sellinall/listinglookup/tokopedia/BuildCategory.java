package com.sellinall.listinglookup.tokopedia;

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

	public static Object buildNewCategoryList(String countryCode) throws JSONException, IOException {
		JSONArray parsedCategorylist = new JSONArray();
		JSONArray categoryList = getCategoryList(countryCode);
		parseCategory("", categoryList, parsedCategorylist);
		return parsedCategorylist;
	}

	private static void parseCategory(String previousChild, JSONArray categories, JSONArray parsedCategorylist)
			throws JSONException {
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			if (category.has("child") && !category.isNull("child") && category.getJSONArray("child").length() > 0) {
				String categoryName = (previousChild.isEmpty() ? "" : previousChild+":") + category.getString("name");
				parseCategory(categoryName, category.getJSONArray("child"), parsedCategorylist);
			} else {
				String categoryName = (previousChild.isEmpty() ? "" : previousChild+":") + category.getString("name");
				parsedCategorylist.put(categoryName + " ## " + category.getString("id"));
			}
		}
	}

	private static JSONArray getCategoryList(String countryCode) throws JSONException, IOException {
		String accountNumber = Config.getConfig().getTokopediaAccountDetails(countryCode);
		String nickNameId = Config.getConfig().getTokopediaNickNameID(countryCode);
		return getCategoryListFromTokopedia(accountNumber, nickNameId);
	}

	public static JSONArray getCategoryListFromTokopedia(String accountNumber, String nickNameID)
			throws JSONException, IOException {
		JSONArray categories = new JSONArray();
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil
				.doGet(Config.getConfig().getSiaTokopediaUrl() + "/categories?nickNameID=" + nickNameID, header);
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			String payload = serviceResponse.getString("payload");
			JSONObject response = new JSONObject(payload);
			if(response.has("categoryList")) {
				categories = response.getJSONArray("categoryList");
			}
		}
		return categories;
	}
}
