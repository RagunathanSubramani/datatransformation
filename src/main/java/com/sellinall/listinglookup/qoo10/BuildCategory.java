package com.sellinall.listinglookup.qoo10;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class BuildCategory {

	public static String buildNewCategiryList(String countryCode) {
		String params = "lang_cd=ENG";
		params = params + "&" + "";
		String ScApiHost = getAPIUrl(countryCode);
		String apiKey = "BhiQeRsAhG14L_g_2_Jse_g_2_Hny5_g_2_SUHjyuW4IJW9yk377Gt0_g_3_";
		String userId = "SELLinALLAPI";
		String password = "";
		String method = "POST";
		String action = "GMKT.INC.Front.OpenApiService/CommonInfoAPIService.api/GetCatagoryListAll";
		String out = Qoo10ConnectionUtil.getQoo10ApiResponse(params, ScApiHost, action, apiKey, userId, password,
				method);
		org.json.JSONObject serviceResponseJSON = XML.toJSONObject(out);
		org.json.JSONObject serviceResponse = serviceResponseJSON
				.getJSONObject("StdCustomResultOfListOfCommonCategoryInfo");
		org.json.JSONObject serviceResponse1 = serviceResponse.getJSONObject("ResultObject");
		return parseAndPrintFile((JSONArray) serviceResponse1.get("CommonCategoryInfo"));
	}

	private static String parseAndPrintFile(JSONArray jsonArray) {
		String category = "";
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject json = jsonArray.getJSONObject(i);
			String categoryFullName = (String) json.get("CATE_L_NM") + "/" + (String) json.get("CATE_M_NM") + "/"
					+ (String) json.get("CATE_S_NM");
			String categoryId = "" + json.get("CATE_S_CD");
			category += "\"" + categoryFullName + " ## " + categoryId + "\",";
		}
		if (category.isEmpty()) {
			return category;
		}
		return "[" + category.substring(0, category.length() - 1) + "]";
	}

	private static String getAPIUrl(String countryCode) {
		switch (countryCode) {
		case "SG":
			return "https://api.qoo10.sg";
		case "MY":
			return "https://api.qoo10.my";
		case "HK":
			return "https://api.qoo10.hk";
		case "ID":
			return "https://api.qoo10.co.id";
		case "JP":
			return "https://api.qoo10.jp";
		case "CN":
			return "https://api.qoo10.cn";
		case "US":
			return "https://api.qoo10.com";
		default:
			return "";
		}
	}
}
