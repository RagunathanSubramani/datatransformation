package com.sellinall.listinglookup.lazada;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;

public class BuildCategory {
	private static String totalString = "";

	public static String buildNewCategiryList(String accountNumber, String nickNameID) {
		totalString = "";
		BasicDBObject userChannel = CategoryLookup.getUserDetailsFromUser(accountNumber, nickNameID);
		Map<String, String> params = new HashMap<String, String>();
		params.put("Action", "GetCategoryTree");
		params.put("UserID", userChannel.getString("userID"));
		String response = RocketEcomConnectionUtil.getSellercenterApiResponse(params, userChannel.getString("hostURL"),
				userChannel.getString("apikey"), "", "GET");
		JSONObject json = new JSONObject(response);
		if (json.has("SuccessResponse")) {
			JSONObject successResponse = (JSONObject) json.get("SuccessResponse");
			parseAndPrintFile("", (JSONArray) successResponse.get("Body"));
			totalString = totalString.substring(0, totalString.length() - 1);
			return "[" + totalString + "]";
		}
		return "";
	}

	private static void parseAndPrintFile(String categoryName, JSONArray jsonArray) {
		// TODO Auto-generated method stub
		// System.out.println(jsonArray);
		categoryName = categoryName.replaceAll("&amp;", "&");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject json = jsonArray.getJSONObject(i);
			if (json.has("children")) {
				JSONArray childJSON = (JSONArray) json.get("children");
				String childCategoryName = (String) json.get("name");
				String childCategoryId = "" + json.get("categoryId");
				if (childJSON.length() != 0) {
					if (categoryName.length() == 0) {
						parseAndPrintFile(childCategoryName, childJSON);
					} else {
						parseAndPrintFile(categoryName + ":" + childCategoryName, childJSON);
					}
				} else {
					totalString = "\"" + categoryName + ":" + childCategoryName + " ## " + childCategoryId + "\" ,"
							+ totalString;
				}

			}

		}
	}

}
