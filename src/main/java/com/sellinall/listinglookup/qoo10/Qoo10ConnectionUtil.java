package com.sellinall.listinglookup.qoo10;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import com.sellinall.util.HttpsURLConnectionUtil;

public class Qoo10ConnectionUtil {

	public static String getQoo10ApiResponse(String params, String qoo10ApiHost, String action, String apiKey,
			String userId, String password, String method) {
		String requestURL = qoo10ApiHost + "/" + action;
		System.out.println("Qoo10 ID:" + userId + "----" + requestURL);
		String output = "";

		try {
			Map<String, String> config = new HashMap<String, String>();
			config.put("Content-Type", "application/x-www-form-urlencoded");
			params = params + "key="
					+ "S5bnbfynQvNi9I_g_1_oNLnmU9qgVI2GKgxrr0Zq6vX_g_2_PGphDsYvj30zDrWOoUdHbKJX7OVZ5omXKh50eE6gQVLQ5selIjY237PM2Ec7zKefwzUVQnO4bfgPEL1PVGO98JXN";
			JSONObject response = HttpsURLConnectionUtil.doPost(requestURL, params, config);
			if (response.getString("httpCode").equals("500")) {
				System.out.println("Error 500 " + response.getString("payload"));
			}
			return response.getString("payload");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}
