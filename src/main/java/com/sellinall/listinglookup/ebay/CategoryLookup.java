package com.sellinall.listinglookup.ebay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.BasicDBObject;

public class CategoryLookup {
	public static Object getCategorySpecifics(String countryCode, String categoryId) {

		BasicDBObject categorySpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId);

		if (categorySpecificsDB != null) {
			return categorySpecificsDB;
		} else {
			JSONObject categorySpecificsEbay = getCategorySpecificsFromEbay(countryCode, categoryId);
			persisitToDB(countryCode, categoryId, categorySpecificsEbay);
			return categorySpecificsEbay;
		}
	}

	private static BasicDBObject getCategorySpecificsFromDB(String countryCode, String categoryId) {
		// TODO Auto-generated method stub
		return null;
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

	private static void persisitToDB(String countryCode, String categoryId, JSONObject categorySpecificsEbay) {
		/*BasicDBObject filterField1 = new BasicDBObject("SKU", sku);
		BasicDBObject filterField2 = new BasicDBObject("userId", userId);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);
		and.add(filterField2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		DBCollection table = DbUtilities.getInventoryDBCollection("inventory");
		BasicDBObject inventory = (BasicDBObject) table.findOne(searchQuery);*/

	}

}
