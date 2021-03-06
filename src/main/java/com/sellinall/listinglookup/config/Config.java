package com.sellinall.listinglookup.config;

import org.springframework.context.ApplicationContext;

public class Config {
	public static ApplicationContext context;

	private String EbayPostURL;
	private String EbayAppName;
	private String EbayDevName;
	private String EbayCertName;
	private String EbayOpenApiURL;
	private String EbayToken;

	private String lookupDBURI;
	private String lookupDBName;
	
	private String userCollectionDBURI;
	private String userCollectionDBName;

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
	private String shopeeUrl;
	private String qoo10URL;

	private String lazadaURL;
	private String elevenStreetUrl;
	private String jdURL;
	private String magentoURL;

	private String qoo10SGDefaultNickNameId;
	private String qoo10SGDefaultAccount;


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
	
	private String shopeeSGDefaultAccount;
	private String shopeeTWDefaultAccount;
	private String shopeeIDDefaultAccount;
	private String shopeeMYDefaultAccount;
	private String shopeePHDefaultAccount;
	private String shopeeTHDefaultAccount;
	private String shopeeVNDefaultAccount;
	
	private String shopeeSGDefaultNickNameId;
	private String shopeeTWDefaultNickNameId;
	private String shopeeIDDefaultNickNameId;
	private String shopeeMYDefaultNickNameId;
	private String shopeePHDefaultNickNameId;
	private String shopeeTHDefaultNickNameId;
	private String shopeeVNDefaultNickNameId;
	
	private long shopeeClientID;
	private String shopeeClientSecret;

	private String etsyUSDefaultAccount;
	private String etsyUSDefaultNickNameId;
	private String etsyConsumerKey;
	private String etsyConsumerSecret;
	private String SIACategoryNameUrl;

	private String elevenStreetMYDefaultAccount;
	private String elevenStreetMYDefaultNickNameId;
	private String siaSmeUrl;
	private String smeSGDefaultAccount;
	private String smeSGDefaultNickNameId;

	private String siaBukalapakUrl;
	private String bukalapakIDDefaultAccount;
	private String bukalapakIDDefaultNickNameId;

	private String siaBlibliUrl;
	private String blibliDefaultAccount;
	private String blibliDefaultNickNameId;

	private String siaTokopediaUrl;
	private String tokopediaIDDefaultAccount;
	private String tokopediaIDDefaultNickNameId;

	private String zaloraSGDefaultAccount;
	private String zaloraIDDefaultAccount;
	private String zaloraMYDefaultAccount;
	private String zaloraPHDefaultAccount;
	private String zaloraTHDefaultAccount;

	private String zaloraSGDefaultNickNameId;
	private String zaloraIDDefaultNickNameId;
	private String zaloraMYDefaultNickNameId;
	private String zaloraPHDefaultNickNameId;
	private String zaloraTHDefaultNickNameId;

	private String rocketEcomAdaptorUrl;
	private String siaAdminUrl;
	private String siaInventoryUrl;

	public String getRagasiyam() {
		return Ragasiyam;
	}

	public void setRagasiyam(String ragasiyam) {
		Ragasiyam = ragasiyam;
	}

	public String getJdURL() {
		return jdURL;
	}

	public void setJdURL(String jdURL) {
		this.jdURL = jdURL;
	}

