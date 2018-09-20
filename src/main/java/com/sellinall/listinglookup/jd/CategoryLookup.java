package com.sellinall.listinglookup.jd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

public class CategoryLookup {

	static Logger log = Logger.getLogger(CategoryLookup.class.getName());
	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId, String accountNumber,
			String nickNameID) throws IOException, JSONException {

		BasicDBObject jdAttributesDB = getCategoryAttributesFromDB(countryCode, categoryId);
		if (jdAttributesDB != null) {
			return jdAttributesDB;
		}
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil.doGet(
				Config.getConfig().getJdURL() + "/category/" + categoryId + "/attributes?nickNameID=" + nickNameID,
				header);
		JSONObject attributes = new JSONObject();
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			attributes = new JSONObject(serviceResponse.getString("payload"));
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

		DBCollection table = DbUtilities.getLookupDBCollection("jdAttributeLookup");
		BasicDBObject lookupData = (BasicDBObject) table.findOne(searchQuery);
		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			BasicDBObject attributesData = new BasicDBObject();
			if (currentTime < expiryTime) {
				attributesData.put("itemSpecifics", lookupData.get("attributes"));
			}
			return attributesData;
		}
		return lookupData;
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONObject jdAttributes)
			throws JSONException {
		BasicDBObject filterField1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filterField2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);
		and.add(filterField2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		long expriyTime = (System.currentTimeMillis() / 1000L) + thirtyDays;
		BasicDBObject updateData = BasicDBObject.parse(jdAttributes.toString());
		updateData.put("expiryTime", expriyTime);
		updateData.put("itemSpecifics", BasicDBObject.parse(jdAttributes.toString()));
		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("jdAttributeLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}
}
