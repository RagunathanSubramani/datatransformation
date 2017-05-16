package com.sellinall.listinglookup.config;

import org.springframework.context.ApplicationContext;

public class Config {
	public static ApplicationContext context;

	private String EbayPostURL;
	private String EbayAppName;
	private String EbayDevName;
	private String EbayCertName;
	private String EbayOpenApiURL;

	private String DbUserName;
	private String DbPassword;
	private String LookupCollectionHostName;
	private String LookupCollectionPort;
	private String LookupCollectionDBName;
	
	private String UserCollectionHostName;
	private String UserCollectionPort;
	private String UserCollectionDBName;
	private String SIAAuthServerURL;

	private String shopcluesItemSpecificationUrl;
	private String shopcluesAuthUrl;
	private String username;
	private String password;
	private String client_id;
	private String client_secret;
	private String grant_type;

	private String snapdealUrl;
	private String snapdealClientId;
	private String snapdealAuthToken;
	private String Ragasiyam;
	private String etsyUrl;
	
	private String lazadaSGDefaultAccount;
	private String lazadaIDDefaultAccount;
	private String lazadaMYDefaultAccount;
	private String lazadaPHDefaultAccount;
	private String lazadaTHDefaultAccount;
	
	private String lazadaSGDefaultNickNameId;
	private String lazadaIDDefaultNickNameId;
	private String lazadaMYDefaultNickNameId;
	private String lazadaPHDefaultNickNameId;
	private String lazadaTHDefaultNickNameId;
	
	private String etsyUSDefaultAccount;
	private String etsyUSDefaultNickNameId;
	private String etsyConsumerKey;
	private String etsyConsumerSecret;

	public String getRagasiyam() {
		return Ragasiyam;
	}

	public void setRagasiyam(String ragasiyam) {
		Ragasiyam = ragasiyam;
	}

