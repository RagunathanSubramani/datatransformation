package com.sellinall.listinglookup.category;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategorySpecific {
	static Logger log = Logger.getLogger(CategorySpecific.class.getName());

	public static BasicDBObject getValues(String channel, String categoryId, String countryCode, String accountNumber) {
		DBCollection collection = getCollection(channel);
		BasicDBObject query = getQueryObject(categoryId, accountNumber, countryCode);
		BasicDBObject fields = new BasicDBObject("_id", 0);
		BasicDBObject result = (BasicDBObject) collection.findOne(query, fields);

		if (result == null) {
			query.put("accountNumber", "default");
			result = (BasicDBObject) collection.findOne(query, fields);
		}

		if (result != null) {
			return result;
		} else {
			return new BasicDBObject();
		}
	}

	public static Object upsertValues(String channel, String categoryId, String accountNumber, String request) {
		JSONObject jsonRequest = new JSONObject(request);
		String countryCode = jsonRequest.has("countryCode") ? jsonRequest.getString("countryCode") : null;

		DBCollection collection = getCollection(channel);
		BasicDBObject query = getQueryObject(categoryId, accountNumber, countryCode);
		log.debug("query:" + query);
		BasicDBObject fields = new BasicDBObject("_id", 0);
		BasicDBObject update = new BasicDBObject("$set", JSON.parse(request));
		BasicDBObject result = (BasicDBObject) collection.findAndModify(query, fields, null, false, update, true, true);
		return result;
	}

	private static DBCollection getCollection(String channel) {
		String collectionName = channel + "CategorySpecificValues";
		log.debug("collectionName:" + collectionName);
		DBCollection collection = DbUtilities.getLookupDBCollection(collectionName);
		return collection;
	}

	private static BasicDBObject getQueryObject(String categoryId, String accountNumber, String countryCode) {
		BasicDBObject query = new BasicDBObject("accountNumber", accountNumber);
		query.put("categoryId", categoryId);
		if (countryCode != null) {
			query.put("countryCode", countryCode);
		}
		return query;
	}
}
