package com.sellinall.listinglookup;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.SparkBase.port;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sellinall.listinglookup.category.CategorySpecific;
import com.sellinall.listinglookup.category.FieldsMap;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.ebay.CategoryLookup;
import com.sellinall.listinglookup.product.ProductLookup;
import com.sellinall.util.AuthConstant;
import com.sellinall.util.HttpsURLConnectionUtil;

import spark.Request;
import spark.Response;


public class Main {

	static Logger log = Logger.getLogger(Main.class.getName());
	private static final String SERVERNAME = "listinglookup";

	public static void main(String[] args) {

		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8083";
		}
		port(Integer.valueOf(webPort));


		Config.context = new ClassPathXmlApplicationContext("ConfigProperties.xml");		
		Config.getConfig().setRagasiyam(System.getenv(AuthConstant.RAGASIYAM_KEY));

		get("/services/:channelName/category/:countryCode/:categoryId",
				(request, response) -> {
					try{

					String channelName = request.params("channelName");
					channelName = channelName.toLowerCase();
					switch (channelName) {
					case "ebay":
						return com.sellinall.listinglookup.ebay.CategoryLookup.getCategorySpecifics(
								request.params(":countryCode"), request.params(":categoryId"));
					case "lazada":					
						String accountNumber = Config.getLazadaAccountDetails(request.params(":countryCode"));
						String nickNameId = Config.getLazadaNickNameID(request.params(":countryCode"));
						
						return com.sellinall.listinglookup.lazada.CategoryLookup.getCategorySpecifics(
									request.params(":countryCode"), request.params(":categoryId"), accountNumber,
									nickNameId);
						case "etsy":
							return com.sellinall.listinglookup.etsy.CategoryLookup.getCategorySpecifics(
									request.params(":countryCode"), request.params(":categoryId"));
						case "shopee":
							return com.sellinall.listinglookup.shopee.CategoryLookup.getCategorySpecifics(
									request.params(":countryCode"), request.params(":categoryId"));
						case "elevenstreet":
							return com.sellinall.listinglookup.elevenStreet.CategoryLookup.getCategorySpecifics(
									request.params(":countryCode"), request.params(":categoryId"));
						case "jd":
							accountNumber = request.headers("accountNumber");
							nickNameId = request.queryParams("nickNameID");
							return com.sellinall.listinglookup.jd.CategoryLookup.getCategorySpecifics(
									request.params(":countryCode"), request.params(":categoryId"), accountNumber,
									nickNameId);
						case "zalora":
							accountNumber = Config.getZaloraAccountDetails(request.params(":countryCode"));
							nickNameId = Config.getZaloraNickNameID(request.params(":countryCode"));
							return com.sellinall.listinglookup.zalora.CategoryLookup.getCategorySpecifics(
										request.params(":countryCode"), request.params(":categoryId"), accountNumber,
										nickNameId);
						case "bukalapak":
							accountNumber = Config.getBukalapakAccountDetails(request.params(":countryCode"));
							nickNameId = Config.getBukalapakNickNameID(request.params(":countryCode"));
							return com.sellinall.listinglookup.bukalapak.CategoryLookup.getCategorySpecifics(
									request.params(":countryCode"), request.params(":categoryId"), accountNumber,
									nickNameId);
						case "tokopedia":
							accountNumber = Config.getTokopediaAccountDetails(request.params(":countryCode"));
							nickNameId = Config.getTokopediaNickNameID(request.params(":countryCode"));
							return com.sellinall.listinglookup.tokopedia.CategoryLookup.getCategorySpecifics(
									request.params(":countryCode"), request.params(":categoryId"), accountNumber,
									nickNameId);
						default:
							return com.sellinall.listinglookup.CategoryLookup.getCategorySpecifics(
								request.params(":countryCode"), request.params(":categoryId"), channelName);
					}
					}catch (Exception e){
						response.status(500);
						e.printStackTrace();
						return "500";
					}
				});
		
