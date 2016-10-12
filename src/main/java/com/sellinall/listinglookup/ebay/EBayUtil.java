package com.sellinall.listinglookup.ebay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.HttpURLConnectionUtil;

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
		String eBayToken = "AgAAAA**AQAAAA**aAAAAA**0dUgVg**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6AAkIqnC5WFpQ+dj6x9nY+seQ**PzMCAA**AAMAAA**Sh28/BPrA6/M0EzHrHzfImVuxFok6Fu1yzjSYRUI5gD1nD7H6Zzyqzha6sqZgOJ1CExD6UxkPiogIn8JKCoSqTVXNdYPzadTJPg5Sbmc+99clUy2dEAg1xYBnPQNC3+NvP5WuqcxcXnRGdJeemKP21rI67s0v1gIvOu8Ng7kOqzB/vKKZFrvUZfmkkjc0gPWJNXz4BdJOoP6vlZPxibMXKUJ+FRx1qoQmi3qwlv1wfzVjf6AyuE5jTfJ4sJF7ORE4+AN4kooEZbq4DPSEjBd1MIyp7jvLWvWItW1C+exlkUI6UxKT9XAVs5oxITX9x0sOLO0uodPYfvtA6poevd8TJqPa95v6D73tefDHuPbZSAnCnvF1K6FfAhcQ8gsDnE/6kiIXXCqjfoD1xfxGJE4XsQafFFQmbaRAZI4PbPjcrFYZlPlUuyyR3OdGtqnGY++uqJRU2rPNK5h4FDnOrRqCFZd+r6zDe8Bn9jmeizS8hSzdy5WxvxTnZNCneImItsDOOdmz8RdvNjOaeC4Y3jfxPvXYhU1mw9nAxjTff79h9CcEp6/D/12EJt+54HbvqO7YZ3K7UhS2rPn+K1IXpSmFxu1HRUn1ugxZalozkeiHGgeeGo1Kl3AY/NWbKiu1/POKQbDW3v7yzjMUaWDN5vzR1T6poF7MJSpcdErOVIoMWHulpVP00fHhvxDm2ZnPDLTqI8MN43KtzY6/mITCHvwgQDG8fwvF2wsfsiVOpSkLHnvn9p+luKXp8xk6OVzIuid";
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
		String eBayToken = "AgAAAA**AQAAAA**aAAAAA**0dUgVg**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6AAkIqnC5WFpQ+dj6x9nY+seQ**PzMCAA**AAMAAA**Sh28/BPrA6/M0EzHrHzfImVuxFok6Fu1yzjSYRUI5gD1nD7H6Zzyqzha6sqZgOJ1CExD6UxkPiogIn8JKCoSqTVXNdYPzadTJPg5Sbmc+99clUy2dEAg1xYBnPQNC3+NvP5WuqcxcXnRGdJeemKP21rI67s0v1gIvOu8Ng7kOqzB/vKKZFrvUZfmkkjc0gPWJNXz4BdJOoP6vlZPxibMXKUJ+FRx1qoQmi3qwlv1wfzVjf6AyuE5jTfJ4sJF7ORE4+AN4kooEZbq4DPSEjBd1MIyp7jvLWvWItW1C+exlkUI6UxKT9XAVs5oxITX9x0sOLO0uodPYfvtA6poevd8TJqPa95v6D73tefDHuPbZSAnCnvF1K6FfAhcQ8gsDnE/6kiIXXCqjfoD1xfxGJE4XsQafFFQmbaRAZI4PbPjcrFYZlPlUuyyR3OdGtqnGY++uqJRU2rPNK5h4FDnOrRqCFZd+r6zDe8Bn9jmeizS8hSzdy5WxvxTnZNCneImItsDOOdmz8RdvNjOaeC4Y3jfxPvXYhU1mw9nAxjTff79h9CcEp6/D/12EJt+54HbvqO7YZ3K7UhS2rPn+K1IXpSmFxu1HRUn1ugxZalozkeiHGgeeGo1Kl3AY/NWbKiu1/POKQbDW3v7yzjMUaWDN5vzR1T6poF7MJSpcdErOVIoMWHulpVP00fHhvxDm2ZnPDLTqI8MN43KtzY6/mITCHvwgQDG8fwvF2wsfsiVOpSkLHnvn9p+luKXp8xk6OVzIuid";
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
				getConfig(configValue, siteID), "xml").getString("payload");
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
