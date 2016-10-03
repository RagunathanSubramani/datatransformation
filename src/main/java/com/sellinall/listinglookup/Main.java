package com.sellinall.listinglookup;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.SparkBase.port;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import spark.Request;
import spark.Response;

import com.sellinall.listinglookup.category.CategorySpecific;
import com.sellinall.listinglookup.category.FieldsMap;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.ebay.CategoryLookup;
import com.sellinall.listinglookup.product.ProductLookup;
import com.sellinall.util.NewHttpURLConnectionUtil;
import com.sun.jersey.api.client.ClientResponse;

public class Main {

	static Logger log = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {

		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8083";
		}
		port(Integer.valueOf(webPort));


		Config.context = new ClassPathXmlApplicationContext("ConfigProperties.xml");

		get("/services/:channelName/category/:countryCode/:categoryId",
				(request, response) -> {
					try{

					String channelName = request.params("channelName");
					switch (channelName) {
					case "ebay":
						return com.sellinall.listinglookup.ebay.CategoryLookup.getCategorySpecifics(
								request.params(":countryCode"), request.params(":categoryId"));
					case "lazada":					
						return com.sellinall.listinglookup.rocket.CategoryLookup.getCategorySpecifics(
									request.params(":countryCode"), request.params(":categoryId"),
									request.attribute("accountNumber").toString(), request.queryParams("nickNameId"));
					default:
						return com.sellinall.listinglookup.CategoryLookup.getCategorySpecifics(
								request.params(":countryCode"), request.params(":categoryId"), channelName);
					}
					}catch (Exception e){
						response.status(500);
						return "500";
					}
				});

		get("/services/ebay/category/categoryNamePath/:countryCode/:categoryId", (request, response) -> {
			try{
				return CategoryLookup.getCategoryNamePath(request.params(":countryCode"), request.params(":categoryId"));
			}catch (Exception e){
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
					response.status(500);
					return "500";
				}

				});

		post("/services/fieldsMap/sourceChannel", (request, response) -> {
			try{
				return FieldsMap.postSourceChannelDetails(request.body(), request.queryParams("standardFormat"));
			}catch (Exception e){
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
				response.status(500);
				return "500";
			}
		});

		put("/services/fieldsMap", (request, response) -> {
			try{
				return FieldsMap.createMap(request.body());
			}catch (Exception e){
				response.status(500);
				return "500";
			}
		});

		put("/services/categorySpecificValues/:nicknameId/:categoryId", (request, response) -> {
			try{
				return CategorySpecific.upsertValues(request.params(":nicknameId"), request.params(":categoryId"),
					request.queryParams("countryCode"), request.queryParams("accountNumber"), request.body());
			}catch (Exception e){
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
			String isSIAServer = request.headers("SIAServer");
			if (isSIAServer != null && isSIAServer.equals("true")) {
				return true;
			}
			String accountNumQueryParam = request.queryParams("accountNumber");
			String mudraToken = request.headers("Mudra");
			Map<String, String> header = new HashMap<String, String>();
			header.put("authType", "facebook");
			org.codehaus.jettison.json.JSONObject payload = new JSONObject();
			payload.put("mudra", mudraToken);
			String url = Config.getConfig().getSIAAuthServerURL() + "/authToken";

			ClientResponse response = NewHttpURLConnectionUtil.doPostWithHeader(url, payload, header, "json");
			// TODO: map the response http code
			if (response.getStatus() == HttpStatus.OK_200) {
				JSONObject responseEntity = new JSONObject(response.getEntity(String.class));
				String accountNumber = responseEntity.getString("userId");
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
				"origin, content-type, accept, authorization, Mudra, keep-alive, Connection");
		response.header("Access-Control-Allow-Credentials", "true");
		response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		response.header("Access-Control-Max-Age", "1209600");
	}

}