	public String getGrant_type() {
		return grant_type;
	}

	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}

	public String getClient_secret() {
		return client_secret;
	}

	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getShopcluesItemSpecificationUrl() {
		return shopcluesItemSpecificationUrl;
	}

	public void setShopcluesItemSpecificationUrl(String shopcluesItemSpecificationUrl) {
		this.shopcluesItemSpecificationUrl = shopcluesItemSpecificationUrl;
	}

	public String getShopcluesAuthUrl() {
		return shopcluesAuthUrl;
	}

	public void setShopcluesAuthUrl(String shopcluesAuthUrl) {
		this.shopcluesAuthUrl = shopcluesAuthUrl;
	}

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

	public String getEbayOpenApiURL() {
		return EbayOpenApiURL;
	}

	public void setEbayOpenApiURL(String ebayOpenApiURL) {
		EbayOpenApiURL = ebayOpenApiURL;
	}

	public String getEtsyUrl() {
		return etsyUrl;
	}

	public void setEtsyUrl(String etsyUrl) {
		this.etsyUrl = etsyUrl;
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

	public String getSIAAuthServerURL() {
		return SIAAuthServerURL;
	}

	public void setSIAAuthServerURL(String sIAAuthServerURL) {
		SIAAuthServerURL = sIAAuthServerURL;
	}

	public String getSnapdealUrl() {
		return snapdealUrl;
	}

	public void setSnapdealUrl(String snapdealUrl) {
		this.snapdealUrl = snapdealUrl;
	}

	public String getSnapdealClientId() {
		return snapdealClientId;
	}

	public void setSnapdealClientId(String snapdealClientId) {
		this.snapdealClientId = snapdealClientId;
	}

	public String getSnapdealAuthToken() {
		return snapdealAuthToken;
	}

	public void setSnapdealAuthToken(String snapdealAuthToken) {
		this.snapdealAuthToken = snapdealAuthToken;
	}

	public String getUserCollectionHostName() {
		return UserCollectionHostName;
	}

	public void setUserCollectionHostName(String userCollectionHostName) {
		UserCollectionHostName = userCollectionHostName;
	}

	public String getUserCollectionPort() {
		return UserCollectionPort;
	}

	public void setUserCollectionPort(String userCollectionPort) {
		UserCollectionPort = userCollectionPort;
	}

	public String getUserCollectionDBName() {
		return UserCollectionDBName;
	}

	public void setUserCollectionDBName(String userCollectionDBName) {
		UserCollectionDBName = userCollectionDBName;
	}
	
	public String getLazadaSGDefaultAccount() {
		return lazadaSGDefaultAccount;
	}

	public void setLazadaSGDefaultAccount(String lazadaSGDefaultAccount) {
		this.lazadaSGDefaultAccount = lazadaSGDefaultAccount;
	}

	public String getLazadaIDDefaultAccount() {
		return lazadaIDDefaultAccount;
	}

	public void setLazadaIDDefaultAccount(String lazadaIDDefaultAccount) {
		this.lazadaIDDefaultAccount = lazadaIDDefaultAccount;
	}

	public String getLazadaMYDefaultAccount() {
		return lazadaMYDefaultAccount;
	}

	public void setLazadaMYDefaultAccount(String lazadaMYDefaultAccount) {
		this.lazadaMYDefaultAccount = lazadaMYDefaultAccount;
	}

	public String getLazadaPHDefaultAccount() {
		return lazadaPHDefaultAccount;
	}

	public void setLazadaPHDefaultAccount(String lazadaPHDefaultAccount) {
		this.lazadaPHDefaultAccount = lazadaPHDefaultAccount;
	}

	public String getLazadaTHDefaultAccount() {
		return lazadaTHDefaultAccount;
	}

	public void setLazadaTHDefaultAccount(String lazadaTHDefaultAccount) {
		this.lazadaTHDefaultAccount = lazadaTHDefaultAccount;
	}

	public String getLazadaSGDefaultNickNameId() {
		return lazadaSGDefaultNickNameId;
	}

	public void setLazadaSGDefaultNickNameId(String lazadaSGDefaultNickNameId) {
		this.lazadaSGDefaultNickNameId = lazadaSGDefaultNickNameId;
	}

	public String getLazadaIDDefaultNickNameId() {
		return lazadaIDDefaultNickNameId;
	}

	public void setLazadaIDDefaultNickNameId(String lazadaIDDefaultNickNameId) {
		this.lazadaIDDefaultNickNameId = lazadaIDDefaultNickNameId;
	}

	public String getLazadaMYDefaultNickNameId() {
		return lazadaMYDefaultNickNameId;
	}

	public void setLazadaMYDefaultNickNameId(String lazadaMYDefaultNickNameId) {
		this.lazadaMYDefaultNickNameId = lazadaMYDefaultNickNameId;
	}

	public String getLazadaPHDefaultNickNameId() {
		return lazadaPHDefaultNickNameId;
	}

	public void setLazadaPHDefaultNickNameId(String lazadaPHDefaultNickNameId) {
		this.lazadaPHDefaultNickNameId = lazadaPHDefaultNickNameId;
	}

	public String getLazadaTHDefaultNickNameId() {
		return lazadaTHDefaultNickNameId;
	}

	public void setLazadaTHDefaultNickNameId(String lazadaTHDefaultNickNameId) {
		this.lazadaTHDefaultNickNameId = lazadaTHDefaultNickNameId;
	}

	
	public String getEtsyUSDefaultAccount() {
		return etsyUSDefaultAccount;
	}

	public void setEtsyUSDefaultAccount(String etsyUSDefaultAccount) {
		this.etsyUSDefaultAccount = etsyUSDefaultAccount;
	}

	public String getEtsyUSDefaultNickNameId() {
		return etsyUSDefaultNickNameId;
	}

	public void setEtsyUSDefaultNickNameId(String etsyUSDefaultNickNameId) {
		this.etsyUSDefaultNickNameId = etsyUSDefaultNickNameId;
	}

	public String getEtsyConsumerKey() {
		return etsyConsumerKey;
	}

	public void setEtsyConsumerKey(String etsyConsumerKey) {
		this.etsyConsumerKey = etsyConsumerKey;
	}

	public String getEtsyConsumerSecret() {
		return etsyConsumerSecret;
	}

	public void setEtsyConsumerSecret(String etsyConsumerSecret) {
		this.etsyConsumerSecret = etsyConsumerSecret;
	}

	public static String getLazadaAccountDetails(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getLazadaSGDefaultAccount();
		case "ID":
			return Config.getConfig().getLazadaIDDefaultAccount();
		case "MY":
			return Config.getConfig().getLazadaMYDefaultAccount();
		case "PH":
			return Config.getConfig().getLazadaPHDefaultAccount();
		case "TH":
			return Config.getConfig().getLazadaTHDefaultAccount();
		default:
			return "";
		} 
	}
	
	public static String getLazadaNickNameID(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getLazadaSGDefaultNickNameId();
		case "ID":
			return Config.getConfig().getLazadaIDDefaultNickNameId();
		case "MY":
			return Config.getConfig().getLazadaMYDefaultNickNameId();
		case "PH":
			return Config.getConfig().getLazadaPHDefaultNickNameId();
		case "TH":
			return Config.getConfig().getLazadaTHDefaultNickNameId();
		default:
			return "";
		} 
	}

	public static String getEtsyAccountDetails(String countryCode) {
		switch (countryCode) {
		case "US":
			return Config.getConfig().getEtsyUSDefaultAccount();
		default:
			return "";
		}
	}

	public static String getEtsyNickNameID(String countryCode) {
		switch (countryCode) {
		case "US":
			return Config.getConfig().getEtsyUSDefaultNickNameId();
		default:
			return "";
		}
	}

	public static Config getConfig() {
		return (Config) context.getBean("Config");
	}

}
