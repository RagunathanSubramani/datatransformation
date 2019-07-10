package com.sellinall.listinglookup.tokopedia;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
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

import freemarker.template.TemplateException;

/**
 *
 * @author Ahamed
 *
 */

public class CategoryLookup {

	static Logger log = Logger.getLogger(CategoryLookup.class.getName());
	private static final long THIRTY_DAYS = 30 * 24 * 60 * 60;

	public static Object getCategorySpecifics(String countryCode, String categoryId, String accountNumber,
			String nickNameID) throws JSONException, IOException, TemplateException {
		BasicDBObject categorySpecificsDB = getCategorySpecificsFromDB(countryCode, categoryId);

		if (categorySpecificsDB != null) {
			return categorySpecificsDB;
		}
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil.doGet(Config.getConfig().getSiaTokopediaUrl() + "/category/"
				+ categoryId + "/attributes?nickNameID=" + nickNameID, header);
		JSONArray data = new JSONArray();
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject(serviceResponse.getString("payload"));
			if (response.has("data")) {
				data = response.getJSONArray("data");
			}
		}
		if (data.length() > 0) {
			JSONObject itemSpecifics = new JSONObject();
			itemSpecifics.put("data", data);
			return persistToDB(countryCode, categoryId, itemSpecifics);
		} else {
			return new JSONObject().put("data", data);
		}
	}

	private static BasicDBObject getCategorySpecificsFromDB(String countryCode, String categoryId) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);
		DBCollection table = DbUtilities.getLookupDBCollection("tokopediaCategoryLookup");
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

	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONObject itemSpecifics) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("countryCode", countryCode);
		searchQuery.put("categoryId", categoryId);
		long expriyTime = (System.currentTimeMillis() / 1000L) + THIRTY_DAYS;
		BasicDBObject updateData = new BasicDBObject();
		updateData.put("expiryTime", expriyTime);
		updateData.put("itemSpecifics", JSON.parse(itemSpecifics.toString()));
		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("tokopediaCategoryLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}
}
