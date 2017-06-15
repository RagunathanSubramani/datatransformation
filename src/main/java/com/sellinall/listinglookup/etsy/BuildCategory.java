package com.sellinall.listinglookup.etsy;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class BuildCategory {
	public static ArrayList<String> categoryList = new ArrayList<String>();

	public static ArrayList<String> buildNewCategoryList(String countryCode) throws JSONException, IOException {
		JSONObject listingResponse = submitEtsyCategory();
		JSONArray result = listingResponse.getJSONArray("results");
		for (int i = 0; i < result.length(); i++) {
			parseChildren("", result.getJSONObject(i), categoryList);
		}
		return categoryList;
	}

	private static String parseChildren(String header, JSONObject inputObject, ArrayList<String> output)
			throws JSONException, IOException {
		JSONObject child = inputObject;
		String category = null;
		if (header.isEmpty()) {
			category = "\"" + header + child.getString("name") + " ## " + child.getInt("id") + "\"";
			header = "\"" + child.getString("name");
			output.add(category);
		}
		if (child.has("children")) {
			JSONArray siblings = child.getJSONArray("children");
			int childCount = siblings.length();
			for (int j = 0; j < childCount; j++) {
				child = siblings.getJSONObject(j);
				category = child.getString("name") + " ## " + child.getInt("id");
				String constructedValue = header + "/" + category + "\"";
				output.add(constructedValue);
				if (child.has("children")) {
					parseChildren(header + "/" + child.getString("name"), child, output);
				}
			}
		}
		return header;
	}

	private static JSONObject submitEtsyCategory() throws IOException, JSONException {
		String consumerKey = "091by7ebcodwbccst3xy3vwe";
		String consumerSecret = "08t2gbxwcd";
		String oauthToken = "b11dfbadd2b1d3d8b536f61d110ef1";
		String oauthTokenSecret = "5e9bbb6692";
		String url = "https://openapi.etsy.com/v2/taxonomy/seller/get";
		HttpResponse httpResponse = EtsyUtil.submitGetRequest(url, consumerKey, consumerSecret, null, oauthToken,
				oauthTokenSecret);
		JSONObject response = EtsyUtil.getJSONResponse(httpResponse);
		return response;
	}

}