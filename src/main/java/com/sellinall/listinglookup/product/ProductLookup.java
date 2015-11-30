package com.sellinall.listinglookup.product;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.amazon.AmazonProductLookup;
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
		 */ JSONObject product = AmazonProductLookup.getProductFromSite(searchParamType, searchParam);
		//return persistToDB(countryCode, categoryId, categorySpecificsEbay, categoryFeaturesEbay);*/
		return product;
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
