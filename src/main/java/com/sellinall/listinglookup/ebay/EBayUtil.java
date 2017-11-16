package com.sellinall.listinglookup.ebay;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.HttpsURLConnectionUtil;

public class EBayUtil {
	static Logger log = Logger.getLogger(EBayUtil.class.getName());

	private static final Map<String, String> siteIdMap = Collections.unmodifiableMap(new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			// https://developer.ebay.com/DevZone/merchandising/docs/Concepts/SiteIDToGlobalID.html
			put("US", "0");
			put("CA", "2");
			put("GB", "3");
			put("AU", "15");
			put("AT", "16");
			put("FR", "71");
			put("DE", "77");
			put("IT", "101");
			put("NL", "146");
			put("ES", "186");
			put("CH", "193");
			put("HK", "201");
			put("IN", "203");
			put("IE", "205");
			put("MY", "207");
			put("PH", "211");
			put("PL", "212");
			put("SG", "216");
		}
	});

	public static String getSiteId(String countryCode) {
		return siteIdMap.get(countryCode);
	}

	public static String getParentCategory(String siteId) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>");
		sb.append("<GetCategoriesRequest xmlns='urn:ebay:apis:eBLBaseComponents'>");
		sb.append("<RequesterCredentials>");
		sb.append("<eBayAuthToken>" + Config.getConfig().getEbayToken() + "</eBayAuthToken>");
		sb.append("</RequesterCredentials>");
		// sb.append("<CategoryParent>550</CategoryParent>");
		sb.append("<DetailLevel>ReturnAll</DetailLevel>");
		sb.append("<LevelLimit>1</LevelLimit> ");
		sb.append("</GetCategoriesRequest>");
		String urlParameters = sb.toString();
		String response = new String();
		try {
			response = getResponse(urlParameters, "GetCategories", siteId);
		} catch (Exception e) {

		}
		return response;
	}

	// Method used to get XML From Ebay
	public static String getChildCategory(String id, String siteId) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>");
		sb.append("<GetCategoriesRequest xmlns='urn:ebay:apis:eBLBaseComponents'>");
		sb.append("<RequesterCredentials>");
		sb.append("<eBayAuthToken>" + Config.getConfig().getEbayToken() + "</eBayAuthToken>");
		sb.append("</RequesterCredentials>");
		sb.append("<CategoryParent>" + id + "</CategoryParent>");
		sb.append("<DetailLevel>ReturnAll</DetailLevel>");
		sb.append("<LevelLimit>9</LevelLimit> ");
		sb.append("</GetCategoriesRequest>");
		String urlParameters = sb.toString();
		String response = new String();
		try {
			System.out.println(sb.toString());
			response = getResponse(urlParameters, "GetCategories", siteId);
		} catch (Exception e) {

		}
		return response;
	}

	public static String getCategorySpecifics(String countryCode, String categoryId) {
		String siteId = getSiteId(countryCode);
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>");
		sb.append("<GetCategorySpecificsRequest xmlns='urn:ebay:apis:eBLBaseComponents'>");
		sb.append("<CategorySpecific>");
		sb.append("<CategoryID>" + categoryId + "</CategoryID>");
		sb.append("</CategorySpecific>");
		sb.append("<RequesterCredentials>");
		sb.append("<eBayAuthToken>" + Config.getConfig().getEbayToken() + "</eBayAuthToken>");
		sb.append("</RequesterCredentials>");
		sb.append("<ErrorLanguage>en_US</ErrorLanguage>");
		sb.append("<WarningLevel>High</WarningLevel>");
		sb.append("</GetCategorySpecificsRequest>");
		String urlParameters = sb.toString();
		String response = new String();
		try {
			log.debug(sb.toString());
			response = getResponse(urlParameters, "GetCategorySpecifics", siteId);
			log.debug(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public static String getCategoryFeatures(String countryCode, String categoryId) {
		String siteId = getSiteId(countryCode);
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version='1.0' encoding='utf-8'?>");
		sb.append("<GetCategoryFeaturesRequest xmlns='urn:ebay:apis:eBLBaseComponents'>");
		sb.append("<DetailLevel>ReturnAll</DetailLevel>");
		sb.append("<ViewAllNodes>true</ViewAllNodes>");
		sb.append("<CategoryID>" + categoryId + "</CategoryID>");
		sb.append("<FeatureID>ConditionEnabled</FeatureID>");
		sb.append("<FeatureID>ConditionValues</FeatureID>");
		sb.append("<FeatureID>VariationsEnabled</FeatureID>");
		sb.append("<FeatureID>ItemSpecificsEnabled</FeatureID>");
		sb.append("<FeatureID>UPCEnabled</FeatureID>");
		sb.append("<RequesterCredentials>");
		sb.append("<eBayAuthToken>" + Config.getConfig().getEbayToken() + "</eBayAuthToken>");
		sb.append("</RequesterCredentials>");
		sb.append("<ErrorLanguage>en_US</ErrorLanguage>");
		sb.append("<WarningLevel>High</WarningLevel>");
		sb.append("</GetCategoryFeaturesRequest>");
		String urlParameters = sb.toString();
		String response = new String();
		try {
			log.debug(sb.toString());
			response = getResponse(urlParameters, "GetCategoryFeatures", siteId);
			log.debug(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public static String getCategoryInfo(String countryCode, String categoryId) throws IOException, JSONException {
		String siteId = getSiteId(countryCode);
		String url = Config.getConfig().getEbayOpenApiURL() + "/Shopping?callname=GetCategoryInfo";
		url = url + "&appid=" + Config.getConfig().getEbayAppName();
		url = url + "&version=677&siteid=" + siteId + "&CategoryID=" + categoryId;
		JSONObject response = HttpsURLConnectionUtil.doGet(url, null);
		if (response.has("payload")) {
			return response.getString("payload");
		}
		return "";
	}

	private static String getResponse(String payLoad, String configValue, String siteID) throws Exception {
		JSONObject response = HttpsURLConnectionUtil
				.doPost(Config.getConfig().getEbayPostURL(), payLoad, getConfig(configValue, siteID));
		if (response.has("payload")) {
			return response.getString("payload");
		}
		return "";
	}

	private static Map<String, String> getConfig(String apiCallName, String siteId) {
		Map<String, String> customConfigurationMap = new HashMap<String, String>();

		customConfigurationMap.put("X-EBAY-API-COMPATIBILITY-LEVEL", "847");
		customConfigurationMap.put("X-EBAY-API-DEV-NAME", Config.getConfig().getEbayDevName());
		customConfigurationMap.put("X-EBAY-API-APP-NAME", Config.getConfig().getEbayAppName());
		customConfigurationMap.put("X-EBAY-API-CERT-NAME", Config.getConfig().getEbayCertName());
		customConfigurationMap.put("X-EBAY-API-SITEID", siteId);
		customConfigurationMap.put("X-EBAY-API-CALL-NAME", apiCallName);
		customConfigurationMap.put("Content-Type", "application/atom+xml");

		return customConfigurationMap;
	}

}