	public String getGrant_type() {
		return grant_type;
	}

	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}

	public String getQoo10URL() {
		return qoo10URL;
	}

	public String getQoo10SGDefaultAccount() {
		return qoo10SGDefaultAccount;
	}

	public void setQoo10SGDefaultAccount(String qoo10sgDefaultAccount) {
		qoo10SGDefaultAccount = qoo10sgDefaultAccount;
	}

	public void setQoo10URL(String qoo10url) {
		qoo10URL = qoo10url;
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

	public String getQoo10SGDefaultNickNameId() {
		return qoo10SGDefaultNickNameId;
	}

	public void setQoo10SGDefaultNickNameId(String qoo10sgDefaultNickNameId) {
		qoo10SGDefaultNickNameId = qoo10sgDefaultNickNameId;
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

	public String getEbayToken() {
		return EbayToken;
	}

	public void setEbayToken(String ebayToken) {
		EbayToken = ebayToken;
	}

	public String getEtsyUrl() {
		return etsyUrl;
	}

	public void setEtsyUrl(String etsyUrl) {
		this.etsyUrl = etsyUrl;
	}

	public String getLookupDBURI() {
		return lookupDBURI;
	}

	public void setLookupDBURI(String lookupDBURI) {
		this.lookupDBURI = lookupDBURI;
	}

	public String getLookupDBName() {
		return lookupDBName;
	}

	public void setLookupDBName(String lookupDBName) {
		this.lookupDBName = lookupDBName;
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

	public String getShopeeUrl() {
		return shopeeUrl;
	}

	public void setShopeeUrl(String shopeeUrl) {
		this.shopeeUrl = shopeeUrl;
	}

	public long getShopeeClientID() {
		return shopeeClientID;
	}

	public void setShopeeClientID(long shopeeClientID) {
		this.shopeeClientID = shopeeClientID;
	}

	public String getShopeeClientSecret() {
		return shopeeClientSecret;
	}

	public void setShopeeClientSecret(String shopeeClientSecret) {
		this.shopeeClientSecret = shopeeClientSecret;
	}

	public String getShopeeSGDefaultAccount() {
		return shopeeSGDefaultAccount;
	}

	public void setShopeeSGDefaultAccount(String shopeeSGDefaultAccount) {
		this.shopeeSGDefaultAccount = shopeeSGDefaultAccount;
	}

	public String getShopeeTWDefaultAccount() {
		return shopeeTWDefaultAccount;
	}

	public void setShopeeTWDefaultAccount(String shopeeTWDefaultAccount) {
		this.shopeeTWDefaultAccount = shopeeTWDefaultAccount;
	}

	public String getShopeeIDDefaultAccount() {
		return shopeeIDDefaultAccount;
	}

	public void setShopeeIDDefaultAccount(String shopeeIDDefaultAccount) {
		this.shopeeIDDefaultAccount = shopeeIDDefaultAccount;
	}

	public String getShopeeMYDefaultAccount() {
		return shopeeMYDefaultAccount;
	}

	public void setShopeeMYDefaultAccount(String shopeeMYDefaultAccount) {
		this.shopeeMYDefaultAccount = shopeeMYDefaultAccount;
	}

	public String getShopeePHDefaultAccount() {
		return shopeePHDefaultAccount;
	}

	public void setShopeePHDefaultAccount(String shopeePHDefaultAccount) {
		this.shopeePHDefaultAccount = shopeePHDefaultAccount;
	}

	public String getShopeeTHDefaultAccount() {
		return shopeeTHDefaultAccount;
	}

	public void setShopeeTHDefaultAccount(String shopeeTHDefaultAccount) {
		this.shopeeTHDefaultAccount = shopeeTHDefaultAccount;
	}

	public String getShopeeVNDefaultAccount() {
		return shopeeVNDefaultAccount;
	}

	public void setShopeeVNDefaultAccount(String shopeeVNDefaultAccount) {
		this.shopeeVNDefaultAccount = shopeeVNDefaultAccount;
	}

	public String getShopeeSGDefaultNickNameId() {
		return shopeeSGDefaultNickNameId;
	}

	public void setShopeeSGDefaultNickNameId(String shopeeSGDefaultNickNameId) {
		this.shopeeSGDefaultNickNameId = shopeeSGDefaultNickNameId;
	}

	public String getShopeeTWDefaultNickNameId() {
		return shopeeTWDefaultNickNameId;
	}

	public void setShopeeTWDefaultNickNameId(String shopeeTWDefaultNickNameId) {
		this.shopeeTWDefaultNickNameId = shopeeTWDefaultNickNameId;
	}

	public String getShopeeIDDefaultNickNameId() {
		return shopeeIDDefaultNickNameId;
	}

	public void setShopeeIDDefaultNickNameId(String shopeeIDDefaultNickNameId) {
		this.shopeeIDDefaultNickNameId = shopeeIDDefaultNickNameId;
	}

	public String getShopeeMYDefaultNickNameId() {
		return shopeeMYDefaultNickNameId;
	}

	public void setShopeeMYDefaultNickNameId(String shopeeMYDefaultNickNameId) {
		this.shopeeMYDefaultNickNameId = shopeeMYDefaultNickNameId;
	}

	public String getShopeePHDefaultNickNameId() {
		return shopeePHDefaultNickNameId;
	}

	public void setShopeePHDefaultNickNameId(String shopeePHDefaultNickNameId) {
		this.shopeePHDefaultNickNameId = shopeePHDefaultNickNameId;
	}

	public String getShopeeTHDefaultNickNameId() {
		return shopeeTHDefaultNickNameId;
	}

	public void setShopeeTHDefaultNickNameId(String shopeeTHDefaultNickNameId) {
		this.shopeeTHDefaultNickNameId = shopeeTHDefaultNickNameId;
	}

	public String getShopeeVNDefaultNickNameId() {
		return shopeeVNDefaultNickNameId;
	}

	public void setShopeeVNDefaultNickNameId(String shopeeVNDefaultNickNameId) {
		this.shopeeVNDefaultNickNameId = shopeeVNDefaultNickNameId;
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


	public static String getQoo10AccountDetails(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getQoo10SGDefaultAccount();
		default:
			return "";
		}
	}

	public static String getQoo10NickNameID(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getQoo10SGDefaultNickNameId();
		default:
			return "";
		}
	}


	public static String getShopeeAccountDetails(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getShopeeSGDefaultAccount();
		case "TW":
			return Config.getConfig().getShopeeTWDefaultAccount();
		case "ID":
			return Config.getConfig().getShopeeIDDefaultAccount();
		case "MY":
			return Config.getConfig().getShopeeMYDefaultAccount();
		case "PH":
			return Config.getConfig().getShopeePHDefaultAccount();
		case "TH":
			return Config.getConfig().getShopeeTHDefaultAccount();
		case "VN":
			return Config.getConfig().getShopeeVNDefaultAccount();
		default:
			return "";
		} 
	}
	
	public static String getShopeeNickNameID(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getShopeeSGDefaultNickNameId();
		case "TW":
			return Config.getConfig().getShopeeSGDefaultNickNameId();
		case "ID":
			return Config.getConfig().getShopeeIDDefaultNickNameId();
		case "MY":
			return Config.getConfig().getShopeeMYDefaultNickNameId();
		case "PH":
			return Config.getConfig().getShopeePHDefaultNickNameId();
		case "TH":
			return Config.getConfig().getShopeeTHDefaultNickNameId();
		case "VN":
			return Config.getConfig().getShopeeSGDefaultNickNameId();
		default:
			return "";
		} 
	}

	public static String getElevenStreetAccountDetails(String countryCode){
		switch (countryCode) {
		case "MY":
			return Config.getConfig().getElevenStreetMYDefaultAccount();
		default:
			return "";
		}
	}

	public static String getElevenStreetNickNameID(String countryCode){
		switch (countryCode) {
		case "MY":
			return Config.getConfig().getElevenStreetMYDefaultNickNameId();
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

	public static String get99SMEAccountDetails(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getSmeSGDefaultAccount();
		default:
			return "";
		}
	}

	public static String get99SMENickNameID(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getSmeSGDefaultNickNameId();
		default:
			return "";
		}
	}

	public static String getTokopediaAccountDetails(String countryCode) {
		switch (countryCode) {
		case "ID":
			return Config.getConfig().getTokopediaIDDefaultAccount();
		default:
			return "";
		}
	}

	public static String getTokopediaNickNameID(String countryCode) {
		switch (countryCode) {
		case "ID":
			return Config.getConfig().getTokopediaIDDefaultNickNameId();
		default:
			return "";
		}
	}

	public static String getBukalapakAccountDetails(String countryCode) {
		switch (countryCode) {
		case "ID":
			return Config.getConfig().getBukalapakIDDefaultAccount();
		default:
			return "";
		}
	}

	public static String getBukalapakNickNameID(String countryCode) {
		switch (countryCode) {
		case "ID":
			return Config.getConfig().getBukalapakIDDefaultNickNameId();
		default:
			return "";
		}
	}

	public static Config getConfig() {
		return (Config) context.getBean("Config");
	}

	public String getSIACategoryNameUrl() {
		return SIACategoryNameUrl;
	}

	public void setSIACategoryNameUrl(String sIACategoryNameUrl) {
		SIACategoryNameUrl = sIACategoryNameUrl;
	}

	public String getLazadaURL() {
		return lazadaURL;
	}

	public void setLazadaURL(String lazadaURL) {
		this.lazadaURL = lazadaURL;
	}

	public String getElevenStreetMYDefaultAccount() {
		return elevenStreetMYDefaultAccount;
	}

	public void setElevenStreetMYDefaultAccount(String elevenStreetMYDefaultAccount) {
		this.elevenStreetMYDefaultAccount = elevenStreetMYDefaultAccount;
	}

	public String getElevenStreetMYDefaultNickNameId() {
		return elevenStreetMYDefaultNickNameId;
	}

	public void setElevenStreetMYDefaultNickNameId(String elevenStreetMYDefaultNickNameId) {
		this.elevenStreetMYDefaultNickNameId = elevenStreetMYDefaultNickNameId;
	}

	public String getElevenStreetUrl() {
		return elevenStreetUrl;
	}

	public void setElevenStreetUrl(String elevenStreetUrl) {
		this.elevenStreetUrl = elevenStreetUrl;
	}

	public String getZaloraSGDefaultAccount() {
		return zaloraSGDefaultAccount;
	}

	public void setZaloraSGDefaultAccount(String zaloraSGDefaultAccount) {
		this.zaloraSGDefaultAccount = zaloraSGDefaultAccount;
	}

	public String getZaloraIDDefaultAccount() {
		return zaloraIDDefaultAccount;
	}

	public void setZaloraIDDefaultAccount(String zaloraIDDefaultAccount) {
		this.zaloraIDDefaultAccount = zaloraIDDefaultAccount;
	}

	public String getZaloraMYDefaultAccount() {
		return zaloraMYDefaultAccount;
	}

	public void setZaloraMYDefaultAccount(String zaloraMYDefaultAccount) {
		this.zaloraMYDefaultAccount = zaloraMYDefaultAccount;
	}

	public String getZaloraPHDefaultAccount() {
		return zaloraPHDefaultAccount;
	}

	public void setZaloraPHDefaultAccount(String zaloraPHDefaultAccount) {
		this.zaloraPHDefaultAccount = zaloraPHDefaultAccount;
	}

	public String getZaloraTHDefaultAccount() {
		return zaloraTHDefaultAccount;
	}

	public void setZaloraTHDefaultAccount(String zaloraTHDefaultAccount) {
		this.zaloraTHDefaultAccount = zaloraTHDefaultAccount;
	}

	public String getZaloraSGDefaultNickNameId() {
		return zaloraSGDefaultNickNameId;
	}

	public void setZaloraSGDefaultNickNameId(String zaloraSGDefaultNickNameId) {
		this.zaloraSGDefaultNickNameId = zaloraSGDefaultNickNameId;
	}

	public String getZaloraIDDefaultNickNameId() {
		return zaloraIDDefaultNickNameId;
	}

	public void setZaloraIDDefaultNickNameId(String zaloraIDDefaultNickNameId) {
		this.zaloraIDDefaultNickNameId = zaloraIDDefaultNickNameId;
	}

	public String getZaloraMYDefaultNickNameId() {
		return zaloraMYDefaultNickNameId;
	}

	public void setZaloraMYDefaultNickNameId(String zaloraMYDefaultNickNameId) {
		this.zaloraMYDefaultNickNameId = zaloraMYDefaultNickNameId;
	}

	public String getZaloraPHDefaultNickNameId() {
		return zaloraPHDefaultNickNameId;
	}

	public void setZaloraPHDefaultNickNameId(String zaloraPHDefaultNickNameId) {
		this.zaloraPHDefaultNickNameId = zaloraPHDefaultNickNameId;
	}

	public String getZaloraTHDefaultNickNameId() {
		return zaloraTHDefaultNickNameId;
	}

	public void setZaloraTHDefaultNickNameId(String zaloraTHDefaultNickNameId) {
		this.zaloraTHDefaultNickNameId = zaloraTHDefaultNickNameId;
	}

	public static String getZaloraAccountDetails(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getZaloraSGDefaultAccount();
		case "ID":
			return Config.getConfig().getZaloraIDDefaultAccount();
		case "MY":
			return Config.getConfig().getZaloraMYDefaultAccount();
		case "PH":
			return Config.getConfig().getZaloraPHDefaultAccount();
		case "TH":
			return Config.getConfig().getZaloraTHDefaultAccount();
		default:
			return "";
		} 
	}
	
	public static String getZaloraNickNameID(String countryCode){
		switch (countryCode) {
		case "SG":
			return Config.getConfig().getZaloraSGDefaultNickNameId();
		case "ID":
			return Config.getConfig().getZaloraIDDefaultNickNameId();
		case "MY":
			return Config.getConfig().getZaloraMYDefaultNickNameId();
		case "PH":
			return Config.getConfig().getZaloraPHDefaultNickNameId();
		case "TH":
			return Config.getConfig().getZaloraTHDefaultNickNameId();
		default:
			return "";
		} 
	}

	public String getRocketEcomAdaptorUrl() {
		return rocketEcomAdaptorUrl;
	}

	public void setRocketEcomAdaptorUrl(String rocketEcomAdaptorUrl) {
		this.rocketEcomAdaptorUrl = rocketEcomAdaptorUrl;
	}

	public String getSmeSGDefaultAccount() {
		return smeSGDefaultAccount;
	}

	public void setSmeSGDefaultAccount(String smeSGDefaultAccount) {
		this.smeSGDefaultAccount = smeSGDefaultAccount;
	}

	public String getSmeSGDefaultNickNameId() {
		return smeSGDefaultNickNameId;
	}

	public void setSmeSGDefaultNickNameId(String smeSGDefaultNickNameId) {
		this.smeSGDefaultNickNameId = smeSGDefaultNickNameId;
	}

	public String getSiaSmeUrl() {
		return siaSmeUrl;
	}

	public void setSiaSmeUrl(String siaSmeUrl) {
		this.siaSmeUrl = siaSmeUrl;
	}

	public String getSiaBukalapakUrl() {
		return siaBukalapakUrl;
	}

	public void setSiaBukalapakUrl(String siaBukalapakUrl) {
		this.siaBukalapakUrl = siaBukalapakUrl;
	}

	public String getBukalapakIDDefaultAccount() {
		return bukalapakIDDefaultAccount;
	}

	public void setBukalapakIDDefaultAccount(String bukalapakIDDefaultAccount) {
		this.bukalapakIDDefaultAccount = bukalapakIDDefaultAccount;
	}

	public String getBukalapakIDDefaultNickNameId() {
		return bukalapakIDDefaultNickNameId;
	}

	public void setBukalapakIDDefaultNickNameId(String bukalapakIDDefaultNickNameId) {
		this.bukalapakIDDefaultNickNameId = bukalapakIDDefaultNickNameId;
	}

	public String getTokopediaIDDefaultAccount() {
		return tokopediaIDDefaultAccount;
	}

	public void setTokopediaIDDefaultAccount(String tokopediaIDDefaultAccount) {
		this.tokopediaIDDefaultAccount = tokopediaIDDefaultAccount;
	}

	public String getTokopediaIDDefaultNickNameId() {
		return tokopediaIDDefaultNickNameId;
	}

	public void setTokopediaIDDefaultNickNameId(String tokopediaIDDefaultNickNameId) {
		this.tokopediaIDDefaultNickNameId = tokopediaIDDefaultNickNameId;
	}

	public String getSiaTokopediaUrl() {
		return siaTokopediaUrl;
	}

	public void setSiaTokopediaUrl(String siaTokopediaUrl) {
		this.siaTokopediaUrl = siaTokopediaUrl;
	}

	public String getSiaAdminUrl() {
		return siaAdminUrl;
	}

	public void setSiaAdminUrl(String siaAdminUrl) {
		this.siaAdminUrl = siaAdminUrl;
	}

	public String getSiaInventoryUrl() {
		return siaInventoryUrl;
	}

	public void setSiaInventoryUrl(String siaInventoryUrl) {
		this.siaInventoryUrl = siaInventoryUrl;
	}

	public String getMagentoURL() {
		return magentoURL;
	}

	public void setMagentoURL(String magentoURL) {
		this.magentoURL = magentoURL;
	}

	public String getSiaBlibliUrl() {
		return siaBlibliUrl;
	}

	public void setSiaBlibliUrl(String siaBlibliUrl) {
		this.siaBlibliUrl = siaBlibliUrl;
	}

	public String getBlibliDefaultAccount() {
		return blibliDefaultAccount;
	}

	public void setBlibliDefaultAccount(String blibliIDDefaultAccount) {
		this.blibliDefaultAccount = blibliIDDefaultAccount;
	}

	public String getBlibliDefaultNickNameId() {
		return blibliDefaultNickNameId;
	}

	public void setBlibliDefaultNickNameId(String blibliIDDefaultNickNameId) {
		this.blibliDefaultNickNameId = blibliIDDefaultNickNameId;
	}

	public String getUserCollectionDBURI() {
		return userCollectionDBURI;
	}

	public void setUserCollectionDBURI(String userCollectionDBURI) {
		this.userCollectionDBURI = userCollectionDBURI;
	}

	public String getUserCollectionDBName() {
		return userCollectionDBName;
	}

	public void setUserCollectionDBName(String userCollectionDBName) {
		this.userCollectionDBName = userCollectionDBName;
	}
	
}