package com.sellinall.listinglookup;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;
import com.sellinall.listinglookup.shopclues.ShopcluesUtil;
import com.sellinall.listinglookup.snapdeal.SnapdealUtil;

import freemarker.template.TemplateException;

public class CategoryLookup {
	static Logger log = Logger.getLogger(CategoryLookup.class.getName());
	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId, String channelName)
			throws JSONException, IOException, TemplateException {

		BasicDBObject channelSpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId, channelName);

		if (channelSpecificsDB != null) {
			return channelSpecificsDB;
		}
		Object channelAttributes = getAttributesFromChannel(countryCode, categoryId, channelName);
		return persistToDB(countryCode, categoryId, channelAttributes, channelName);

	}

	private static BasicDBObject getCategorySpecificsFromDB(String countryCode, String categoryId, String channelName) {
		BasicDBObject filter1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filter2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filter1);
		and.add(filter2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		String collectionName = getCollectionName(channelName);// "channelCategoryLookup";
		DBCollection table = DbUtilities.getLookupDBCollection(collectionName);
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

	private static String getCollectionName(String channelName) {
		switch(channelName){
		case "shopclues":
			return "shopcluesCategoryLookup";
		case "snapdeal":
			return "snapdealCategoryLookup";
		default:
			log.error("unsupported channel " + channelName);
			return null;
		}
	}

	private static Object getAttributesFromChannel(String countryCode, String categoryId, String channelName)
			throws JSONException, IOException, TemplateException {
		switch (channelName) {
		case "shopclues":
			return ShopcluesUtil.getCategorySpecifics(countryCode, categoryId);
		case "snapdeal":
			return SnapdealUtil.getCategorySpecifics(countryCode, categoryId);
		default:
			log.error("unsupported channel " + channelName);
			return null;
		}
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, Object channelAttributes, String channelName) {
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
		updateData.put("attributes", JSON.parse(channelAttributes.toString()));

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		String collectionName = getCollectionName(channelName);
		DBCollection table = DbUtilities.getLookupDBCollection(collectionName);
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}
