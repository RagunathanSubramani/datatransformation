package com.sellinall.listinglookup.product;

import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.amazon.AmazonProductLookup;
import com.sellinall.listinglookup.database.DbUtilities;

public class ProductLookup {
	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getMatchingProduct(String searchParam, String countryCode) {

		String searchParamType = findSearchParamType(searchParam);
		BasicDBObject matchingProductDB = getProductFromDB(searchParamType, searchParam, countryCode);

		if (matchingProductDB != null) {
			return matchingProductDB;
		}
		/*
		 * Need to write code to fetch from amazon
		 */
		JSONObject product = AmazonProductLookup.getProductFromSite(searchParamType, searchParam, countryCode);
		if (product.has("Error")) {
			return product;
		} else {
			return persistToDB(product, searchParamType, searchParam, countryCode);
		}
		// return product;
	}

	private static String findSearchParamType(String searchParam) {
		// TODO Auto-generated method stub
		// Need to write code to find whether it is ASIN or UPC or EAN
		int length = searchParam.length();
		if (length == 12) {
			return "UPC";
		} else if (length == 13) {
			return "EAN";
		} else {
			return "ASIN";
		}

	}

	private static BasicDBObject getProductFromDB(String searchParamType, String searchParam, String countryCode) {
		BasicDBObject searchQuery = new BasicDBObject(searchParamType, searchParam);
		if (countryCode != null) {
			searchQuery.put("countryCode", countryCode);
		}

		DBCollection table = DbUtilities.getLookupDBCollection("amazonProductLookup");
		BasicDBObject lookupData = (BasicDBObject) table.findOne(searchQuery);
		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			if (currentTime < expiryTime) {
				lookupData.removeField("expiryTime");
				lookupData.removeField("_id");
				return lookupData;
			}
			return null;// Since the time is expired
		}
		return lookupData;
	}

	private static BasicDBObject persistToDB(JSONObject product, String searchParamType, String searchParam,
			String countryCode) {

		long expriyTime = (System.currentTimeMillis() / 1000L) + thirtyDays;
		BasicDBObject updateData = new BasicDBObject();
		updateData.putAll((DBObject) JSON.parse(product.toString()));
		updateData.put("expiryTime", expriyTime);

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("amazonProductLookup");
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(searchParamType, searchParam);

		if (countryCode != null) {
			searchQuery.put("countryCode", countryCode);
			updateData.put("countryCode", countryCode);
		}
		table.update(searchQuery, setObject, true, false);

		updateData.removeField("expiryTime");
		updateData.removeField("_id");
		return updateData;
	}

}
