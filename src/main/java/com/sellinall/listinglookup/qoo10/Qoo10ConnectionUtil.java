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
					+ "S5bnbfynQvPp0_g_1_YcGN1OOlCMdQJpv5Cv5XrCFuXKwqddaMFc8c_g_1_C42FPM2D_g_2_HS4KXsfPHNe4EBlBg8GUtMGKUu4hUjjOPigHO7PGuCGy4nl0lLxs1SPxluhMIdRC0G7e";
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