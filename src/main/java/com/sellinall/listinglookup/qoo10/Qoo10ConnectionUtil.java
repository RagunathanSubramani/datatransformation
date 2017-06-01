package com.sellinall.listinglookup.qoo10;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import com.sellinall.util.HttpURLConnectionUtil;

public class Qoo10ConnectionUtil {
	
	public static String getQoo10ApiResponse(String params, String qoo10ApiHost, String action, String apiKey,
			String userId, String password, String method) {
			String requestURL = qoo10ApiHost + "/" + action;
			System.out.println("Qoo10 ID:" + userId + "----" + requestURL);
			String output = "";

			try {
				Map < String,
				String > config = new HashMap < String,
				String > ();
				config.put("Content-Type", "application/x-www-form-urlencoded");
				params = params + "key=" + "S5bnbfynQvNi9I_g_1_oNLnmU9qgVI2GKgxrr0Zq6vX_g_2_PGphDsYvj30zDqt_g_1_6GV9awp1TYJc6dDKOCikNtR2ytVenzuD1blsaaUL_g_2_D2Jwhc4ucVsMNrVtmCNHc7_g_2_LglYxxi_g_2_";

				
					JSONObject payLoad = new JSONObject();
					payLoad.put("data", params);
					config.put("Content-Length", "" + params.length());
					JSONObject response = HttpURLConnectionUtil.doPostWithHeader(requestURL, payLoad, config, "string");
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
