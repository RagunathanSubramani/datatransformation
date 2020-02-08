package com.sellinall.listinglookup.blibli;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
				.doGet(Config.getConfig().getSiaBlibliUrl() + "/brand?nickNameID=" + nickNameID, header);
		if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
			JSONObject response = new JSONObject(serviceResponse.getString("payload"));
			if (response.has("data")) {
				brandArray = response.getJSONArray("data").toString();
			}
		}
		return brandArray;
	}
}
