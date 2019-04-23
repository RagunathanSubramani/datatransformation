package com.sellinall.listinglookup.sme;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.EncryptionUtil;
import com.sellinall.util.HttpsURLConnectionUtil;

public class BuildCategory {
	static Logger log = Logger.getLogger(BuildCategory.class.getName());

	public static Object buildNewCategoryList(String countryCode) throws JSONException, IOException {
		JSONArray parsedCategorylist = new JSONArray();
		JSONArray categoryList = getCategoryList(countryCode);
		if (categoryList.length() > 0) {
			parseCategory("", 0, categoryList, parsedCategorylist);
		}
		return parsedCategorylist;
	}

	private static void parseCategory(String categoryName, int categoryID, JSONArray categories,
			JSONArray parsedCategorylist) throws JSONException {
		Boolean isLastLeaf = true;
		String appendCategory = "";
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			if (category.has("processed")) {
				continue;
			}
			if (category.getInt("parent_id") == categoryID) {
				isLastLeaf = false;
				appendCategory = categoryName + (categoryName.isEmpty() ? "" : ":") + category.getString("category");
				category.put("processed", true);
				parseCategory(appendCategory, category.getInt("category_id"), categories, parsedCategorylist);

			}
		}
		if (isLastLeaf) {
			parsedCategorylist.put(categoryName + " ## " + categoryID);
		}
	}

	private static JSONArray getCategoryList(String countryCode) throws JSONException, IOException {
		String accountNumber = Config.getConfig().get99SMEAccountDetails(countryCode);
		String nickNameId = Config.getConfig().get99SMENickNameID(countryCode);
		return getCategoryListFromSME(accountNumber,nickNameId);
	}

	public static JSONArray getCategoryListFromSME(String accountNumber, String nickNameID) throws JSONException, IOException {
		JSONArray categories = new JSONArray();
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil
				.doGet(Config.getConfig().getSiaSmeUrl() + "/categories?nickNameID=" + nickNameID, header);
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject payload = new JSONObject(serviceResponse.getString("payload"));
			categories = payload.getJSONArray("categoryList");
		}
		return categories;
	}

	public static BasicDBObject getAccountDetails(String accountNumber, String nickNameId) {
		BasicDBObject elemMatch = new BasicDBObject();
		String siteName = nickNameId.split("-")[0];
		elemMatch.put("nickName.id", nickNameId);
		BasicDBObject site = new BasicDBObject("$elemMatch", elemMatch);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("_id", new ObjectId(accountNumber));
		searchQuery.put(siteName, site);
		BasicDBObject projection = new BasicDBObject("99SME.$", 1);
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
}
