package com.sellinall.listinglookup.elevenStreet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.json.XML;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;
import com.sellinall.util.HttpsURLConnectionUtil;

import freemarker.template.TemplateException;

public class CategoryLookup {

	static Logger log = Logger.getLogger(CategoryLookup.class.getName());

	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId)
			throws JSONException, IOException, TemplateException {
		BasicDBObject categorySpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId);

		if (categorySpecificsDB != null) {
			return categorySpecificsDB;
		}
		JSONObject categorySpecificsElevenStreet = getCategorySpecificsFromElevenStreet(countryCode, categoryId);
		JSONObject payload = new JSONObject();
		if (categorySpecificsElevenStreet.getInt("httpCode") == 200) {
			payload = new JSONObject(XML.toJSONObject(categorySpecificsElevenStreet.getString("payload")).toString());
			JSONObject AttributeMessage = payload.getJSONObject("AttributeMessage");
			if (AttributeMessage.getInt("resultCode") == HttpStatus.OK_200) {
				return persistToDB(countryCode, categoryId, payload);
			} else {
				payload = new JSONObject();
				payload.put("resultCode", AttributeMessage.getInt("resultCode"));
				payload.put("message", AttributeMessage.getString("message"));
				return JSON.parse(payload.toString());
			}
		} else {
			payload.put("resultCode", 500);
			payload.put("message", "Internal Error");
			return JSON.parse(payload.toString());
		}
	}

	private static BasicDBObject getCategorySpecificsFromDB(String countryCode, String categoryId) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);
		DBCollection table = DbUtilities.getLookupDBCollection("elevenStreetCategoryLookup");
		BasicDBObject lookupData = (BasicDBObject) table.findOne(searchQuery);
		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			BasicDBObject categoryData = null;
			if (currentTime < expiryTime) {
				categoryData = new BasicDBObject();
				categoryData.put("itemSpecifics", lookupData.get("itemSpecifics"));
			}
			return categoryData;
		}
		return lookupData;
	}

	private static JSONObject getCategorySpecificsFromElevenStreet(String countryCode, String categoryId)
			throws JSONException, IOException, TemplateException {
		JSONObject categorySpecifics = new JSONObject();
		String accountNumber = Config.getConfig().getElevenStreetAccountDetails(countryCode);
		String nickNameId = Config.getConfig().getElevenStreetNickNameID(countryCode);
		BasicDBObject accountInformation = getAccountDetails(accountNumber, nickNameId);
		if (accountInformation != null) {
			categorySpecifics = getCategorySpecifics(accountInformation, countryCode, categoryId);
		}
		return categorySpecifics;
	}

	public static BasicDBObject getAccountDetails(String accountNumber, String nickNameId) {
		BasicDBObject elemMatch = new BasicDBObject();
		String siteName = nickNameId.split("-")[0];
		elemMatch.put("nickName.id", nickNameId);
		BasicDBObject site = new BasicDBObject("$elemMatch", elemMatch);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("_id", new ObjectId(accountNumber));
		searchQuery.put(siteName, site);
		BasicDBObject projection = new BasicDBObject("elevenStreet.$", 1);
		DBCollection table = DbUtilities.getUserDBCollection("accounts");
		BasicDBObject account = (BasicDBObject) table.findOne(searchQuery, projection);
		if (account == null) {
			log.error("no data found in the accounts collection for accountNumber: " + accountNumber
					+ " and nickNameId: " + nickNameId);
			return account;
		}
		BasicDBList channelList = (BasicDBList) account.get(siteName);
		// Here always return single object only
		return (BasicDBObject) channelList.get(0);
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONObject itemSpecifics)
			throws JSONException {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);
		long expiryTime = (System.currentTimeMillis() / 1000L) + thirtyDays;
		BasicDBObject updateData = new BasicDBObject();
		JSONObject AttributeMessage = itemSpecifics.getJSONObject("AttributeMessage");
		updateData.put("resultCode", AttributeMessage.getInt("resultCode"));
		AttributeMessage.remove("resultCode");
		updateData.put("message", AttributeMessage.getString("message"));
		AttributeMessage.remove("message");
		updateData.put("itemSpecifics", JSON.parse(itemSpecifics.toString()));
		updateData.put("expiryTime", expiryTime);
		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("elevenStreetCategoryLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

	public static JSONObject getCategorySpecifics(BasicDBObject accountInformation, String countryCode,
			String categoryId) throws JSONException, IOException, TemplateException {
		String url = Config.getConfig().getElevenStreetUrl() + "prodservices/product-attributes/get/" + categoryId;
		Map<String, String> config = new HashMap<String, String>();
		config.put("Accept-Charset", "UTF-8");
		config.put("Content-Type", "application/xml");
		config.put("openapikey", accountInformation.getString("apiKey"));
		JSONObject response = HttpsURLConnectionUtil.doGet(url, config);
		return response;
	}

}