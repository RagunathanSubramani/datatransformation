package com.sellinall.listinglookup.zalora;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

public class BuildCategory {
	private static String totalString = "";

	public static String buildNewCategoryList(String accountNumber, String nickNameID)
			throws IOException, JSONException {
		totalString = "";
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil
				.doGet(/*Config.getConfig().getRocketEcomAdaptorUrl()*/ "http://localhost:8082/services"+ "/categories?nickNameID=" + nickNameID, header);
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject(serviceResponse.getString("payload"));
			if (response.has("SuccessResponse")) {
				JSONObject successResponse = (JSONObject) response.get("SuccessResponse");
				parseAndPrintFile("",
						successResponse.getJSONObject("Body").getJSONObject("Categories").get("Category"));
			}
			totalString = totalString.substring(0, totalString.length() - 1);
			return "[" + totalString + "]";
		}
		return "";
	}

	private static void parseAndPrintFile(String categoryName, Object json) throws JSONException {
		categoryName = categoryName.replaceAll("&amp;", "&");
		if (json instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) json;
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject JsonObject = jsonArray.getJSONObject(i);
				construct(categoryName, JsonObject);
			}
		} else {

			JSONObject JsonObject = (JSONObject) json;
			construct(categoryName, JsonObject);
		}
	}

	private static void construct(String categoryName, JSONObject json) throws JSONException {
		String childCategoryName = (String) json.get("Name");
		String childCategoryId = "";
		if (json.has("CategoryId")) {
			childCategoryId = "" + json.get("CategoryId");
		}
		if (json.has("Children") && !json.getString("Children").equals("")) {
			Object object = json.getJSONObject("Children").get("Category");
			JSONArray childJSONArray = new JSONArray();
			JSONObject childJSON = new JSONObject();
			if (object instanceof JSONObject) {
				childJSON = (JSONObject) object;
			} else {
				childJSONArray = (JSONArray) object;
			}
			childCategoryName = (String) json.get("Name");
			if (json.has("CategoryId")) {
				childCategoryId = "" + json.get("CategoryId");
			}
			if (childJSONArray.length() != 0) {
				if (categoryName.length() == 0) {
					parseAndPrintFile(childCategoryName, childJSONArray);
				} else {
					parseAndPrintFile(categoryName + ":" + childCategoryName, childJSONArray);
				}
			} else {
				totalString = "\"" + categoryName + ":" + childCategoryName + " ## " + childCategoryId + "\" ,"
						+ totalString;
			}
			if (childJSON.has("Children") && !childJSON.getString("Children").equals("")) {
				if (categoryName.length() == 0) {
					parseAndPrintFile(childCategoryName, childJSON);
				} else {
					parseAndPrintFile(categoryName + ":" + childCategoryName, childJSON);
				}
			}

		} else {
			totalString = "\"" + categoryName + ":" + childCategoryName + " ## " + childCategoryId + "\" ,"
					+ totalString;
		}
	}

}
