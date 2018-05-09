package com.sellinall.listinglookup.lazada;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

public class CategoryLookup {
	static Logger log = Logger.getLogger(CategoryLookup.class.getName());
	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId, String accountNumber,
			String nickNameID) throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException {

		BasicDBObject lazadaAttributesDB = getCategoryAttributesFromDB(countryCode, categoryId);
		if (lazadaAttributesDB != null) {
			return lazadaAttributesDB;
		}
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil.doGet(Config.getConfig().getLazadaURL()
				+ "/category/" + categoryId + "/attributes?nickNameID=" + nickNameID, header);
		JSONArray attributes = new JSONArray();
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject(serviceResponse.getString("payload"));
			if (response.has("SuccessResponse")) {
				JSONObject successResponse = (JSONObject) response.get("SuccessResponse");
				attributes = successResponse.getJSONArray("Body");
			} else if (response.has("code") && response.getString("code").equals("0")) {
				attributes = response.getJSONArray("data");
			}
		}
		return persistToDB(countryCode, categoryId, attributes);
	}

	private static BasicDBObject getCategoryAttributesFromDB(String countryCode, String categoryId) {
		BasicDBObject filterField1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filterField2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);
		and.add(filterField2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		DBCollection table = DbUtilities.getLookupDBCollection("lazadaAttributeLookup");
		BasicDBObject lookupData = (BasicDBObject) table.findOne(searchQuery);
		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			BasicDBObject attributesData = null;
			if (currentTime < expiryTime) {
				attributesData = new BasicDBObject();
				if (lookupData.containsKey("variants")) {
					attributesData.put("variants", lookupData.get("variants"));
				}
				attributesData.put("attributes", lookupData.get("attributes"));
			}
			return attributesData;
		}
		return lookupData;
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONArray lazadaAttributes)
			throws JSONException {
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
		JSONArray variants = constructVariantsAndUpdateKeyValues(lazadaAttributes);
		if (variants.length() != 0) {
			updateData.put("variants", JSON.parse(variants.toString()));
		}
		updateData.put("attributes", JSON.parse(lazadaAttributes.toString()));
		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("lazadaAttributeLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

	private static JSONArray constructVariantsAndUpdateKeyValues(JSONArray lazadaAttributes) throws JSONException {
		JSONArray variations = new JSONArray();
		String attributeType = "";
		String inputType = "";
		for (int i = 0; i < lazadaAttributes.length(); i++) {
			JSONObject filterFields = lazadaAttributes.getJSONObject(i);
			if (filterFields.has("attribute_type")) {
				attributeType = filterFields.getString("attribute_type");
			} else if (filterFields.has("attributeType")) {
				attributeType = filterFields.getString("attributeType");
			}
			if (filterFields.has("input_type")) {
				inputType = filterFields.getString("input_type");
			} else if (filterFields.has("inputType")) {
				inputType = filterFields.getString("inputType");
			}
			if (attributeType.equals("sku") && (inputType.equals("singleSelect") || inputType.equals("multiSelect")
					|| inputType.equals("multiEnumInput"))) {
				variations.put(filterFields.get("name"));
			}
			if (filterFields.get("name").equals("warranty_type")) {
				filterFields.put("name", "warrantyType");
			}
		}
		return variations;
	}
}
