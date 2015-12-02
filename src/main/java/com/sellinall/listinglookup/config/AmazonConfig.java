package com.sellinall.listinglookup.config;

/**
 * 
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vikraman
 * @company sellinall
 *
 */
public class AmazonConfig {

	
	private static final Map<String, String> productAdvertisingAPIEndPointMap = Collections.unmodifiableMap(new HashMap<String, String>() {
		{
			put("A2EUQ1WTGCTBG2", "https://webservices.amazon.ca");
			put("A1PA6795UKMFR9", "https://mws-eu.amazonservices.com");
			put("ATVPDKIKX0DER", "https://webservices.amazon.com");
			put("A1RKKUPIHCS9HS", "https://mws-eu.amazonservices.com");
			put("A13V1IB3VIYZZH", "https://mws-eu.amazonservices.com");
			put("A21TJRUUN4KGV", "https://mws.amazonservices.in");
			put("APJ6JRA9NG5V4", "https://mws-eu.amazonservices.com");
			put("A1F83G8C2ARO7P", "https://mws-eu.amazonservices.com");
			put("A1VC38T7YXB528", "https://mws.amazonservices.jp");
			put("AAHKV2X7AFYLW", "https://mws.amazonservices.com.cn");
		}
	});

	/**
	 * @return the signatureMethod
	 */
	public static String getSignatureMethod() {
		return "HmacSHA256";
	}



	
	public static String getProductAdvertisingAPIEndPoint(String marketplaceID) {
		return productAdvertisingAPIEndPointMap.get(marketplaceID);
	}

	/**
	 * @return the associateTag
	 */
	public static String getProductAdvertisingAPIAssociateTag() {
		return "sellinall-20";
	}
	
	/**
	 * @return the AWSAccessKeyId
	 */
	public static String getProductAdvertisingAPIAWSAccessKeyId() {
		return "AKIAJPRBK37MZJNPI4YQ";
	}
	
	/**
	 * @return the AWSSecretKey
	 */
	public static String getProductAdvertisingAPIAWSSecretKey() {
		return "HqMs1h3dJqkepc+PYZvbHixPGlN860WbKKFWo3c+";
	}
	
}