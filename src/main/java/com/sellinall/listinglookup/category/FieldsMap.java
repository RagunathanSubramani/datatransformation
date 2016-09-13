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

		List<DBObject> results = readDB(jsonRequest, standardFormatSource);
		if (results.isEmpty() && !jsonRequest.getString("accountNumber").equals(CategoryUtil.DEFAULT_ACCOUNT_NUMBER)) {
			jsonRequest.put("sourceNicknameId", jsonRequest.getString("sourceNicknameId").split("-")[0]);
			jsonRequest.put("targetNicknameId", jsonRequest.getString("targetNicknameId").split("-")[0]);
			results = readDB(jsonRequest, standardFormatSource);
			if (results.isEmpty()) {
				jsonRequest.put("accountNumber", CategoryUtil.DEFAULT_ACCOUNT_NUMBER);
				results = readDB(jsonRequest, standardFormatSource);
			}
		}

		DBObject result = new BasicDBObject();
		if (!results.isEmpty()) {
			result = results.get(0);
		}

		if (Boolean.parseBoolean(standardFormat)) {
			return result;
		} else {
			JSONObject siteFormatResult = convertToSiteFormat(result, jsonRequest.getJSONObject("source"));
			return siteFormatResult;
		}
	}

	private static List<DBObject> readDB(JSONObject jsonRequest, JSONArray standardFormatSource) {
		String searchKey = "";
		searchKey = getKeyFromSource(standardFormatSource, searchKey);
		BasicDBObject query = getQueryObject(jsonRequest);
		query.put("$text", new BasicDBObject("$search", searchKey));
		log.debug("query " + query);

		BasicDBObject fields = new BasicDBObject("_id", 0);
		fields.put("score", new BasicDBObject("$meta", "textScore"));
		log.debug("fields " + fields);
		DBCollection collection = DbUtilities.getLookupDBCollection("fieldsMap");
		List<DBObject> results = (List<DBObject>) collection.find(query, fields)
				.sort(new BasicDBObject("score", new BasicDBObject("$meta", "textScore"))).limit(1).toArray();
		return results;
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

	@SuppressWarnings("unchecked")
	private static JSONObject convertToSiteFormat(DBObject result, JSONObject sourceFromRequest) {
		JSONObject output = new JSONObject(sourceFromRequest.toString());
		if (result.containsField("map")) {
			List<BasicDBObject> map = (List<BasicDBObject>) result.get("map");
			for (BasicDBObject mapEntry : map) {
				List<BasicDBObject> sourceList = (List<BasicDBObject>) mapEntry.get("source");
				BasicDBObject target = (BasicDBObject) mapEntry.get("target");
				Object targetValue = null;
				if (target.containsField("value")) {
					targetValue = target.getString("value");
				} else {
					// For the current use cases, only one object can be present
					// in
					// the array. Handle multiple objects use case in future as
					// needed.
					BasicDBObject source = sourceList.get(0);
					targetValue = getValueFromSource(source.getString("field"), sourceFromRequest);
				}

				if (targetValue != null) {
					String targetField = target.getString("field");
					JSONObject json = getJSONObjectFromDotNotation(targetField, targetValue);
					mergeKeys(json, output);
				}

			}
		}
		return output;
	}

	// Merge is done only for nested json objects
	private static void mergeKeys(JSONObject mergeFrom, JSONObject mergeTo) {
		String key = mergeFrom.keys().next();
		if (mergeTo.has(key)) {
			if ((mergeFrom.get(key) instanceof JSONObject) && (mergeFrom.get(key) instanceof JSONObject)) {
				mergeKeys(mergeFrom.getJSONObject(key), mergeTo.getJSONObject(key));
			} else {
				mergeTo.put(key, mergeFrom.get(key));
			}
		} else {
			mergeTo.put(key, mergeFrom.get(key));
		}
	}

	private static Object getValueFromSource(String field, JSONObject sourceFromRequest) {
		String[] fields = field.split("\\.", 2);
		if (fields.length == 2) {
			return getValueFromSource(fields[1], sourceFromRequest);
		} else {
			return sourceFromRequest.get(fields[0]);
		}
	}

	private static JSONObject getJSONObjectFromDotNotation(String field, Object value) {
		String[] fields = field.split("\\.", 2);
		JSONObject result = new JSONObject();
		if (fields.length == 2) {
			result.put(fields[0], getJSONObjectFromDotNotation(fields[1], value));
		} else {
			result.put(fields[0], value);
		}
		return result;
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
		BasicDBObject result = updateDB(jsonRequest, true);
		if (!jsonRequest.getString("accountNumber").equals(CategoryUtil.DEFAULT_ACCOUNT_NUMBER)) {
			persistAccountGenericData(jsonRequest);
		}
		return result;
	}

	private static BasicDBObject updateDB(JSONObject jsonRequest, boolean returnResult) {
		String searchKey = "";
		JSONArray map = jsonRequest.getJSONArray("map");
		for (int i = 0; i < map.length(); i++) {
			searchKey = addDelimiter(i, searchKey, ",");
			JSONObject entry = map.getJSONObject(i);
			JSONArray source = entry.getJSONArray("source");
			searchKey = getKeyFromSource(source, searchKey);
		}
		log.debug("searchKey:" + searchKey);
		BasicDBObject update = (BasicDBObject) JSON.parse(jsonRequest.toString());
		update.put("searchKey", searchKey);

		BasicDBObject query = getQueryObject(jsonRequest);
		query.put("searchKey", searchKey);

		BasicDBObject fields = new BasicDBObject("_id", 0);
		DBCollection collection = DbUtilities.getLookupDBCollection("fieldsMap");
		log.debug("query " + query + "fields " + fields + "update " + update);
		if (returnResult) {
			BasicDBObject result = (BasicDBObject) collection.findAndModify(query, fields, null, false, update, true,
					true);
			return result;
		} else {
			collection.update(query, update, true, false);
			return null;
		}
	}

	private static void persistAccountGenericData(JSONObject jsonRequest) {
		jsonRequest.put("sourceNicknameId", jsonRequest.getString("sourceNicknameId").split("-")[0]);
		jsonRequest.put("targetNicknameId", jsonRequest.getString("targetNicknameId").split("-")[0]);
		removeSiteSpecificFields(jsonRequest);
		updateDB(jsonRequest, false);
	}

	private static void removeSiteSpecificFields(JSONObject jsonRequest) {
		JSONArray map = jsonRequest.getJSONArray("map");
		for (int i = 0; i < map.length(); i++) {
			JSONObject entry = map.getJSONObject(i);
			JSONArray source = entry.getJSONArray("source");
			for (int j = 0; j < source.length(); j++) {
				JSONObject sourceItem = source.getJSONObject(j);
				String field = sourceItem.getString("field");
				if (CategoryUtil.isFieldAccountAndSiteSpecific(jsonRequest.getString("sourceNicknameId"), field)) {
					map.remove(i);
					i--;
					break;
				}
			}
		}
	}

	private static BasicDBObject getQueryObject(JSONObject jsonRequest) {
		BasicDBObject query = new BasicDBObject();
		query.put("sourceNicknameId", jsonRequest.getString("sourceNicknameId"));
		query.put("sourceCountryCode", jsonRequest.getString("sourceCountryCode"));
		query.put("targetNicknameId", jsonRequest.getString("targetNicknameId"));
		query.put("targetCountryCode", jsonRequest.getString("targetCountryCode"));
		query.put("accountNumber", jsonRequest.getString("accountNumber"));
		query.put("sourceCategoryId", jsonRequest.getString("sourceCategoryId"));
		return query;
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
		// TODO: fix this to replace all punctuation marks with _. The list can
		// be found at
		// https://docs.mongodb.com/manual/core/index-text/#tokenization-delimiters.
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
