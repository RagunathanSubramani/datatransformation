package com.sellinall.listinglookup.shopee;

import java.io.IOException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.sellinall.listinglookup.config.Config;

public class BuildCategory {
	public static Object buildNewCategoryList(String countryCode) throws JSONException, IOException {
		JSONArray parsedCategorylist = new JSONArray();
		JSONObject categoryList = getCategorySpecificsFromShopee(countryCode);
		if (categoryList.has("categories")) {
			JSONArray categories = categoryList.getJSONArray("categories");
			parseCategory("", 0, categories, parsedCategorylist);
		}
		return parsedCategorylist;
	}

	private static void parseCategory(String categoryName, int categoryID, JSONArray categories,
			JSONArray parsedCategorylist) throws JSONException {
		Boolean isLastLeaf = true;
		String appendCategory = "";
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			if (category.has("processed")) {
				continue;
			}
			if (category.getInt("parent_id") == categoryID) {
				isLastLeaf = false;
				appendCategory = categoryName + (categoryName.isEmpty() ? "" : ":")
						+ category.getString("category_name");
				category.put("processed", true);
				parseCategory(appendCategory, category.getInt("category_id"), categories, parsedCategorylist);

			}
		}
		if (isLastLeaf) {
			parsedCategorylist.put(categoryName + " ## " + categoryID);
		}
	}

	private static JSONObject getCategorySpecificsFromShopee(String countryCode)
			throws JSONException, IOException {
		JSONObject categoryList = new JSONObject();
		String accountNumber = Config.getConfig().getShopeeAccountDetails(countryCode);
		String nickNameId = Config.getConfig().getShopeeNickNameID(countryCode);
		BasicDBObject accountInformation = new BasicDBObject();
		if (!accountNumber.isEmpty() && !nickNameId.isEmpty()) {
			accountInformation = CategoryLookup.getAccountDetails(accountNumber, nickNameId);
			if (accountInformation != null) {
				categoryList = ShopeeUtil.getCategoryList(accountInformation);
			}
		}
		return categoryList;
	}
}