		get("/services/:channelName/category/:countryCode", (request, response) -> {
			String categoryName = request.params("channelName");
			String countryCode = request.params("countryCode");
			String callbackUrl = request.queryParams("callbackUrl");
			new Thread(() -> {
				try {
					refreshCategory(categoryName, countryCode, callbackUrl);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
			return "Processing";
		});

		get("/services/:channelName/category/categoryNamePath/:countryCode/:categoryId", (request, response) -> {
			try {
				String channelName = request.params("channelName");
				String accountNumber = request.headers("accountNumber");
				switch (channelName) {
				case "eBay":
					return CategoryLookup.getCategoryNamePath(request.params(":countryCode"),
							request.params(":categoryId"));
				case "qoo10":
				case "lazada":
				case "bukalapak":
				case "elevenStreet":
				case "shopee":
				case "etsy":
				case "shopify":
					return CategoryNameLookup.getCategoryNameFromSIA(request.params(":countryCode"),
							request.params(":categoryId"), request.params(":channelName"),
							request.queryParams("nicknameID"), accountNumber);
				default:
					return "";

				}
			} catch (Exception e) {
				e.printStackTrace();
				response.status(500);
				return "500";
			}
		});

		get("/services/product/:searchParam",
				(request, response) -> {
				try{
					return ProductLookup.getMatchingProduct(request.params(":searchParam"),
							request.queryParams("countryCode"));
				}catch (Exception e){
					e.printStackTrace();
					response.status(500);
					return "500";
				}

				});

		post("/services/fieldsMap/sourceChannel", (request, response) -> {
			try{
				return FieldsMap.postSourceChannelDetails(request.body(), request.queryParams("standardFormat"));
			}catch (Exception e){
				e.printStackTrace();
				response.status(500);
				return "500";
			}
		});

		get("/services/categorySpecificValues/:nicknameId/:categoryId", (request, response) -> {
			try{
			if (Boolean.parseBoolean(request.queryParams("standardFormat"))) {
				return CategorySpecific.getValues(request.params(":nicknameId"), request.params(":categoryId"),
						request.queryParams("countryCode"), request.queryParams("accountNumber"));
			} else {
				return CategorySpecific.getSiteFormatValues(request.params(":nicknameId"),
						request.params(":categoryId"), request.queryParams("countryCode"),
						request.queryParams("accountNumber"));
			}
			}catch (Exception e){
				e.printStackTrace();
				response.status(500);
				return "500";
			}
		});

		put("/services/fieldsMap", (request, response) -> {
			try{
				return FieldsMap.createMap(request.body());
			}catch (Exception e){
				e.printStackTrace();
				response.status(500);
				return "500";
			}
		});

		put("/services/categorySpecificValues/:nicknameId/:categoryId", (request, response) -> {
			try{
				return CategorySpecific.upsertValues(request.params(":nicknameId"), request.params(":categoryId"),
					request.queryParams("countryCode"), request.queryParams("accountNumber"), request.body());
			}catch (Exception e){
				e.printStackTrace();
				response.status(500);
				return "500";
			}
		});

		after((request, response) -> {
			setResponseHeaders(response);
		});

		before((request, response) -> {
			if (request.requestMethod().equals("OPTIONS")) {
				setResponseHeaders(response);
				halt(200);
			}
			if ((request.requestMethod().equals("PUT")) || (request.requestMethod().equals("POST"))
					|| (request.requestMethod().equals("GET")
							&& (request.pathInfo().startsWith("/services/categorySpecificValues")
									|| request.pathInfo().contains("lazada/category/")))) {
				boolean isValidRequest = validate(request);
				if (!isValidRequest) {
					response.status(401);
					setResponseHeaders(response);
					halt(401,"401");
					
				}
			}
		});

	}

	private static boolean validate(Request request) {
		try {
			if (request.headers(AuthConstant.RAGASIYAM_KEY) != null && Config.getConfig().getRagasiyam() != null
					&& checkValidUser(request.headers(AuthConstant.RAGASIYAM_KEY).split(","),
							Config.getConfig().getRagasiyam().split(","))) {
				return true;
			}

			String accountNumQueryParam = request.queryParams("accountNumber");
			String mudraToken = request.headers("Mudra");
			Map<String, String> header = new HashMap<String, String>();
			header.put("authType", "SELLinALL");
			header.put("Content-type", "application/json");
			org.codehaus.jettison.json.JSONObject payload = new JSONObject();
			payload.put("mudra", mudraToken);
			payload.put("method", request.requestMethod());
			payload.put("path", request.pathInfo());
			payload.put("serverName", SERVERNAME);
			String url = Config.getConfig().getSIAAuthServerURL() + "/authToken";
			JSONObject response = HttpsURLConnectionUtil.doPost(url, payload.toString(), header);
			// TODO: map the response http code
			if (response.getInt("httpCode") == HttpStatus.OK_200) {
				JSONObject responseEntity = new JSONObject(response.getString("payload"));
				String accountNumber = responseEntity.getString("accountNumber");
				request.attribute("accountNumber", accountNumber);
				if (accountNumQueryParam != null && !accountNumQueryParam.equals(accountNumber)) {
					return false;
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error("exception while calling auth serv");
			e.printStackTrace();
			return false;
		}
	}

	private static void setResponseHeaders(Response response) {
		response.header("Access-Control-Allow-Origin", "*");
		response.header("Connection", "keep-alive");
		response.header("keep-alive", "timeout=5, max=100");
		response.header("Access-Control-Allow-Headers",
				"origin, content-type, accept, authorization, Mudra, keep-alive, Connection, username");
		response.header("Access-Control-Allow-Credentials", "true");
		response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		response.header("Access-Control-Max-Age", "1209600");
	}

	public static boolean checkValidUser(String ragasiyam[], String originalvalue[]) {
		boolean flag = false;
		for (int i = 0; i < ragasiyam.length; i++) {
			for (int j = 0; j < originalvalue.length; j++) {
				if (ragasiyam[i] != null && originalvalue[j] != null && ragasiyam[i].equals(originalvalue[j])) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
	
	private static void refreshCategory(String channelName, String countryCode, String callbackUrl)
			throws JSONException, IOException {
		String newCategory = "";
		String accountNumber = "";
		String nickNameId = "";
		try {
			switch (channelName) {
			case "lazada":
				accountNumber = Config.getLazadaAccountDetails(countryCode);
				nickNameId = Config.getLazadaNickNameID(countryCode);
				newCategory =  new com.sellinall.listinglookup.lazada.BuildCategory().buildNewCategiryList(accountNumber, nickNameId);
				break;
			case "qoo10":
				newCategory = com.sellinall.listinglookup.qoo10.BuildCategory.buildNewCategiryList(countryCode);
				break;
			case "eBay":
				newCategory = com.sellinall.listinglookup.ebay.BuildCategory.buildNewCategiryList(countryCode)
						.toString();
				break;
			case "etsy":
				newCategory = com.sellinall.listinglookup.etsy.BuildCategory.buildNewCategoryList(countryCode)
						.toString();
				break;
			case "shopee":
				newCategory = com.sellinall.listinglookup.shopee.BuildCategory.buildNewCategoryList(countryCode)
						.toString();
				break;
			case "zalora":
				accountNumber = Config.getZaloraAccountDetails(countryCode);
				nickNameId = Config.getZaloraNickNameID(countryCode);
				newCategory = com.sellinall.listinglookup.zalora.BuildCategory
						.buildNewCategoryList(accountNumber, nickNameId).toString();
				break;
			case "99SME":
				newCategory = com.sellinall.listinglookup.sme.BuildCategory.buildNewCategoryList(countryCode)
						.toString();
				break;
			case "bukalapak":
				newCategory = com.sellinall.listinglookup.bukalapak.BuildCategory.buildNewCategoryList(countryCode)
						.toString();
				break;
			case "tokopedia":
				newCategory = com.sellinall.listinglookup.tokopedia.BuildCategory.buildNewCategoryList(countryCode)
						.toString();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!newCategory.isEmpty()) {
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/json");
			header.put(AuthConstant.RAGASIYAM_KEY, Config.getConfig().getRagasiyam());
			JSONObject payload = new JSONObject();
			payload.put("data", new JSONArray(newCategory));
			HttpsURLConnectionUtil.doPost(callbackUrl, payload.toString(), header);
		}
	}

}
