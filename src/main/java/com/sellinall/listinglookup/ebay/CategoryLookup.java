package com.sellinall.listinglookup.ebay;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CategoryLookup {
	public static JSONObject getCategorySpecifics(String countryCode, String categoryId) {

		/*
		 * TODO: Below is a sample code. Change the logic to get this from db.
		 * If not in db, get from ebay API, store in db and return the same.
		 */

		JSONObject res = new JSONObject();
		try {
			res.put("country", countryCode);
			res.put("categoryId", categoryId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return res;
	}
}
