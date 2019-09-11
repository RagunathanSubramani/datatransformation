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

public class BuildBrand {

	public static String buildBrand(String accountNumber, String nickNameID) throws IOException, JSONException {
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil
				.doGet(Config.getConfig().getLazadaURL() + "/brand?nickNameID=" + nickNameID, header);
		String brandList = "";
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject(serviceResponse.getString("payload"));
			response = new JSONObject(response.getString("data"));
			if (response.has("code") && response.getString("code").equals("0")) {
				brandList = parseAndPrintFile((JSONArray) response.get("data"));
			}
		}
		return brandList;
	}

	private static String parseAndPrintFile(JSONArray jsonArray) throws JSONException {
		JSONArray brandString = new JSONArray();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject brand = jsonArray.getJSONObject(i);
			if (brand.has("brand_id")) {
				if (brand.has("name_en")) {
					brandString.put(brand.getString("name_en") + " ## " + brand.getString("brand_id"));
				} else if (brand.has("name")) {
					brandString.put(brand.getString("name") + " ## " + brand.getString("brand_id"));
				}
			}
		}
		return brandString.toString();
	}
}
