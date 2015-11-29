package com.sellinall.listinglookup.product;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.amazon.AmazonUtil;
import com.sellinall.listinglookup.database.DbUtilities;

public class ProductLookup {
	private static final long thirtyDays = 30 * 24 * 60 * 60;


	public static Object getMatchingProduct( String searchParam) {
		
		String searchParamType = findSearchParamType(searchParam);
		BasicDBObject matchingProductDB = getProductFromDB(searchParamType, searchParam);

		if (matchingProductDB != null) {
			return matchingProductDB;
		}
		/*Need to write code to fetch from amazon
		 * JSONObject categorySpecificsEbay = getCategorySpecificsFromEbay(countryCode, categoryId);
		JSONObject categoryFeaturesEbay = getCategoryFeaturesFromEbay(countryCode, categoryId);
		return persistToDB(countryCode, categoryId, categorySpecificsEbay, categoryFeaturesEbay);*/
		return null;
	}

	private static String findSearchParamType(String searchParam) {
		// TODO Auto-generated method stub
		//Need to write code to find whether it is ASIN or UPC or EAN
		return "ASIN";
	}

	private static BasicDBObject getProductFromDB(String searchParamType, String searchParam) {
		BasicDBObject filterField1 = new BasicDBObject(searchParamType, searchParam);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		DBCollection table = DbUtilities.getLookupDBCollection("amazonProductLookup");
		BasicDBObject lookupData = (BasicDBObject) table.findOne(searchQuery);
		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			if (currentTime < expiryTime) {
				return lookupData;
			}
			return null;//Since the time is expired
		}
		return lookupData;
	}

	private static JSONObject getCategorySpecificsFromEbay(String countryCode, String categoryId) {
		String categorySpecificsXML = AmazonUtil.getCategorySpecifics(countryCode, categoryId);

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
		String categorySpecificsXML = AmazonUtil.getCategoryFeatures(countryCode, categoryId);

		JSONObject categoryFeaturesFromEbay = XML.toJSONObject(categorySpecificsXML);
		System.out.println(categoryFeaturesFromEbay);
		JSONObject GetCategoryFeaturesResponse = categoryFeaturesFromEbay.getJSONObject("GetCategoryFeaturesResponse");
		JSONObject categoryFeatures = new JSONObject();
		if (GetCategoryFeaturesResponse.has("Category")) {
			categoryFeatures = GetCategoryFeaturesResponse.getJSONObject("Category");
			categoryFeatures.remove("CategoryID");
		}
		return categoryFeatures;
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONObject itemSpecifics,
			JSONObject categoryFeatures) {
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
		updateData.put("itemSpecifics", JSON.parse(itemSpecifics.toString()));
		updateData.put("features", JSON.parse(categoryFeatures.toString()));

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("ebayCategoryLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}
