package com.sellinall.listinglookup.lazada;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

public class BuildBrand {

	public static String buildBrand(String accountNumber, String nickNameID) throws IOException, JSONException {
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		boolean nextPageAvailable = true;
		int loopCount = 0;
		int pageLimit = 1000;
		long offset = 0;
		String brandList = "";
		BasicDBList overAllResponse = new BasicDBList();
		do {
			if (loopCount != 0) {
				offset = offset + pageLimit;
			}
			nextPageAvailable = false;
			JSONObject serviceResponse = HttpsURLConnectionUtil.doGet(Config.getConfig().getLazadaURL()
					+ "/brand?nickNameID=" + nickNameID + "&offset=" + offset + "&pageLimit=" + pageLimit, header);
			if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
				JSONObject response = new JSONObject(serviceResponse.getString("payload"));
				response = new JSONObject(response.getString("data"));
				if (response.has("code") && response.getString("code").equals("0")) {
					JSONArray data = (JSONArray) response.get("data");
					overAllResponse.addAll((BasicDBList) JSON.parse(data.toString()));
					if (data.length() == pageLimit) {
						nextPageAvailable = true;
					}
				}
			}
			loopCount++;
		} while (nextPageAvailable);
		brandList = parseAndPrintFile(overAllResponse);
		return brandList;
	}

	private static String parseAndPrintFile(BasicDBList overAllResponse) {
		JSONArray brandString = new JSONArray();
		for (int i = 0; i < overAllResponse.size(); i++) {
			BasicDBObject brand = (BasicDBObject) overAllResponse.get(i);
			if (brand.containsField("brand_id")) {
				if (brand.containsField("name_en")) {
					brandString.put(brand.getString("name_en") + " ## " + brand.getString("brand_id"));
				} else if (brand.containsField("name")) {
					brandString.put(brand.getString("name") + " ## " + brand.getString("brand_id"));
				}
			}
		}
		return brandString.toString();
	}
}
