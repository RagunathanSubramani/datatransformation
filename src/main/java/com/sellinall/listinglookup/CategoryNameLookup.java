package com.sellinall.listinglookup;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.database.DbUtilities;
import com.sellinall.util.HttpURLConnectionUtil;

public class CategoryNameLookup {
	static Logger log = Logger.getLogger(CategoryNameLookup.class.getName());

	public static Object getCategoryNameFromSIA(String countryCode, String categoryID, String channelName,
			String nicknameId, String accountNumber) throws IOException {
		String categoryName = "";
		String url = Config.getConfig().getSIACategoryNameUrl() + "?channelName=" + channelName + "&countryCode="
				+ countryCode + "&categoryID=" + categoryID;
		if (channelName.equals("shopify")) {
			String merchantID = null;
			DBObject result = getMerchantID(accountNumber);
			if (result != null && result.containsField("merchantID")) {
				merchantID = (String) result.get("merchantID");
			}
			url += "&merchantID=" + merchantID + "&nicknameID=" + nicknameId;
		}
		String responseObj = HttpURLConnectionUtil.doGet(url);
		if (!responseObj.isEmpty()) {
			categoryName = responseObj;
		}
		JSONObject response = new JSONObject();
		response.put("categoryNamePath", categoryName);
		return response;
	}

	private static DBObject getMerchantID(String accountNumber) {
		DBCollection table = DbUtilities.getUserDBCollection("accounts");
		BasicDBObject query = new BasicDBObject();
		BasicDBObject fields = new BasicDBObject();
		fields.put("merchantID", 1);
		fields.put("_id", 0);
		query.put("_id", new ObjectId(accountNumber));
		return table.findOne(query, fields);
	}
}
