package com.sellinall.listinglookup.amazon;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.mudra.sellinall.util.HttpURLConnectionUtil;
import com.sellinall.listinglookup.config.AmazonConfig;

public class AmazonUtil {
	static Logger log = Logger.getLogger(AmazonUtil.class.getName());

	public static String getProduct(String searchParamType, String searchsearchParam, String countryCode) {
		String response = new String();
		try {
			response = getResponse(searchParamType, searchsearchParam, countryCode);
		} catch (Exception e) {

		}
		return response;
	}

	private static String getResponse(String searchParamTypeType, String searchParamType, String countryCode)
			throws Exception {
		countryCode = (countryCode == null) ? "US" : countryCode;
		String queryString = urlEncodeUTF8(getQueryStringwihtURL(searchParamTypeType, searchParamType, countryCode));
		log.debug(AmazonConfig.getProductAdvertisingAPIEndPoint(countryCode) + "/onca/xml?" + queryString);
		return HttpURLConnectionUtil.doGet(AmazonConfig.getProductAdvertisingAPIEndPoint(countryCode) + "/onca/xml?"
				+ queryString);
	}

	private static String urlEncodeUTF8(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	private static String urlEncodeUTF8(Map<?, ?> map) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(String.format("%s=%s", urlEncodeUTF8(entry.getKey().toString()), urlEncodeUTF8(entry.getValue()
					.toString())));
		}
		return sb.toString();
	}

	private static Map<String, String> getQueryStringwihtURL(String searchParamTypeType, String searchParamType,
			String countryCode) {

		HashMap<String, String> queryStringMap = new HashMap<String, String>();

		// Read below from property file
		queryStringMap.put("AWSAccessKeyId", AmazonConfig.getProductAdvertisingAPIAWSAccessKeyId());
		queryStringMap.put("AssociateTag", AmazonConfig.getProductAdvertisingAPIAssociateTag());
		queryStringMap.put("ResponseGroup",
				"EditorialReview,Images,ItemAttributes,VariationMatrix,VariationSummary,Variations,BrowseNodes");
		queryStringMap.put("Timestamp", AmazonUtil.getFormattedTimeInMS());
		queryStringMap.put("Version", "2011-08-01");
		queryStringMap.put("Operation", "ItemLookup");
		if (!searchParamTypeType.equals("ASIN")) {
			queryStringMap.put("SearchIndex", "All");
		}
		queryStringMap.put("Service", "AWSECommerceService");
		queryStringMap.put("Condition", "All");
		queryStringMap.put("IdType", searchParamTypeType);
		queryStringMap.put("ItemId", searchParamType);
		// Needs to change based on user country of posting
		String serviceURL = AmazonConfig.getProductAdvertisingAPIEndPoint(countryCode) + "/onca/xml";
		try {
			queryStringMap.put(
					"Signature",
					AmazonSignatureUtil.signProductAdvertisingAPIParameters(queryStringMap,
							AmazonConfig.getProductAdvertisingAPIAWSSecretKey(), serviceURL));
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return queryStringMap;

	}

	public static String getFormattedTimeInMS() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format(new Date());
	}
}
