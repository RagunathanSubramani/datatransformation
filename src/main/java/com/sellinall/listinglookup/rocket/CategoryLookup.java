package com.sellinall.listinglookup.rocket;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategoryLookup {
	static Logger log = Logger.getLogger(CategoryLookup.class.getName());
	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId,String accountNumber, String nickNameID) {

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
				attributesData.put("attributes", lookupData.get("attributes"));
			}
			return attributesData;
		}
		return lookupData;
	}

	private static JSONArray getAttributesFromLazada(String countryCode, String categoryId, String accountNumber,
			String nickNameId) {
		BasicDBObject userChannel = getUserDetailsFromUser(accountNumber, nickNameId);
		String categorySpecificsXML = RocketEcomConnectionUtil.getCategorySpecifics(countryCode, categoryId,
				userChannel.getString("userID"), userChannel.getString("apikey"), userChannel.getString("hostURL"));

		JSONObject categorySpecificsFromLazada = new JSONObject(categorySpecificsXML);
		log.debug(categorySpecificsFromLazada);
		JSONObject successResponse = categorySpecificsFromLazada.getJSONObject("SuccessResponse");
		JSONArray attributes = successResponse.getJSONArray("Body");
		log.debug(attributes);
		return attributes;
	}
	
	private static BasicDBObject getUserDetailsFromUser(String accountNumber, String nickNameId) {
		BasicDBObject elemMatch = new BasicDBObject();
		String siteName = nickNameId.split("-")[0];
		elemMatch.put("nickName.id", nickNameId);
		BasicDBObject site = new BasicDBObject("$elemMatch", elemMatch);
		BasicDBObject searchQuery = new BasicDBObject(siteName, site);
		searchQuery.put("_id", new ObjectId(accountNumber));
		BasicDBObject fields = new BasicDBObject("lazada.$", 1);
		DBCollection table = DbUtilities.getUserDBCollection("user");
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
		updateData.put("attributes", JSON.parse(lazadaAttributes.toString()));

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("lazadaAttributeLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}
