package com.sellinall.listinglookup.category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

public class FieldsMap {
	static Logger log = Logger.getLogger(FieldsMap.class.getName());

	public static Object postSourceChannelDetails(String request, String standardFormat) {

		JSONArray standardFormatSource;
		JSONObject jsonRequest = new JSONObject(request);
		if (Boolean.parseBoolean(standardFormat)) {
			standardFormatSource = jsonRequest.getJSONArray("source");
		} else {
			JSONObject source = jsonRequest.getJSONObject("source");
			standardFormatSource = convertToStandardFormatSource(source);
		}
		log.debug("standardFormatSource:" + standardFormatSource);
		List<DBObject> results = readDB(jsonRequest, standardFormatSource);
		String targetNicknameId = jsonRequest.getString("targetNicknameId");
		String accountNumber = jsonRequest.getString("accountNumber");
		String targetCountryCode = jsonRequest.getString("targetCountryCode");
		String sourceNicknameId = jsonRequest.getString("sourceNicknameId");
		if (results.isEmpty() && !accountNumber.equals(CategoryUtil.DEFAULT_ACCOUNT_NUMBER)) {
			if (sourceNicknameId.contains("-") || targetNicknameId.contains("-")) {
				jsonRequest.put("sourceNicknameId", sourceNicknameId.split("-")[0]);
				jsonRequest.put("targetNicknameId", targetNicknameId.split("-")[0]);
				results = readDB(jsonRequest, standardFormatSource);
			}
			if (results.isEmpty()) {
				jsonRequest.put("accountNumber", CategoryUtil.DEFAULT_ACCOUNT_NUMBER);
				results = readDB(jsonRequest, standardFormatSource);
			}
		}
		DBObject result = new BasicDBObject();
		if (!results.isEmpty()) {
			result = results.get(0);
		}
		log.debug("result:" + result);

		if (Boolean.parseBoolean(standardFormat)) {
			return result;
		} else {
			JSONObject output = getSiteFormat(jsonRequest, targetNicknameId, accountNumber, targetCountryCode,
					sourceNicknameId, result);
			return output;
		}
	}

	private static JSONObject getSiteFormat(JSONObject jsonRequest, String targetNicknameId, String accountNumber,
			String targetCountryCode, String sourceNicknameId, DBObject result) {
		String targetCategoryID = getTargetCategoryID(result, jsonRequest.getJSONObject("source"));
		boolean sameChannel = sourceNicknameId.split("-")[0].equals(targetNicknameId.split("-")[0]);
		if (targetCategoryID == null && sameChannel && jsonRequest.getJSONObject("source").has("categoryID")) {
			targetCategoryID = jsonRequest.getJSONObject("source").getString("categoryID");
		}
		log.debug("targetCategoryID:" + targetCategoryID);
		JSONObject siteFormatResult = convertToSiteFormat(result, jsonRequest.getJSONObject("source"));
		log.debug("siteFormatResult:" + siteFormatResult);
		if (targetCategoryID != null) {
			JSONObject defaultValues = CategorySpecific.getSiteFormatValues(targetNicknameId, targetCategoryID,
					targetCountryCode, accountNumber);
			Set<String> keySet = defaultValues.keySet();
			for (String key : keySet) {
				if (!siteFormatResult.has(key)) {
					siteFormatResult.put(key, defaultValues.get(key));
				}
			}
			if (defaultValues.has("itemSpecifics")) {
				processItemSpecificsValues(defaultValues.getJSONArray("itemSpecifics"), siteFormatResult);
			}
		}
		log.debug("siteFormatResult:" + siteFormatResult);
		JSONObject output = new JSONObject();
		if (sameChannel) {
			output = new JSONObject(jsonRequest.getJSONObject("source").toString());
		}
		log.debug("output:" + output);
		Set<String> keySet = siteFormatResult.keySet();
		for (String key : keySet) {
			JSONObject json = new JSONObject();
			json.put(key, siteFormatResult.get(key));
			log.debug("json:" + json);
			CategoryUtil.mergeKeys(json, output);
		}
		if (result.containsField("targetCategoryText")) {
			output.put("categoryName", result.get("targetCategoryText"));
		}
		return output;
	}

