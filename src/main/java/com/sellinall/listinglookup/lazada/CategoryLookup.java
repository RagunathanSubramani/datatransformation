package com.sellinall.listinglookup.lazada;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategoryLookup {
	static Logger log = Logger.getLogger(CategoryLookup.class.getName());
	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId,String accountNumber, String nickNameID) throws KeyManagementException, NoSuchAlgorithmException, IOException {

		BasicDBObject lazadaAttributesDB = getCategoryAttributesFromDB(countryCode, categoryId);
		if (lazadaAttributesDB != null) {
			return lazadaAttributesDB;
		}
		JSONArray lazadaAttributes = getAttributesFromLazada(countryCode, categoryId, accountNumber, nickNameID);
		return persistToDB(countryCode, categoryId, lazadaAttributes);
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

	private static JSONArray getAttributesFromLazada(String countryCode, String categoryId, String accountNumber,
			String nickNameId) throws KeyManagementException, NoSuchAlgorithmException, IOException {
		boolean useNewApi = Config.getConfig().getUseNewLazadaApi();
		JSONArray attributes = new JSONArray();
		String categorySpecifics = "";
		BasicDBObject userChannel = getUserDetailsFromUser(accountNumber, nickNameId);
		if(useNewApi) {
			String hostUrl = Config.getLazadaAPIUrl(userChannel.getString("countryCode"));
			categorySpecifics = RocketEcomConnectionUtil.getCategorySpecificsFromNewApi(categoryId, hostUrl);
			JSONObject categorySpecificsFromLazada = new JSONObject(categorySpecifics);
			log.debug(categorySpecificsFromLazada);
			attributes = categorySpecificsFromLazada.getJSONArray("data");
		} else {
			String categorySpecificsXML = RocketEcomConnectionUtil.getCategorySpecifics(countryCode, categoryId,
					userChannel.getString("userID"), userChannel.getString("apikey"), userChannel.getString("hostURL"));
			JSONObject categorySpecificsFromLazada = new JSONObject(categorySpecificsXML);
			log.debug(categorySpecificsFromLazada);
			JSONObject successResponse = categorySpecificsFromLazada.getJSONObject("SuccessResponse");
			attributes = successResponse.getJSONArray("Body");
		}
		log.debug(attributes);
		return attributes;
	}
	
	public static BasicDBObject getUserDetailsFromUser(String accountNumber, String nickNameId) {
		BasicDBObject elemMatch = new BasicDBObject();
		String siteName = nickNameId.split("-")[0];
		elemMatch.put("nickName.id", nickNameId);
		BasicDBObject site = new BasicDBObject("$elemMatch", elemMatch);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("_id", new ObjectId(accountNumber));
		searchQuery.put(siteName, site);
		BasicDBObject fields = new BasicDBObject("lazada.$", 1);
		DBCollection table = DbUtilities.getUserDBCollection("accounts");
		BasicDBObject user = (BasicDBObject) table.findOne(searchQuery, fields);
		BasicDBList channelList = (BasicDBList) user.get(siteName);
		//Here always return single object only 
		return (BasicDBObject) channelList.get(0);
	}



	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONArray lazadaAttributes) {
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

	private static JSONArray constructVariantsAndUpdateKeyValues(JSONArray lazadaAttributes) {
		JSONArray variations = new JSONArray();
		String attributeType = "";
		String inputType = "";
		for (int i = 0; i < lazadaAttributes.length(); i++) {
			JSONObject filterFields = lazadaAttributes.getJSONObject(i);
			if(filterFields.has("attribute_type")) {
				attributeType = filterFields.getString("attribute_type");
			} else if(filterFields.has("attributeType")) {
				attributeType = filterFields.getString("attributeType");
			}
			if(filterFields.has("input_type")) {
				inputType = filterFields.getString("input_type");
			} else if(filterFields.has("inputType")) {
				inputType = filterFields.getString("inputType");
			}
			if (attributeType.equals("sku") && (inputType.equals("singleSelect")
					|| inputType.equals("multiSelect") || inputType.equals("multiEnumInput"))) {
				variations.put(filterFields.get("name"));
			}
			if(filterFields.get("name").equals("warranty_type")){
				filterFields.put("name", "warrantyType");
			}
		}
		return variations;
	}
}
