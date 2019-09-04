package com.sellinall.listinglookup.qoo10;

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
		String brandArray = "";
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		JSONObject serviceResponse = HttpsURLConnectionUtil
				.doGet(Config.getConfig().getQoo10URL() + "/brand?siteNicknames=" + nickNameID, header);
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject(serviceResponse.getString("payload"));
			if (response.has("statusCode") && response.getString("statusCode").equals("0")) {
				brandArray = parseAndPrintFile((JSONArray) response.get("data"));
			}
		}
		return brandArray;
	}

	private static String parseAndPrintFile(JSONArray responseData) throws JSONException {
		JSONArray brandString = new JSONArray();
		for (int i = 0; i < responseData.length(); i++) {
			JSONObject brand = responseData.getJSONObject(i);
			if (brand.has("M_B_NO")) {
				if (brand.has("M_B_NM_EN") && !brand.getString("M_B_NM_EN").isEmpty()) {
					brandString.put(brand.getString("M_B_NM_EN") + " ## " + brand.getString("M_B_NO").split("_")[1]);
				} else if (brand.has("M_B_NM") && !brand.getString("M_B_NM").isEmpty()) {
					brandString.put(brand.getString("M_B_NM") + " ## " + brand.getString("M_B_NO").split("_")[1]);
				}
			}
		}
		return brandString.toString();
	}

}
