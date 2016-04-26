package com.sellinall.listinglookup.shopclues;

import java.io.IOException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;
import freemarker.template.TemplateException;

public class CategoryLookup {
	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId) throws JSONException, IOException,
			TemplateException {

		BasicDBObject shopcluesSpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId);

		if (shopcluesSpecificsDB != null) {
			return shopcluesSpecificsDB;
		}
		JSONArray shopcluesAttributes = getAttributeFromShopclues(countryCode, categoryId);
		return persistToDB(countryCode, categoryId, shopcluesAttributes);

	}

	private static BasicDBObject getCategorySpecificsFromDB(String countryCode, String categoryId) {
		BasicDBObject filter1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filter2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filter1);
		and.add(filter2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		DBCollection table = DbUtilities.getLookupDBCollection("shopcluesCategoryLookup");
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

	private static JSONArray getAttributeFromShopclues(String countryCode, String categoryId) throws JSONException,
			IOException, TemplateException {
		return ShopcluesUtil.getCategorySpecifics(countryCode, categoryId);
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONArray shopcluesAttributes) {
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
		updateData.put("attributes", JSON.parse(shopcluesAttributes.toString()));

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("shopcluesCategoryLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}
