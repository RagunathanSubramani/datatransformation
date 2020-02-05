package com.sellinall.listinglookup.blibli;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

public class CategoryLookup {

	private static final long thirtyDays = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId, String accountNumber,
			String nickNameID) throws JSONException, IOException {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);
		BasicDBObject blibliAttributesDB = getCategoryAttributesFromDB(searchQuery);
		if (blibliAttributesDB != null) {
			return blibliAttributesDB;
		}
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		String url = Config.getConfig().getSiaBlibliUrl() + "/category/" + categoryId + "/attributes?nickNameID="
				+ nickNameID;
		JSONObject serviceResponse = HttpsURLConnectionUtil.doGet(url, header);
		JSONArray attributes = new JSONArray();
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject();
			if (serviceResponse.has("payload")) {
				response = new JSONObject(serviceResponse.getString("payload"));
				attributes = response.getJSONArray("attributes");
			} else {
				return new BasicDBObject();
			}
		}
		return persistToDB(searchQuery, attributes);
	}

	private static Object persistToDB(BasicDBObject searchQuery, JSONArray attributes) {
		long expiryTime = (System.currentTimeMillis() / 1000L) + thirtyDays;
		BasicDBObject updateData = new BasicDBObject();
		updateData.put("expiryTime", expiryTime);
		updateData.put("attributes", JSON.parse(attributes.toString()));
		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("blibliAttributeLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

	private static BasicDBObject getCategoryAttributesFromDB(BasicDBObject searchQuery) {
		DBCollection table = DbUtilities.getLookupDBCollection("blibliAttributeLookup");
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

}
