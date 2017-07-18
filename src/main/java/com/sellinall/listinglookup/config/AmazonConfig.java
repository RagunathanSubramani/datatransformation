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

	private static final Map<String, String> productAdvertisingAPIEndPointMap = Collections
			.unmodifiableMap(new HashMap<String, String>() {
				{
					put("CA", "https://webservices.amazon.ca");
					put("BR", "https://webservices.amazon.com.br");
					put("US", "https://webservices.amazon.com");
					put("CN", "https://webservices.amazon.cn");
					put("DE", "https://webservices.amazon.de");
					put("IN", "https://webservices.amazon.in");
					put("ES", "https://webservices.amazon.es");
					put("FR", "https://webservices.amazon.fr");
					put("IT", "https://webservices.amazon.it");
					put("JP", "https://webservices.amazon.co.jp");
					put("MX", "https://webservices.amazon.com.mx");
					put("GB", "https://webservices.amazon.co.uk");
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
		return "i10a3-20";
	}

	/**
	 * @return the AWSAccessKeyId
	 */
	public static String getProductAdvertisingAPIAWSAccessKeyId() {
		return "AKIAJKJ2YHR7CP7PANAA";
	}

	/**
	 * @return the AWSSecretKey
	 */
	public static String getProductAdvertisingAPIAWSSecretKey() {
		return "FufJjkiTonzRvfYV2CYP6yQZerR61IDV0Jpspzfh";
	}

}