package com.sellinall.listinglookup.ebay;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.HttpURLConnectionUtil;

public class EBayUtil {
	static Logger log = Logger.getLogger(EBayUtil.class.getName());
	private static String eBayToken = "AgAAAA**AQAAAA**aAAAAA**CVedWQ**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6AAl4GlCpiHpASdj6x9nY+seQ**PzMCAA**AAMAAA**RYCpuWLvAo4KybMRoLvh7a4LAx6y3z5OyJzRgZqRRMoJ/vackPytXNR+j8e0wGlOC3/MXcByMJExILgKR7dZpKTknUB67t4LjnY3GaHOx4nqncp4fDkGSg7q3WXsyxf6XigtAywhcl/GKStQO28OlaWV7IfcqKe5qYVImHnYfmCadS3Wv03XPmPFqsw6slIPydq8+phhiDbbiuiL7IcEeqIHVdEtspC4Q9MxPfzf98ePYvErPj78m1Y0ieP0R+FqvqlLUZMyWViSfxIWizS1nbu9zvY8C7xw7AOvwSFd6OnqEodKKdAkNS06jgvlDgRS4aFSU1KQkwFyEnJcbzf6/qObPScuWHGKJf8N6vbkUY9GJO2Vn1ahOaaHfAcMrfqSoEB/mVN5mkl2DgScL3kjNlFbRCsrc+RWlGsJmz/YMXJ3Vp5UUqiu363h0t21di5GW9aide83XWUOO+vtghQzIgleDtXdJsqglBf/Ek8dNWrdXydCA9X8+G6OBniG2FigY9TzsH+gVvQewLmp6xorQ7wkliNk6i6hiXVYjTWYXOxRf/9z4DN9XSFfIeddmL9XaL1sGMYoeC6059fmJpQAKBaLwsrGX7PPdE+qbBLbvr/sr2bqGnKanyLcUBA6t/NtMO7IMSy81OBc6H5jh8y4NisS2HXQr5qE2lBuo7jOb6itzmXpKzhc3JLpG9ytK7K8UcjlDbi10Fp/DHQjKgVQQDL3HUTJ0M3ViQS2bDDiSlTLkBSsm1yVo60ol+SZcRz/";
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
		sb.append("<eBayAuthToken>" + eBayToken + "</eBayAuthToken>");
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
		sb.append("<eBayAuthToken>" + eBayToken + "</eBayAuthToken>");
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

	public static String getCategoryInfo(String countryCode, String categoryId) throws IOException {
		String siteId = getSiteId(countryCode);
		String url = Config.getConfig().getEbayOpenApiURL() + "/Shopping?callname=GetCategoryInfo";
		url = url + "&appid=" + Config.getConfig().getEbayAppName();
		url = url + "&version=677&siteid=" + siteId + "&CategoryID=" + categoryId;
		return HttpURLConnectionUtil.doGet(url);
	}

	private static String getResponse(String urlParameter, String configValue, String siteID) throws Exception {
		org.codehaus.jettison.json.JSONObject payLoad = new org.codehaus.jettison.json.JSONObject();
		payLoad.put("data", urlParameter);
		return HttpURLConnectionUtil
				.doPostWithHeader(Config.getConfig().getEbayPostURL(), payLoad, getConfig(configValue, siteID), "xml")
				.getString("payload");
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
