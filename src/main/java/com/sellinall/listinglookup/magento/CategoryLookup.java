package com.sellinall.listinglookup.magento;

import java.io.IOException;
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
			String nickNameID) throws IOException, JSONException {

		BasicDBObject magentoAttributesDB = getCategoryAttributesFromDB(countryCode, categoryId);
		if (magentoAttributesDB != null) {
			return magentoAttributesDB;
		}
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil.doGet(
				Config.getConfig().getMagentoURL() + "/category/" + categoryId + "/attributes?nickNameID=" + nickNameID,
				header);
		JSONArray attributes = new JSONArray();
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject(serviceResponse.getString("payload"));
			if (response.has("payload")) {
				attributes = new JSONArray(response.getString("payload"));
			} else {
				return new BasicDBObject();
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

		DBCollection table = DbUtilities.getLookupDBCollection("magentoAttributeLookup");
		BasicDBObject lookupData = (BasicDBObject) table.findOne(searchQuery);
		if (lookupData != null) {
			long expiryTime = lookupData.getLong("expiryTime");
			long currentTime = (System.currentTimeMillis() / 1000L);
			BasicDBObject attributesData = new BasicDBObject();
			if (currentTime < expiryTime) {
				attributesData.put("attributes", lookupData.get("attributes"));
			}
			return attributesData;
		}
		return lookupData;
	}

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONArray attributes)
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
		updateData.put("attributes", JSON.parse(attributes.toString()));
		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("magentoAttributeLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}
}
