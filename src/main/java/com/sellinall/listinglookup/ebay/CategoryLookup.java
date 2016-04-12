package com.sellinall.listinglookup.ebay;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategoryLookup {

	static Logger log = Logger.getLogger(CategoryLookup.class.getName());

	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId) {

		BasicDBObject categorySpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId);

		if (categorySpecificsDB != null) {
			return categorySpecificsDB;
		}
		JSONObject categorySpecificsEbay = getCategorySpecificsFromEbay(countryCode, categoryId);
		JSONObject categoryFeaturesEbay = getCategoryFeaturesFromEbay(countryCode, categoryId);
		String categoryNamePath = getCategoryNamePathFromEbay(countryCode, categoryId);
		return persistToDB(countryCode, categoryId, categoryNamePath, categorySpecificsEbay, categoryFeaturesEbay);
	}

	public static Object getCategoryNamePath(String countryCode, String categoryId) {

		BasicDBObject categorySpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId);
		String categoryNamePath = "";
		if ((categorySpecificsDB != null) && categorySpecificsDB.containsField("categoryNamePath")) {
			categoryNamePath = categorySpecificsDB.getString("categoryNamePath");
		} else {
			JSONObject categorySpecificsEbay = getCategorySpecificsFromEbay(countryCode, categoryId);
			JSONObject categoryFeaturesEbay = getCategoryFeaturesFromEbay(countryCode, categoryId);
			categoryNamePath = getCategoryNamePathFromEbay(countryCode, categoryId);
			categorySpecificsDB = persistToDB(countryCode, categoryId, categoryNamePath, categorySpecificsEbay, categoryFeaturesEbay);
			categoryNamePath = categorySpecificsDB.getString("categoryNamePath");
		}
		JSONObject response = new JSONObject();
		response.put("categoryNamePath", categoryNamePath);
		return response;
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
		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			BasicDBObject categoryData = null;
			if (currentTime < expiryTime) {
				categoryData = new BasicDBObject();
				categoryData.put("itemSpecifics", lookupData.get("itemSpecifics"));
				categoryData.put("features", lookupData.get("features"));
				if (lookupData.containsField("categoryNamePath")) {
					categoryData.put("categoryNamePath", lookupData.getString("categoryNamePath"));
				}
			}
			return categoryData;
		}
		return lookupData;
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
			for (int i = 0; i < NameRecommendation.length(); i++) {
				JSONObject NameRecommendataionObj = NameRecommendation.getJSONObject(i);
				if (NameRecommendataionObj.has("ValueRecommendation")) {
					JSONArray ValueRecommendation = new JSONArray();
					if (NameRecommendataionObj.get("ValueRecommendation").getClass() == org.json.JSONArray.class) {
						ValueRecommendation = NameRecommendataionObj.getJSONArray("ValueRecommendation");
					} else {
						ValueRecommendation.put(NameRecommendataionObj.getJSONObject("ValueRecommendation"));
					}
					JSONArray valuesOfRecommendation = new JSONArray();
					for (int j = 0; j < ValueRecommendation.length(); j++) {
						valuesOfRecommendation.put(ValueRecommendation.getJSONObject(j).get("Value").toString());
					}
					NameRecommendataionObj.put("valuesOfRecommendation", valuesOfRecommendation);
					NameRecommendation.put(i, NameRecommendataionObj);
				}
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

	private static JSONObject getCategoryFeaturesFromEbay(String countryCode, String categoryId) {
		String categorySpecificsXML = EBayUtil.getCategoryFeatures(countryCode, categoryId);

		JSONObject categoryFeaturesFromEbay = XML.toJSONObject(categorySpecificsXML);
		log.debug(categoryFeaturesFromEbay);
		JSONObject GetCategoryFeaturesResponse = categoryFeaturesFromEbay.getJSONObject("GetCategoryFeaturesResponse");
		JSONObject categoryFeatures = new JSONObject();
		if (GetCategoryFeaturesResponse.has("Category")) {
			categoryFeatures = GetCategoryFeaturesResponse.getJSONObject("Category");
			categoryFeatures.remove("CategoryID");
		}
		return categoryFeatures;
	}

	private static String getCategoryNamePathFromEbay(String countryCode, String categoryId) {
		try {
			String categoryInfo = EBayUtil.getCategoryInfo(countryCode, categoryId);
			JSONObject categoryInfoJson = XML.toJSONObject(categoryInfo);
			log.debug(categoryInfoJson);
			String categoryNamePath = categoryInfoJson.getJSONObject("GetCategoryInfoResponse")
					.getJSONObject("CategoryArray").getJSONObject("Category").getString("CategoryNamePath");
			return categoryNamePath;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("returned empty category path name because of exception");
			return "";
		}
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, String categoryNamePath,
			JSONObject itemSpecifics, JSONObject categoryFeatures) {
		BasicDBObject filterField1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filterField2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);
		and.add(filterField2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		long expriyTime = (System.currentTimeMillis() / 1000L) + thirtyDays;
		BasicDBObject updateData = new BasicDBObject();
		updateData.put("expiryTime", expriyTime);
		if (!categoryNamePath.isEmpty()) {
			updateData.put("categoryNamePath", categoryNamePath);
		}
		updateData.put("itemSpecifics", JSON.parse(itemSpecifics.toString()));
		updateData.put("features", JSON.parse(categoryFeatures.toString()));

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("ebayCategoryLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}
