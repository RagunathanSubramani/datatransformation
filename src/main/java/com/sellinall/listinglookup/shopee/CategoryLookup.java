package com.sellinall.listinglookup.shopee;

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

/**
 * 
 * @author Raguvaran
 *
 */

public class CategoryLookup {

	static Logger log = Logger.getLogger(CategoryLookup.class.getName());

	private static final long SEVEN_DAYS = 7 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId)
			throws JSONException, IOException, TemplateException {
		BasicDBObject categorySpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId);

		if (categorySpecificsDB != null) {
			return categorySpecificsDB;
		}
		JSONObject categorySpecificsShopee = getCategorySpecificsFromShopee(countryCode, categoryId);
		JSONObject payload = new JSONObject(categorySpecificsShopee.getString("payload"));
		if (categorySpecificsShopee.getInt("httpCode") == 200) {
			if (payload.length() != 0 && !payload.has("error") && !payload.has("msg")) {
				return persistToDB(countryCode, categoryId,
						new JSONObject(categorySpecificsShopee.getString("payload")));
			}
		}
		return JSON.parse(payload.toString());
	}

	private static BasicDBObject getCategorySpecificsFromDB(String countryCode, String categoryId) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);
		DBCollection table = DbUtilities.getLookupDBCollection("shopeeCategoryLookup");
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

	private static JSONObject getCategorySpecificsFromShopee(String countryCode, String categoryId)
			throws JSONException, IOException, TemplateException {
		JSONObject categorySpecifics = new JSONObject();
		String accountNumber = Config.getConfig().getShopeeAccountDetails(countryCode);
		String nickNameId = Config.getConfig().getShopeeNickNameID(countryCode);
		BasicDBObject accountInformation = getAccountDetails(accountNumber, nickNameId);
		log.info("accountInformation:"+accountInformation);
		if (accountInformation != null) {
			categorySpecifics = ShopeeUtil.getCategorySpecifics(accountInformation, countryCode, categoryId);
		}
		log.info("response:"+categorySpecifics);
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
		BasicDBObject projection = new BasicDBObject("shopee.$", 1);
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

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONObject itemSpecifics) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);
		long expiryTime = (System.currentTimeMillis() / 1000L) + SEVEN_DAYS;
		BasicDBObject updateData = new BasicDBObject();
		updateData.put("itemSpecifics", JSON.parse(itemSpecifics.toString()));
		updateData.put("expiryTime", expiryTime);
		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("shopeeCategoryLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}