	private static void processItemSpecificsValues(JSONArray itemSpecifics, JSONObject siteFormatResult) {
		JSONArray mappedItemSpecifics = siteFormatResult.getJSONArray("itemSpecifics");
		for (int i = 0; i < itemSpecifics.length(); i++) {
			JSONObject itemSpecific = itemSpecifics.getJSONObject(i);
			boolean isItemAlreadyInMappedSpecific = false;
			for (int mappedSpecificIndex = 0; mappedSpecificIndex < mappedItemSpecifics
					.length(); mappedSpecificIndex++) {
				JSONObject mappedItemSpecific = mappedItemSpecifics.getJSONObject(mappedSpecificIndex);
				if (itemSpecific.getString("title").equals(mappedItemSpecific.getString("title"))) {
					isItemAlreadyInMappedSpecific = true;
					break;
				}
			}
			if (!isItemAlreadyInMappedSpecific) {
				mappedItemSpecifics.put(itemSpecific);
			}
		}
	}
	@SuppressWarnings("unchecked")
	private static String getTargetCategoryID(DBObject result, JSONObject sourceFromRequest) {
		String categoryID = null;
		if (result.containsField("map")) {
			List<BasicDBObject> map = (List<BasicDBObject>) result.get("map");
			for (BasicDBObject mapEntry : map) {
				List<BasicDBObject> sourceList = (List<BasicDBObject>) mapEntry.get("source");
				BasicDBObject target = (BasicDBObject) mapEntry.get("target");
				if ("categoryID".equals(target.getString("field"))) {
					if (target.containsField("value")) {
						categoryID = target.getString("value");
					} else {
						// For the current use cases, only one object can be
						// present in the array. Handle multiple objects use
						// case in future as needed.
						BasicDBObject source = sourceList.get(0);
						categoryID = (String) getValueFromSource(source.getString("field"), sourceFromRequest);
					}
					break;
				}

			}
		}
		return categoryID;
	}

