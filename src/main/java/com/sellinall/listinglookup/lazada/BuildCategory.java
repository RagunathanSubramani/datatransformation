package com.sellinall.listinglookup.lazada;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

public class BuildCategory {
	String totalString = "";

	public String buildNewCategiryList(String accountNumber, String nickNameID) throws IOException, JSONException {
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil
				.doGet(Config.getConfig().getLazadaURL() + "/categories?nickNameID=" + nickNameID, header);
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject(serviceResponse.getString("payload"));
			if (response.has("SuccessResponse")) {
				JSONObject successResponse = (JSONObject) response.get("SuccessResponse");
				parseAndPrintFile("", (JSONArray) successResponse.get("Body"));
			} else if (response.has("code") && response.getString("code").equals("0")) {
				parseAndPrintFile("", (JSONArray) response.get("data"));
			}
			totalString = totalString.substring(0, totalString.length() - 1);
			return "[" + totalString + "]";
		}
		return "";
	}

	private void parseAndPrintFile(String categoryName, JSONArray jsonArray) throws JSONException {
		categoryName = categoryName.replaceAll("&amp;", "&");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject json = jsonArray.getJSONObject(i);
			String childCategoryName = (String) json.get("name");
			String childCategoryId = "";
			if (json.has("categoryId")) {
				childCategoryId = "" + json.get("categoryId");
			} else {
				childCategoryId = "" + json.get("category_id");
			}
			if (json.has("children")) {
				JSONArray childJSON = (JSONArray) json.get("children");
				childCategoryName = (String) json.get("name");
				if (json.has("categoryId")) {
					childCategoryId = "" + json.get("categoryId");
				} else {
					childCategoryId = "" + json.get("category_id");
				}
				if (childJSON.length() != 0) {
					if (categoryName.length() == 0) {
						parseAndPrintFile(childCategoryName, childJSON);
					} else {
						parseAndPrintFile(categoryName + ":" + childCategoryName, childJSON);
					}
				} else {
					totalString = "\"" + categoryName + ":" + childCategoryName + " ## " + childCategoryId + "\" ,"
							+ totalString;
				}

			} else {
				totalString = "\"" + categoryName + ":" + childCategoryName + " ## " + childCategoryId + "\" ,"
						+ totalString;
			}

		}
	}

}
