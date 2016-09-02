package com.sellinall.listinglookup.category;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategoryMap {
	static Logger log = Logger.getLogger(CategorySpecific.class.getName());

	public static Object getCategoryMap(String sourceChannel, String sourceCountryCode, String categoryId,
			String targetChannel, String targetCountryCode) {
		String input = "{\"sourceNicknameId\":\"eBay-1\",\"sourceCountryCode\":\"US\",\"targetNicknameId\":\"eBay-2\",\"targetCountryCode\":\"AU\",\"accountNumber\":\"12345\",\"source\":[{\"field\":\"auction\",\"value\":\"true\"},{\"field\":\"bestOffer\",\"value\":\"false\"},{\"field\":\"categoryID\",\"value\":\"12344\"},{\"field\":\"conditionID\",\"value\":\"1000\"},{\"field\":\"storeFront.storeCategoryID\",\"value\":\"12\"},{\"field\":\"itemDuration\",\"value\":\"Days_7\"},{\"parent\":\"itemSpecifics\",\"field\":\"title+names\",\"value\":\"Gem Type+Chrysoberyl\"},{\"parent\":\"itemSpecifics\",\"field\":\"title+names\",\"value\":\"Color+Lime_Green\"},{\"parent\":\"itemSpecifics\",\"field\":\"title+names\",\"value\":\"Hardness+8.0_MOH' SCALE\"}]}";
		JSONObject jsonInput = new JSONObject(input);
		JSONArray source = jsonInput.getJSONArray("source");
		String key = "";
		key = getKeyFromSource(source, key);

		log.debug("key = " + key);

		BasicDBObject query = new BasicDBObject();
		query.put("sourceNicknameId", jsonInput.getString("sourceNicknameId"));
		query.put("sourceCountryCode", jsonInput.getString("sourceCountryCode"));
		query.put("targetNicknameId", jsonInput.getString("targetNicknameId"));
		query.put("targetCountryCode", jsonInput.getString("targetCountryCode"));
		query.put("accountNumber", jsonInput.getString("accountNumber"));
		query.put("$text", new BasicDBObject("$search", key));
		log.debug("query " + query);

		BasicDBObject fields = new BasicDBObject("_id", 0);
		fields.put("score", new BasicDBObject("$meta", "textScore"));
		log.debug("fields " + fields);
		DBCollection collection = DbUtilities.getLookupDBCollection("categoryMapPoC");
		List<DBObject> result = (List<DBObject>) collection.find(query, fields)
				.sort(new BasicDBObject("score", new BasicDBObject("$meta", "textScore"))).limit(1).toArray();
		if (result.size() > 0) {
			return result.get(0);
		} else {
			return new BasicDBObject();
		}
	}

	public static BasicDBObject createMap(String Mudra, String body) {
		String input = "{\"sourceNicknameId\":\"eBay-1\",\"sourceCountryCode\":\"US\",\"targetNicknameId\":\"eBay-2\",\"targetCountryCode\":\"AU\",\"accountNumber\":\"12345\",\"map\":[{\"source\":[{\"field\":\"categoryId\",\"value\":\"12344\"},{\"parent\":\"itemSpecifics\",\"field\":\"title+names\",\"value\":\"Gem Type+Chrysoberyl\"},],\"target\":{\"field\":\"categoryId\",\"value\":\"12345\"}},{\"source\":[{\"field\":\"storeFront.storeCategoryID\",\"value\":\"12\"}],\"target\":{\"field\":\"storeFront.storeCategoryID\",\"value\":\"15\"}}]}";
		JSONObject jsonInput = new JSONObject(input);
		JSONArray map = jsonInput.getJSONArray("map");
		String key = "";
		for (int i = 0; i < map.length(); i++) {
			key = addDelimiter(i, key, ",");
			JSONObject entry = map.getJSONObject(i);
			JSONArray source = entry.getJSONArray("source");
			key = getKeyFromSource(source, key);
		}
		log.debug("key:" + key);
		BasicDBObject update = (BasicDBObject) JSON.parse(jsonInput.toString());
		update.put("key", key);

		BasicDBObject query = new BasicDBObject();
		query.put("sourceNicknameId", jsonInput.getString("sourceNicknameId"));
		query.put("sourceCountryCode", jsonInput.getString("sourceCountryCode"));
		query.put("targetNicknameId", jsonInput.getString("targetNicknameId"));
		query.put("targetCountryCode", jsonInput.getString("targetCountryCode"));
		query.put("accountNumber", jsonInput.getString("accountNumber"));
		query.put("key", key);

		BasicDBObject fields = new BasicDBObject("_id", 0);
		DBCollection collection = DbUtilities.getLookupDBCollection("categoryMapPoC");
		BasicDBObject result = (BasicDBObject) collection.findAndModify(query, fields, null, false, update, true, true);
		return result;
	}

	private static String getKeyFromSource(JSONArray source, String key) {
		for (int j = 0; j < source.length(); j++) {
			key = addDelimiter(j, key, ",");
			JSONObject sourceItem = source.getJSONObject(j);
			String field = sourceItem.getString("field");
			String value = sourceItem.getString("value");
			if (sourceItem.has("parent")) {
				key = key + sourceItem.getString("parent") + "_";
				Map<String, String> fieldValueMap = getMap(field, value);
				field = "";
				value = "";
				int mapIndex = 0;
				for (Entry<String, String> fieldValueEntry : fieldValueMap.entrySet()) {
					field = addDelimiter(mapIndex, field, "+");
					value = addDelimiter(mapIndex, value, "+");
					field = field + fieldValueEntry.getKey();
					value = value + fieldValueEntry.getValue();
					mapIndex++;
				}
			}
			key = key + field.replace(" ", "_").replace("+", "_").replace(".", "_") + "_";
			key = key + value.replace(" ", "_").replace("+", "_").replace(".", "_");
		}
		return key;
	}

	private static Map<String, String> getMap(String field, String value) {
		String[] fields = field.split("\\+");
		String[] values = value.split("\\+");
		Map<String, String> fieldValueMap = new TreeMap<String, String>();
		for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
			fieldValueMap.put(fields[fieldIndex], values[fieldIndex]);
		}
		return fieldValueMap;
	}

	private static String addDelimiter(int index, String str, String delimiter) {
		if (index > 0) {
			str = str + delimiter;
		}
		return str;
	}

}