	private static List<DBObject> readDB(JSONObject jsonRequest, JSONArray standardFormatSource) {
		String searchKey = "";
		searchKey = getKeyFromSource(standardFormatSource, searchKey, true);
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
					json.put("value", CategoryUtil.getCSVFromJSONArray(valueArray));
					output.put(json);
				} else {
					for (int i = 0; i < valueArray.length(); i++) {
						String title = valueArray.getJSONObject(i).getString("title");
						JSONArray names = valueArray.getJSONObject(i).getJSONArray("names");
						JSONObject json = new JSONObject();
						json.put("field", key + "." + title);
						json.put("value", CategoryUtil.getCSVFromJSONArray(names));
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
		JSONObject output = new JSONObject();
		if (result.containsField("map")) {
			List<BasicDBObject> map = (List<BasicDBObject>) result.get("map");
			for (BasicDBObject mapEntry : map) {
				List<BasicDBObject> sourceList = (List<BasicDBObject>) mapEntry.get("source");
				BasicDBObject target = (BasicDBObject) mapEntry.get("target");
				String targetField = target.getString("field");
				Object targetValue = null;
				if (target.containsField("value")) {
					targetValue = target.get("value");
				} else {
					// For the current use cases, only one object can be present
					// in the array. Handle multiple objects use case in future
					// as needed.
					BasicDBObject source = sourceList.get(0);
					String sourceField = source.getString("field");
					// special handling for item specifics
					if (sourceField.startsWith("itemSpecifics")) {
						String itemSpecificTitle = sourceField.split("\\.")[1];
						targetValue = getNamesFromItemSpecifics(sourceFromRequest, itemSpecificTitle);
						if (!targetField.startsWith("itemSpecifics") && targetValue instanceof JSONArray) {
							targetValue = ((JSONArray) targetValue).get(0);
						}
					} else {
						targetValue = getValueFromSource(sourceField, sourceFromRequest);
					}
				}
				if (targetValue != null) {
					// special handling for item specifics
					if (targetField.startsWith("itemSpecifics")) {
						JSONObject itemSpecificsSiteFormat = new JSONObject();
						itemSpecificsSiteFormat.put("title", targetField.split("\\.")[1]);
						// targetValue can either be CSV string or JSON array
						if (targetValue instanceof String) {
							targetValue = CategoryUtil.getJSONArrayFromCSV((String) targetValue);
						}
						itemSpecificsSiteFormat.put("names", targetValue);
						JSONArray itemSpecifics = new JSONArray();
						if (output.has("itemSpecifics")) {
							itemSpecifics = output.getJSONArray("itemSpecifics");
						}
						itemSpecifics.put(itemSpecificsSiteFormat);
						output.put("itemSpecifics", itemSpecifics);
					} else {
						JSONObject json = CategoryUtil.getJSONObjectFromDotNotation(targetField, targetValue);
						String key = json.keys().next();
						output.put(key, json.get(key));
					}
				}

			}
		}
		return output;
	}

	private static JSONArray getNamesFromItemSpecifics(JSONObject sourceFromRequest, String itemSpecificTitle) {
		if (sourceFromRequest.has("itemSpecifics")) {
			JSONArray itemSpecifics = sourceFromRequest.getJSONArray("itemSpecifics");
			for (int i = 0; i < itemSpecifics.length(); i++) {
				if (itemSpecifics.getJSONObject(i).getString("title").equals(itemSpecificTitle)) {
					return itemSpecifics.getJSONObject(i).getJSONArray("names");
				}
			}
		}
		return null;
	}

	private static Object getValueFromSource(String field, JSONObject sourceFromRequest) {
		String[] fields = field.split("\\.", 2);
		if (fields.length == 2) {
			return getValueFromSource(fields[1], sourceFromRequest);
		} else {
			if (sourceFromRequest.has(fields[0])) {
				return sourceFromRequest.get(fields[0]);
			} else {
				return null;
			}
		}
	}

	public static BasicDBObject createMap(String request) {
		JSONObject jsonRequest = new JSONObject(request);
		BasicDBObject result = updateDB(jsonRequest, true);
		if (jsonRequest.getString("sourceNicknameId").split("-")[0].equals("kartrocket")
				|| jsonRequest.getString("targetNicknameId").split("-")[0].equals("kartrocket")) {
			return result;
		}
		if (!jsonRequest.getString("accountNumber").equals(CategoryUtil.DEFAULT_ACCOUNT_NUMBER)) {
			persistAccountGenericData(jsonRequest);
			//persistDefaultData(jsonRequest);
		}
		return result;
	}

	private static BasicDBObject updateDB(JSONObject jsonRequest, boolean returnResult) {
		String searchKey = "";
		JSONArray map = jsonRequest.getJSONArray("map");
		for (int i = 0; i < map.length(); i++) {
			searchKey = CategoryUtil.addDelimiter(i, searchKey, ",");
			JSONObject entry = map.getJSONObject(i);
			JSONArray source = entry.getJSONArray("source");
			searchKey = getKeyFromSource(source, searchKey, false);
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
		if (jsonRequest.getJSONArray("map").length() > 0) {
			updateDB(jsonRequest, false);
		}
	}

	private static void persistDefaultData(JSONObject jsonRequest) {
		jsonRequest.put("accountNumber", CategoryUtil.DEFAULT_ACCOUNT_NUMBER);
		if (jsonRequest.getJSONArray("map").length() > 0) {
			updateDB(jsonRequest, false);
		}
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
		query.put("accountNumber", jsonRequest.getString("accountNumber"));
		query.put("sourceNicknameId", jsonRequest.getString("sourceNicknameId"));
		query.put("sourceCountryCode", jsonRequest.getString("sourceCountryCode"));
		query.put("targetNicknameId", jsonRequest.getString("targetNicknameId"));
		query.put("targetCountryCode", jsonRequest.getString("targetCountryCode"));
		return query;
	}

	private static String getKeyFromSource(JSONArray source, String key, boolean strictSearch) {
		for (int j = 0; j < source.length(); j++) {
			key = CategoryUtil.addDelimiter(j, key, ",");
			JSONObject sourceItem = source.getJSONObject(j);
			String field = sourceItem.getString("field");
			String value = "";
			if (sourceItem.has("value")) {
				value = sourceItem.getString("value");
			}
			if (field.contains("variantDetails")) {
				try {
					JSONObject variantDetails = new JSONObject(value);
					key = replacePunctuationMarks(key, field + ".title", variantDetails.getString("title"),
							strictSearch);
					key = CategoryUtil.addDelimiter(j, key, ",");
					key = replacePunctuationMarks(key, field + ".name", variantDetails.getString("name"), strictSearch);
				} catch (Exception e) {
					key = replacePunctuationMarks(key, field, value, strictSearch);
				}
			} else {
				key = replacePunctuationMarks(key, field, value, strictSearch);
			}
		}
		return key;
	}

	private static String replacePunctuationMarks(String key, String field, String value, boolean strictSearch) {
		String enclosingChar = "";
		if (strictSearch && "categoryID".equalsIgnoreCase(field)) {
			enclosingChar = "\"";
		}
		// TODO: fix this to replace all punctuation marks with _. The list can
		// be found at
		// https://docs.mongodb.com/manual/core/index-text/#tokenization-delimiters.
		key = key + enclosingChar + field.replace(" ", "_").replace("+", "_").replace(".", "_").replace("\"", "") + "_";
		key = key + value.replace(" ", "_").replace("+", "_").replace(".", "_").replace("\"", "") + enclosingChar;
		return key;
	}

	public static Object constructMappedCategory(String request) throws IOException, JSONException {
		JSONObject requestObj = new JSONObject(request);
		JSONObject response = new JSONObject();
		List<String> mappedCategoryList = new ArrayList<String>();
		if (!requestObj.getString("accountNumber").equals(CategoryUtil.DEFAULT_ACCOUNT_NUMBER)) {
			response = getMappedCategory(requestObj, requestObj.getString("accountNumber"), mappedCategoryList);
			if (requestObj.getString("categoryType").equals("accountwiseUnmapped")) {
				response = getAccountSpecificUnmappedCategory(requestObj, mappedCategoryList);
			}
		} else {
			response = getMappedCategory(requestObj, CategoryUtil.DEFAULT_ACCOUNT_NUMBER, mappedCategoryList);
			if (requestObj.getString("categoryType").equals("defaultUnmapped")) {
				response = getDefaultUnmappedCategory(requestObj, mappedCategoryList);
			}
		}
		return response;
	}

	private static JSONObject getDefaultUnmappedCategory(JSONObject requestObj, List<String> mappedCategoryList)
			throws IOException, JSONException {
		List<BasicDBObject> unmappedCategory = new ArrayList<BasicDBObject>();
		int pageSize = requestObj.getInt("pageSize");
		int pageNumber = requestObj.getInt("pageNumber");
		String url = Config.getConfig().getSiaAdminUrl() + "/category/" + requestObj.getString("sourceChannel") + "/"
				+ requestObj.getString("sourceCountryCode") + ".json?_=" + System.currentTimeMillis();
		Map<String, String> config = new HashMap<String, String>();
		config.put("Content-Type", "application/json");
		org.codehaus.jettison.json.JSONObject res = HttpsURLConnectionUtil.doGet(url, config);
		if (res.getInt("httpCode") != 200) {
			return new JSONObject();
		}
		JSONArray categories = new JSONArray(res.getString("payload"));
		for (int i = 0; i < categories.length(); i++) {
			String categoryID = categories.getString(i).split("##")[1].trim();
			if (!mappedCategoryList.contains(categoryID)) {
				BasicDBObject categoryObj = new BasicDBObject();
				categoryObj.put("sourceCategoryID", categoryID);
				categoryObj.put("targetCategoryID", "");
				unmappedCategory.add(categoryObj);
			}
		}
		long count = unmappedCategory.size();
		long numberOfPages = ((count / pageSize) + (count % pageSize > 0 ? 1 : 0));
		int endIndex = (int) (((pageSize * pageNumber) > count) ? count : (pageSize * pageNumber));
		JSONObject response = new JSONObject();
		response.put("categories",
				new HashSet<DBObject>(unmappedCategory.subList((pageSize * (pageNumber - 1)), endIndex)));
		response.put("numberOfPages", numberOfPages);
		response.put("numberOfRecords", count);
		return response;
	}

	private static JSONObject getAccountSpecificUnmappedCategory(JSONObject requestObj,
			List<String> accountWiseMappedCategoryList) throws IOException, JSONException {
		List<BasicDBObject> unmappedCategory = new ArrayList<BasicDBObject>();
		int pageSize = requestObj.getInt("pageSize");
		int pageNumber = requestObj.getInt("pageNumber");
		JSONArray nickNameIds = requestObj.getJSONArray("sourceNickNameIds");
		Set<String> categories = new HashSet<String>();
		Map<String, String> config = new HashMap<String, String>();
		config.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		config.put("accountNumber", requestObj.getString("accountNumber"));
		config.put("Content-Type", "application/json");
		for (int i = 0; i < nickNameIds.length(); i++) {
			String url = Config.getConfig().getSiaInventoryUrl() + "/inventory/categories?nicknameID="
					+ nickNameIds.getString(i);
			org.codehaus.jettison.json.JSONObject response = HttpsURLConnectionUtil.doGet(url, config);
			if (response.getInt("httpCode") != 200) {
				continue;
			}
			JSONObject payload = new JSONObject(response.getString("payload"));
			if (payload.has("data") && payload.getJSONArray("data").length() > 0) {
				JSONArray data = payload.getJSONArray("data");
				for (int j = 0; j < data.length(); j++) {
					if(data.getString(j).contains("##")) {
						categories.add(data.getString(j).split("##")[1].trim());
					}
				}
			}
		}
		if (categories.size() == 0) {
			return new JSONObject();
		}
		for (String categoryID : categories) {
			if (!accountWiseMappedCategoryList.contains(categoryID)) {
				BasicDBObject categoryObj = new BasicDBObject();
				categoryObj.put("sourceCategoryID", categoryID);
				categoryObj.put("targetCategoryID", "");
				unmappedCategory.add(categoryObj);
			}
		}
		long count = unmappedCategory.size();
		long numberOfPages = ((count / pageSize) + (count % pageSize > 0 ? 1 : 0));
		int endIndex = (int) (((pageSize * pageNumber) > count) ? count : (pageSize * pageNumber));
		JSONObject response = new JSONObject();
		response.put("categories",
				new HashSet<DBObject>(unmappedCategory.subList((pageSize * (pageNumber - 1)), endIndex)));
		response.put("numberOfPages", numberOfPages);
		response.put("numberOfRecords", count);
		return response;
	}

	private static JSONObject getMappedCategory(JSONObject requestObj, String accountNumber,
			List<String> mappedCategoryList) {
		List<BasicDBObject> mappedCategory = new ArrayList<BasicDBObject>();
		int pageSize = requestObj.getInt("pageSize");
		int pageNumber = requestObj.getInt("pageNumber");
		DBCollection collection = DbUtilities.getLookupDBCollection("fieldsMap");
		BasicDBObject searchQuery = new BasicDBObject("accountNumber", accountNumber);
		searchQuery.put("sourceNicknameId", requestObj.getString("sourceChannel"));
		searchQuery.put("sourceCountryCode", requestObj.getString("sourceCountryCode"));
		searchQuery.put("targetNicknameId", requestObj.getString("targetChannel"));
		searchQuery.put("targetCountryCode", requestObj.getString("targetCountryCode"));
		searchQuery.put("map.source.field", "categoryID");
		BasicDBObject projection = new BasicDBObject();
		projection.put("_id", 0);
		projection.put("map.$", 1);
		List<DBObject> results = collection.find(searchQuery, projection).skip(pageSize * (pageNumber - 1))
				.limit(pageSize).toArray();
		for (DBObject resultObj : results) {
			BasicDBObject categoryObj = new BasicDBObject();
			ArrayList<BasicDBObject> mapList = (ArrayList<BasicDBObject>) resultObj.get("map");
			BasicDBObject mapObj = mapList.get(0);
			ArrayList<BasicDBObject> sourceList = (ArrayList<BasicDBObject>) mapObj.get("source");
			String sourceCategoryID = ((BasicDBObject) sourceList.get(0)).getString("value");
			String targetCategoryID = ((BasicDBObject) mapObj.get("target")).getString("value");
			categoryObj.put("sourceCategoryID", sourceCategoryID);
			categoryObj.put("targetCategoryID", targetCategoryID);
			mappedCategoryList.add(sourceCategoryID);
			mappedCategory.add(categoryObj);
		}
		long count = collection.count(searchQuery);
		long numberOfPages = ((count / pageSize) + (count % pageSize > 0 ? 1 : 0));
		JSONObject response = new JSONObject();
		response.put("categories", mappedCategory);
		response.put("numberOfPages", numberOfPages);
		response.put("numberOfRecords", count);
		return response;
	}

}
