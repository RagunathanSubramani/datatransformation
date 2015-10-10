package com.sellinall.listinglookup.config;

import org.springframework.context.ApplicationContext;

public class Config {
	public static ApplicationContext context;

	private String EbayPostURL;
	private String EbayAppName;
	private String EbayDevName;
	private String EbayCertName;

	private String DbUserName;
	private String DbPassword;
	private String LookupCollectionHostName;
	private String LookupCollectionPort;
	private String LookupCollectionDBName;

	public String getEbayPostURL() {
		return EbayPostURL;
	}

	public void setEbayPostURL(String ebayPostURL) {
		EbayPostURL = ebayPostURL;
	}

	public String getEbayAppName() {
		return EbayAppName;
	}

	public void setEbayAppName(String ebayAppName) {
		EbayAppName = ebayAppName;
	}

	public String getEbayDevName() {
		return EbayDevName;
	}

	public void setEbayDevName(String ebayDevName) {
		EbayDevName = ebayDevName;
	}

	public String getEbayCertName() {
		return EbayCertName;
	}

	public void setEbayCertName(String ebayCertName) {
		EbayCertName = ebayCertName;
	}

	public String getDbUserName() {
		return DbUserName;
	}

	public void setDbUserName(String dbUserName) {
		DbUserName = dbUserName;
	}

	public String getDbPassword() {
		return DbPassword;
	}

	public void setDbPassword(String dbPassword) {
		DbPassword = dbPassword;
	}

	public String getLookupCollectionHostName() {
		return LookupCollectionHostName;
	}

	public void setLookupCollectionHostName(String userCollectionHostName) {
		LookupCollectionHostName = userCollectionHostName;
	}

	public String getLookupCollectionPort() {
		return LookupCollectionPort;
	}

	public void setLookupCollectionPort(String userCollectionPort) {
		LookupCollectionPort = userCollectionPort;
	}

	public String getLookupCollectionDBName() {
		return LookupCollectionDBName;
	}

	public void setLookupCollectionDBName(String userCollectionDBName) {
		LookupCollectionDBName = userCollectionDBName;
	}

	public static Config getConfig() {
		return (Config) context.getBean("Config");
	}

}
