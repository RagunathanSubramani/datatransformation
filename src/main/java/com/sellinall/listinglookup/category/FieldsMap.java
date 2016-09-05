package com.sellinall.listinglookup.category;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class FieldsMap {
	static Logger log = Logger.getLogger(FieldsMap.class.getName());

	public static Object postSourceChannelDetails(String request, String standardFormat) {

		JSONArray standardFormatSource;
		JSONObject jsonRequest = new JSONObject(request);

		if (!Boolean.parseBoolean(standardFormat)) {
			JSONObject source = jsonRequest.getJSONObject("source");
			standardFormatSource = convertToStandardFormatSource(source);
		} else {
			standardFormatSource = jsonRequest.getJSONArray("source");
		}
		log.debug(standardFormatSource);
		String searchKey = "";
		searchKey = getKeyFromSource(standardFormatSource, searchKey);

		log.debug("searchKey = " + searchKey);

		BasicDBObject query = new BasicDBObject();
		query.put("sourceNicknameId", jsonRequest.getString("sourceNicknameId"));
		query.put("sourceCountryCode", jsonRequest.getString("sourceCountryCode"));
		query.put("targetNicknameId", jsonRequest.getString("targetNicknameId"));
		query.put("targetCountryCode", jsonRequest.getString("targetCountryCode"));
		query.put("accountNumber", jsonRequest.getString("accountNumber"));
		query.put("$text", new BasicDBObject("$search", searchKey));
		log.debug("query " + query);

		BasicDBObject fields = new BasicDBObject("_id", 0);
		fields.put("score", new BasicDBObject("$meta", "textScore"));
		log.debug("fields " + fields);
		DBCollection collection = DbUtilities.getLookupDBCollection("fieldsMap");
		List<DBObject> result = (List<DBObject>) collection.find(query, fields)
				.sort(new BasicDBObject("score", new BasicDBObject("$meta", "textScore"))).limit(1).toArray();
		if (result.size() > 0) {
			return result.get(0);
		} else {
			return new BasicDBObject();
		}
	}

	private static JSONArray convertToStandardFormatSource(JSONObject source) {
		JSONArray output = new JSONArray();
		Set<String> keySet = source.keySet();
		for (String key : keySet) {

			Object value = source.get(key);
			if (value instanceof JSONObject) {
				JSONArray outputArray = convertToStandardFormatSource((JSONObject) value);
				for (int i = 0; i < outputArray.length(); i++) {
					JSONObject json = new JSONObject();
					json.put("field", key + "." + outputArray.getJSONObject(i).getString("field"));
					json.put("value", outputArray.getJSONObject(i).getString("value"));
					output.put(json);
				}
			} else if (value instanceof JSONArray) {
				JSONArray valueArray = (JSONArray) value;
				if (!"itemSpecifics".equals(key)) {
					JSONObject json = new JSONObject();
					json.put("field", key);
					json.put("value", getCSV(valueArray));
					output.put(json);
				} else {
					for (int i = 0; i < valueArray.length(); i++) {
						String title = valueArray.getJSONObject(i).getString("title");
						JSONArray names = valueArray.getJSONObject(i).getJSONArray("names");
						JSONObject json = new JSONObject();
						json.put("field", key + "." + title);
						json.put("value", getCSV(names));
						output.put(json);
					}
				}
			} else {
				JSONObject json = new JSONObject();
				json.put("field", key);
				json.put("value", value.toString());
				output.put(json);
			}
		}

		return output;
	}

	private static String getCSV(JSONArray names) {
		String str = "";
		for (int i = 0; i < names.length(); i++) {
			str = addDelimiter(i, str, ",");
			str = str + names.get(i).toString();
		}
		return str;
	}

	public static BasicDBObject createMap(String request) {
		JSONObject jsonRequest = new JSONObject(request);
		JSONArray map = jsonRequest.getJSONArray("map");
		String searchKey = "";
		for (int i = 0; i < map.length(); i++) {
			searchKey = addDelimiter(i, searchKey, ",");
			JSONObject entry = map.getJSONObject(i);
			JSONArray source = entry.getJSONArray("source");
			searchKey = getKeyFromSource(source, searchKey);
		}
		log.debug("searchKey:" + searchKey);
		BasicDBObject update = (BasicDBObject) JSON.parse(jsonRequest.toString());
		update.put("searchKey", searchKey);

		BasicDBObject query = new BasicDBObject();
		query.put("sourceNicknameId", jsonRequest.getString("sourceNicknameId"));
		query.put("sourceCountryCode", jsonRequest.getString("sourceCountryCode"));
		query.put("targetNicknameId", jsonRequest.getString("targetNicknameId"));
		query.put("targetCountryCode", jsonRequest.getString("targetCountryCode"));
		query.put("accountNumber", jsonRequest.getString("accountNumber"));

		BasicDBObject fields = new BasicDBObject("_id", 0);
		DBCollection collection = DbUtilities.getLookupDBCollection("fieldsMap");
		log.debug("query " + query + "fields " + fields + "update " + update);
		BasicDBObject result = (BasicDBObject) collection.findAndModify(query, fields, null, false, update, true, true);
		return result;
	}

	private static String getKeyFromSource(JSONArray source, String key) {
		for (int j = 0; j < source.length(); j++) {
			key = addDelimiter(j, key, ",");
			JSONObject sourceItem = source.getJSONObject(j);
			String field = sourceItem.getString("field");
			String value = "";
			if (sourceItem.has("value")) {
				value = sourceItem.getString("value");
			}
			key = replacePunctuationMarks(key, field, value);
		}
		return key;
	}

	private static String replacePunctuationMarks(String key, String field, String value) {
		// TODO: fix this to replace all punctuation marks with _.
		key = key + field.replace(" ", "_").replace("+", "_").replace(".", "_") + "_";
		key = key + value.replace(" ", "_").replace("+", "_").replace(".", "_");
		return key;
	}

	private static String addDelimiter(int index, String str, String delimiter) {
		if (index > 0) {
			str = str + delimiter;
		}
		return str;
	}

}
