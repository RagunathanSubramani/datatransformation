package com.sellinall.listinglookup.ebay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategoryLookup {
	private static final long twoDays = 2 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId) {

		BasicDBObject categorySpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId);

		if (categorySpecificsDB != null) {
			return categorySpecificsDB;
		} else {
			JSONObject categorySpecificsEbay = getCategorySpecificsFromEbay(countryCode, categoryId);
			persistToDB(countryCode, categoryId, categorySpecificsEbay);
			return categorySpecificsEbay;
		}
	}

	private static BasicDBObject getCategorySpecificsFromDB(String countryCode, String categoryId) {
		BasicDBObject filterField1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filterField2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);
		and.add(filterField2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		DBCollection table = DbUtilities.getLookupDBCollection("ebayCategoryLookup");
		BasicDBObject lookupData = (BasicDBObject) table.findOne(searchQuery);
		BasicDBObject itemSpecifics = null;

		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			if (expiryTime > currentTime) {
				itemSpecifics = (BasicDBObject) lookupData.get("itemSpecifics");
			}
		}

		return itemSpecifics;
	}

	private static JSONObject getCategorySpecificsFromEbay(String countryCode, String categoryId) {
		String categorySpecificsXML = EBayUtil.getCategorySpecifics(countryCode, categoryId);

		JSONObject categorySpecificsFromEbay = XML.toJSONObject(categorySpecificsXML);
		JSONObject GetCategorySpecificsResponse = categorySpecificsFromEbay
				.getJSONObject("GetCategorySpecificsResponse");
		JSONObject Recommendations = GetCategorySpecificsResponse.getJSONObject("Recommendations");
		JSONArray NameRecommendation;
		if (Recommendations.has("NameRecommendation")) {
			// NameRecommendation can either be a json object (in case of
			// single element) or json array. Handle accordingly.
			if (Recommendations.get("NameRecommendation").getClass() == org.json.JSONArray.class) {
				NameRecommendation = new JSONArray(Recommendations.getJSONArray("NameRecommendation").toString());
			} else {
				JSONObject NameRecommendationElement = new JSONObject(Recommendations.getJSONObject(
						"NameRecommendation").toString());
				NameRecommendation = new JSONArray();
				NameRecommendation.put(NameRecommendationElement);
			}
		} else {
			// create empty array when NameRecommendation is not present in
			// the response from eBay.
			NameRecommendation = new JSONArray();
		}
		JSONObject categorySpecifics = new JSONObject();
		categorySpecifics.put("NameRecommendation", NameRecommendation);
		return categorySpecifics;
	}

	private static void persistToDB(String countryCode, String categoryId, JSONObject itemSpecifics) {
		BasicDBObject filterField1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filterField2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);
		and.add(filterField2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		long expriyTime = (System.currentTimeMillis() / 1000L) + twoDays;
		BasicDBObject updateData = new BasicDBObject();
		updateData.put("expiryTime", expriyTime);
		updateData.put("itemSpecifics", JSON.parse(itemSpecifics.toString()));

		BasicDBObject setObject = new BasicDBObject("$set", updateData);

		DBCollection table = DbUtilities.getLookupDBCollection("ebayCategoryLookup");
		table.update(searchQuery, setObject, true, false);
	}

}
