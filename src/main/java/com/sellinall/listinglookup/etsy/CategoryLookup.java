package com.sellinall.listinglookup.etsy;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;

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
		JSONObject categorySpecificsEtsy = getCategorySpecificsFromEtsy(countryCode, categoryId);
		BasicDBObject persistData = new BasicDBObject();
		if (categorySpecificsEtsy.length() != 0) {
			persistData = persistToDB(countryCode, categoryId, categorySpecificsEtsy);
		}
		return persistData;
	}

	private static BasicDBObject getCategorySpecificsFromDB(String countryCode, String categoryId) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);

		DBCollection table = DbUtilities.getLookupDBCollection("etsyCategoryLookup");
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

	private static JSONObject getCategorySpecificsFromEtsy(String countryCode, String categoryId)
			throws JSONException, IOException, TemplateException {
		JSONObject categorySpecifics = new JSONObject();
		String accountNumber = Config.getConfig().getEtsyAccountDetails(countryCode);
		String nickNameId = Config.getConfig().getEtsyNickNameID(countryCode);
		BasicDBObject accountInformation = getUserDetailsFromUser(accountNumber, nickNameId);
		if (accountInformation != null) {
			categorySpecifics = EtsyUtil.getCategorySpecifics(accountInformation, countryCode, categoryId);
		}
		return categorySpecifics;
	}

	private static BasicDBObject getUserDetailsFromUser(String accountNumber, String nickNameId) {
		BasicDBObject elemMatch = new BasicDBObject();
		String siteName = nickNameId.split("-")[0];
		elemMatch.put("nickName.id", nickNameId);
		BasicDBObject site = new BasicDBObject("$elemMatch", elemMatch);
		BasicDBObject searchQuery = new BasicDBObject(siteName, site);
		searchQuery.put("_id", new ObjectId(accountNumber));
		BasicDBObject fields = new BasicDBObject("etsy.$", 1);
		DBCollection table = DbUtilities.getUserDBCollection("accounts");
		BasicDBObject user = (BasicDBObject) table.findOne(searchQuery, fields);
		if (user == null) {
			log.error("no data found in the accounts collection " + accountNumber + " nickName " + nickNameId);
			return user;
		}
		BasicDBList channelList = (BasicDBList) user.get(siteName);
		// Here always return single object only
		return (BasicDBObject) channelList.get(0);
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONObject itemSpecifics) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);

		long expriyTime = (System.currentTimeMillis() / 1000L) + thirtyDays;
		BasicDBObject updateData = new BasicDBObject();
		updateData.put("expiryTime", expriyTime);
		updateData.put("itemSpecifics", JSON.parse(itemSpecifics.toString()));
		updateData.put("expiryTime", expriyTime);
		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("etsyCategoryLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}
