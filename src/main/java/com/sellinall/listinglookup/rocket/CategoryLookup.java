package com.sellinall.listinglookup.rocket;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategoryLookup {
	static Logger log = Logger.getLogger(CategoryLookup.class.getName());
	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId) {

		BasicDBObject lazadaAttributesDB = getCategoryAttributesFromDB(countryCode, categoryId);

		if (lazadaAttributesDB != null) {
			return lazadaAttributesDB;
		}
		JSONArray lazadaAttributes = getAttributesFromLazada(countryCode, categoryId);
		return persistToDB(countryCode, categoryId, lazadaAttributes);
	}

	private static BasicDBObject getCategoryAttributesFromDB(String countryCode, String categoryId) {
		BasicDBObject filterField1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filterField2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);
		and.add(filterField2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		DBCollection table = DbUtilities.getLookupDBCollection("lazadaAttributeLookup");
		BasicDBObject lookupData = (BasicDBObject) table.findOne(searchQuery);
		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			BasicDBObject attributesData = null;
			if (currentTime < expiryTime) {
				attributesData = new BasicDBObject();
				attributesData.put("attributes", lookupData.get("attributes"));
			}
			return attributesData;
		}
		return lookupData;
	}

	private static JSONArray getAttributesFromLazada(String countryCode, String categoryId) {
		String categorySpecificsXML = RocketEcomConnectionUtil.getCategorySpecifics(countryCode, categoryId);

		JSONObject categorySpecificsFromLazada = new JSONObject(categorySpecificsXML);
		log.debug(categorySpecificsFromLazada);
		JSONObject body = categorySpecificsFromLazada
				.getJSONObject("SuccessResponse")
				.getJSONObject("Body");
		JSONArray attributes = body.getJSONArray("Attribute");
//		JSONArray NameRecommendation;
//		if (Recommendations.has("NameRecommendation")) {
//			// NameRecommendation can either be a json object (in case of
//			// single element) or json array. Handle accordingly.
//			if (Recommendations.get("NameRecommendation").getClass() == org.json.JSONArray.class) {
//				NameRecommendation = new JSONArray(Recommendations.getJSONArray("NameRecommendation").toString());
//			} else {
//				JSONObject NameRecommendationElement = new JSONObject(Recommendations.getJSONObject(
//						"NameRecommendation").toString());
//				NameRecommendation = new JSONArray();
//				NameRecommendation.put(NameRecommendationElement);
//			}
//			for (int i = 0; i < NameRecommendation.length(); i++) {
//				JSONObject NameRecommendataionObj = NameRecommendation.getJSONObject(i);
//				if (NameRecommendataionObj.has("ValueRecommendation")) {
//					JSONArray ValueRecommendation = new JSONArray();
//					if (NameRecommendataionObj.get("ValueRecommendation").getClass() == org.json.JSONArray.class) {
//						ValueRecommendation = NameRecommendataionObj.getJSONArray("ValueRecommendation");
//					} else {
//						ValueRecommendation.put(NameRecommendataionObj.getJSONObject("ValueRecommendation"));
//					}
//					JSONArray valuesOfRecommendation = new JSONArray();
//					for (int j = 0; j < ValueRecommendation.length(); j++) {
//						valuesOfRecommendation.put(ValueRecommendation.getJSONObject(j).get("Value").toString());
//					}
//					NameRecommendataionObj.put("valuesOfRecommendation", valuesOfRecommendation);
//					NameRecommendation.put(i, NameRecommendataionObj);
//				}
//			}
//		} else {
//			// create empty array when NameRecommendation is not present in
//			// the response from lazada.
//			NameRecommendation = new JSONArray();
//		}
//		JSONObject categorySpecifics = new JSONObject();
//		categorySpecifics.put("NameRecommendation", NameRecommendation);
		log.debug(attributes);
		return attributes;
	}



	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONArray lazadaAttributes) {
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
		updateData.put("attributes", JSON.parse(lazadaAttributes.toString()));

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("lazadaAttributeLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}
