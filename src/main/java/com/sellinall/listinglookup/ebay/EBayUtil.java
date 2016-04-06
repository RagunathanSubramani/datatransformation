package com.sellinall.listinglookup.ebay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mudra.sellinall.util.HttpURLConnectionUtil;
import com.sellinall.listinglookup.config.Config;

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

	public static String getCategorySpecifics(String countryCode, String categoryId) {
		String siteId = getSiteId(countryCode);
		StringBuffer sb = new StringBuffer();
		String eBayToken = "AgAAAA**AQAAAA**aAAAAA**L2ZaUw**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wFk4GhC5eBpgidj6x9nY+seQ**1GkCAA**AAMAAA**B4yBM9s30VyWeK4bgDxdveY9i/cYkcY1COXnnodbDa4Xljztk5PYUwUDAf21vmCP6Q4/hY5dPv8zZJWPuqoZIB6u4f9ndYz1+Lq+AtEn32FI/EwuLIvcKT7v4vVv7XiplIrFm/NJLO7LM0UeXiDiIL51WHOaUJNwdxgGfdocN/ZID5sFPpviFXyPfTpr6eJgBqaOEoIVf6c5fBKDOm2UmahYnzGlwySm2UXie5ut7UBjimi2psVZptwwUtwtCi7cDQ9IbdmO3bkMlSQEF1vYrj72oNB7HkYb1pO3uhJZjlRra6gN2ADh366FwoDyrjp0kO7FKezWTX+FRF8L0afkTKKTYMECQJ4p/i5hAJGN3pMkvApa55YWuRnExuwBfipiTNeIjAyFuLf8+1AdEZiQiIWgEOpKHP6c6IN8/OfVyT6jls1aEsDsHl7Gxs6V3NeB5FTBsRduuEH6/i1Hr+7yQuAuy4rCl9SzSGeZpXh9ept/tVhFR4hxYakzzhTQEw/DAz/SUDIqtsyRF2cYFwVKjPMcfPg3NwlSfR23Rpbr7jeXDWvryaP028hOLMvMcAnhXTQqX0GakV5svwUIRTNaTF4MhkJLx/quethIZYaunvo7Pyem2o6ExBZwA1PgxkeORB56RfZwK8jTnm2Le0CPsHaILDfyHDleNxkkfcVW2AsC0LTYgfHxQFzH39RyWc9zOl1zbPiVezRQVVSXUpgOJCI1kS37Ma5RwFUyzOFCAINPesIW2DAsHHxWD2Y0/DlJ";
		sb.append("<?xml version='1.0' encoding='utf-8'?>");
		sb.append("<GetCategorySpecificsRequest xmlns='urn:ebay:apis:eBLBaseComponents'>");
		sb.append("<CategorySpecific>");
		sb.append("<CategoryID>" + categoryId + "</CategoryID>");
		sb.append("</CategorySpecific>");
		sb.append("<RequesterCredentials>");
		sb.append("<eBayAuthToken>" + eBayToken + "</eBayAuthToken>");
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
		String eBayToken = "AgAAAA**AQAAAA**aAAAAA**L2ZaUw**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wFk4GhC5eBpgidj6x9nY+seQ**1GkCAA**AAMAAA**B4yBM9s30VyWeK4bgDxdveY9i/cYkcY1COXnnodbDa4Xljztk5PYUwUDAf21vmCP6Q4/hY5dPv8zZJWPuqoZIB6u4f9ndYz1+Lq+AtEn32FI/EwuLIvcKT7v4vVv7XiplIrFm/NJLO7LM0UeXiDiIL51WHOaUJNwdxgGfdocN/ZID5sFPpviFXyPfTpr6eJgBqaOEoIVf6c5fBKDOm2UmahYnzGlwySm2UXie5ut7UBjimi2psVZptwwUtwtCi7cDQ9IbdmO3bkMlSQEF1vYrj72oNB7HkYb1pO3uhJZjlRra6gN2ADh366FwoDyrjp0kO7FKezWTX+FRF8L0afkTKKTYMECQJ4p/i5hAJGN3pMkvApa55YWuRnExuwBfipiTNeIjAyFuLf8+1AdEZiQiIWgEOpKHP6c6IN8/OfVyT6jls1aEsDsHl7Gxs6V3NeB5FTBsRduuEH6/i1Hr+7yQuAuy4rCl9SzSGeZpXh9ept/tVhFR4hxYakzzhTQEw/DAz/SUDIqtsyRF2cYFwVKjPMcfPg3NwlSfR23Rpbr7jeXDWvryaP028hOLMvMcAnhXTQqX0GakV5svwUIRTNaTF4MhkJLx/quethIZYaunvo7Pyem2o6ExBZwA1PgxkeORB56RfZwK8jTnm2Le0CPsHaILDfyHDleNxkkfcVW2AsC0LTYgfHxQFzH39RyWc9zOl1zbPiVezRQVVSXUpgOJCI1kS37Ma5RwFUyzOFCAINPesIW2DAsHHxWD2Y0/DlJ";
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
		sb.append("<eBayAuthToken>" + eBayToken + "</eBayAuthToken>");
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

	public static String getCategoryInfo(String countryCode, String categoryId) {
		String siteId = getSiteId(countryCode);
		String url = Config.getConfig().getEbayOpenApiURL() + "/Shopping?callname=GetCategoryInfo";
		url = url + "&appid=" + Config.getConfig().getEbayAppName();
		url = url + "&version=677&siteid=" + siteId + "&CategoryID=" + categoryId;
		return HttpURLConnectionUtil.doGet(url);
	}

	private static String getResponse(String urlParameter, String configValue, String siteID) throws Exception {
		org.codehaus.jettison.json.JSONObject payLoad = new org.codehaus.jettison.json.JSONObject();
		payLoad.put("data", urlParameter);
		return HttpURLConnectionUtil.doPostWithHeader(Config.getConfig().getEbayPostURL(), payLoad,
				getConfig(configValue, siteID), "xml");
	}

	private static Map<String, String> getConfig(String apiCallName, String siteId) {
		Map<String, String> customConfigurationMap = new HashMap<String, String>();

		customConfigurationMap.put("X-EBAY-API-COMPATIBILITY-LEVEL", "847");
		customConfigurationMap.put("X-EBAY-API-DEV-NAME", Config.getConfig().getEbayDevName());
		customConfigurationMap.put("X-EBAY-API-APP-NAME", Config.getConfig().getEbayAppName());
		customConfigurationMap.put("X-EBAY-API-CERT-NAME", Config.getConfig().getEbayCertName());
		customConfigurationMap.put("X-EBAY-API-SITEID", siteId);
		customConfigurationMap.put("X-EBAY-API-CALL-NAME", apiCallName);
		customConfigurationMap.put("Content-Type", "text/xml");

		return customConfigurationMap;
	}

}
