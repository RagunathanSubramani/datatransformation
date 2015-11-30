package com.sellinall.listinglookup.product;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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
		 */ 
		JSONObject product = AmazonProductLookup.getProductFromSite(searchParamType, searchParam);
		System.out.println(product);
		return persistToDB( product,searchParamType, searchParam);
		//return product;
	}

	private static String findSearchParamType(String searchParam) {
		// TODO Auto-generated method stub
		//Need to write code to find whether it is ASIN or UPC or EAN
		return "ASIN";
	}

	private static BasicDBObject getProductFromDB(String searchParamType, String searchParam) {
		BasicDBObject searchQuery = new BasicDBObject(searchParamType, searchParam);

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



	private static BasicDBObject persistToDB(JSONObject product, String searchParamType, String searchParam) {


		long expriyTime = (System.currentTimeMillis() / 1000L) + thirtyDays;
		BasicDBObject updateData = new BasicDBObject();
		updateData.putAll( (DBObject)JSON.parse(product.toString()));
		updateData.put("expiryTime", expriyTime);

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("amazonProductLookup");
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(searchParamType,searchParam);
		table.update( searchQuery , setObject, true, false);
	
		updateData.removeField("expiryTime");
		updateData.removeField("_id");
		return updateData;
	}

}
