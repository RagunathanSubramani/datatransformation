package com.sellinall.listinglookup.category;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategorySpecific {
	static Logger log = Logger.getLogger(CategorySpecific.class.getName());

	public static BasicDBObject getValues(String nicknameId, String categoryId, String countryCode, String accountNumber) {
		String channel = nicknameId.split("-")[0];
		DBCollection collection = getCollection(channel);
		BasicDBObject query = getQueryObject(nicknameId, categoryId, accountNumber, countryCode);
		BasicDBObject fields = new BasicDBObject("_id", 0);
		BasicDBObject result = (BasicDBObject) collection.findOne(query, fields);

		if (result == null && !accountNumber.equals(CategoryUtil.DEFAULT_ACCOUNT_NUMBER)) {
			query.put("nicknameId", channel);
			result = (BasicDBObject) collection.findOne(query, fields);
			if (result == null) {
				query.put("accountNumber", CategoryUtil.DEFAULT_ACCOUNT_NUMBER);
				result = (BasicDBObject) collection.findOne(query, fields);
			}
		}

		if (result != null) {
			return result;
		} else {
			return new BasicDBObject();
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getSiteFormatValues(String nicknameId, String categoryId, String countryCode,
			String accountNumber) {
		JSONObject output = new JSONObject();
		BasicDBObject result = getValues(nicknameId, categoryId, countryCode, accountNumber);
		if (result.containsField("values")) {
			List<BasicDBObject> values = (List<BasicDBObject>) result.get("values");
			for (BasicDBObject value : values) {
				JSONObject json = CategoryUtil.getJSONObjectFromDotNotation(value.getString("field"),
						value.get("value"));
				String key = json.keys().next();
				output.put(key, json.get(key));
			}
		}
		log.debug("output:" + output);
		return output;
	}

	public static Object upsertValues(String nicknameId, String categoryId, String countryCode, String accountNumber,
			String request) {
		JSONObject jsonRequest = new JSONObject(request);
		String channel = nicknameId.split("-")[0];
		DBCollection collection = getCollection(channel);

		BasicDBObject query = getQueryObject(nicknameId, categoryId, accountNumber, countryCode);
		log.debug("query:" + query);
		BasicDBObject fields = new BasicDBObject("_id", 0);

		jsonRequest.put("accountNumber", accountNumber);
		jsonRequest.put("nicknameId", nicknameId);
		jsonRequest.put("categoryID", categoryId);
		jsonRequest.put("countryCode", countryCode);
		BasicDBObject update = (BasicDBObject) JSON.parse(jsonRequest.toString());
		BasicDBObject result = (BasicDBObject) collection.findAndModify(query, fields, null, false, update, true, true);
		if (!jsonRequest.getString("accountNumber").equals(CategoryUtil.DEFAULT_ACCOUNT_NUMBER)) {
			// add record specific to account, but general to country and site
			persistAccountGenericData(jsonRequest, channel, collection, query);

			// add default record
			persistDefaultData(jsonRequest, collection, query);
		}
		return result;
	}

	private static void persistDefaultData(JSONObject jsonRequest, DBCollection collection, BasicDBObject query) {
		BasicDBObject update;
		query.put("accountNumber", CategoryUtil.DEFAULT_ACCOUNT_NUMBER);
		jsonRequest.put("accountNumber", CategoryUtil.DEFAULT_ACCOUNT_NUMBER);
		if (jsonRequest.getJSONArray("values").length() > 0) {
			update = (BasicDBObject) JSON.parse(jsonRequest.toString());
			collection.update(query, update, true, false);
		}
	}

	private static void persistAccountGenericData(JSONObject jsonRequest, String channel, DBCollection collection,
			BasicDBObject query) {
		BasicDBObject update;
		query.put("nicknameId", channel);
		jsonRequest.put("nicknameId", channel);
		removeSiteSpecificFields(jsonRequest);
		if (jsonRequest.getJSONArray("values").length() > 0) {
			update = (BasicDBObject) JSON.parse(jsonRequest.toString());
			collection.update(query, update, true, false);
		}
	}

	private static void removeSiteSpecificFields(JSONObject jsonRequest) {
		JSONArray values = jsonRequest.getJSONArray("values");
		for (int i = 0; i < values.length(); i++) {
			JSONObject value = values.getJSONObject(i);
			String field = value.getString("field");
			if (CategoryUtil.isFieldAccountAndSiteSpecific(jsonRequest.getString("nicknameId"), field)) {
				values.remove(i);
				i--;
			}
		}
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
		query.put("categoryID", categoryId);
		query.put("nicknameId", nicknameId);
		query.put("countryCode", countryCode);
		return query;
	}
}
