package com.sellinall.listinglookup.category;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategorySpecific {
	private static final String DEFAULT = "default";
	static Logger log = Logger.getLogger(CategorySpecific.class.getName());

	public static BasicDBObject getValues(String nicknameId, String categoryId, String countryCode, String accountNumber) {
		String channel = nicknameId.split("-")[0];
		DBCollection collection = getCollection(channel);
		BasicDBObject query = getQueryObject(nicknameId, categoryId, accountNumber, countryCode);
		BasicDBObject fields = new BasicDBObject("_id", 0);
		BasicDBObject result = (BasicDBObject) collection.findOne(query, fields);

		if (result == null) {
			query.put("accountNumber", DEFAULT);
			query.put("nicknameId", channel);
			result = (BasicDBObject) collection.findOne(query, fields);
		}

		if (result != null) {
			return result;
		} else {
			return new BasicDBObject();
		}
	}

	public static Object upsertValues(String nicknameId, String categoryId, String accountNumber, String request) {
		JSONObject jsonRequest = new JSONObject(request);
		String countryCode = jsonRequest.has("countryCode") ? jsonRequest.getString("countryCode") : null;

		String channel = nicknameId.split("-")[0];
		DBCollection collection = getCollection(channel);
		if (accountNumber.equals(DEFAULT)) {
			nicknameId = channel;
		}

		BasicDBObject query = getQueryObject(nicknameId, categoryId, accountNumber, countryCode);
		log.debug("query:" + query);
		BasicDBObject fields = new BasicDBObject("_id", 0);

		jsonRequest.put("accountNumber", accountNumber);
		jsonRequest.put("nicknameId", nicknameId);
		jsonRequest.put("categoryId", categoryId);
		if (countryCode != null) {
			jsonRequest.put("countryCode", countryCode);
		}
		BasicDBObject update = (BasicDBObject) JSON.parse(jsonRequest.toString());
		BasicDBObject result = (BasicDBObject) collection.findAndModify(query, fields, null, false, update, true, true);
		return result;
	}

	private static DBCollection getCollection(String channel) {
		String collectionName = channel + "CategorySpecificValues";
		log.debug("collectionName:" + collectionName);
		DBCollection collection = DbUtilities.getLookupDBCollection(collectionName);
		return collection;
	}

	private static BasicDBObject getQueryObject(String nicknameId, String categoryId, String accountNumber,
			String countryCode) {
		BasicDBObject query = new BasicDBObject("accountNumber", accountNumber);
		query.put("categoryId", categoryId);
		query.put("nicknameId", nicknameId);
		if (countryCode != null) {
			query.put("countryCode", countryCode);
		}
		return query;
	}
}
