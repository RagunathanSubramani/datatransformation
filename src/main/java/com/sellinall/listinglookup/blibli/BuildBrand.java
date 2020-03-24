package com.sellinall.listinglookup.blibli;

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

	public static String buildBrand(String accountNumber, String nickNameID)
			throws IOException, JSONException, InterruptedException {
		JSONArray totalBrandList = new JSONArray();
		Map<String, String> header = new HashMap<String, String>();
		header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
		header.put("accountNumber", accountNumber);
		header.put("Content-Type", "application/json");
		int pageNumber = 0;
		int totalPages = 1;
		int retryCount = 0;
		for (pageNumber = 0; pageNumber < totalPages; pageNumber++) {
			JSONObject serviceResponse = HttpsURLConnectionUtil.doGet(Config.getConfig().getSiaBlibliUrl()
					+ "/brand?nickNameID=" + nickNameID + "&pageNumber=" + pageNumber, header);
			if (serviceResponse.getInt("httpCode") == HttpStatus.OK_200) {
				JSONObject response = new JSONObject(serviceResponse.getString("payload"));
				if (response.has("data")) {
					retryCount = 0;
					JSONArray brandList = response.getJSONArray("data");
					for (int i = 0; i < brandList.length(); i++) {
						String brand = brandList.getString(i);
						totalBrandList.put(brand);
					}
					if (response.has("totalPages")) {
						totalPages = response.getInt("totalPages");
					}
					pageNumber = response.getInt("pageNumber");
				} else if (retryCount < 2) {
					pageNumber -= 1;
					retryCount++;
					Thread.sleep(500);
				}
			}
		}
		return totalBrandList.toString();
	}
}