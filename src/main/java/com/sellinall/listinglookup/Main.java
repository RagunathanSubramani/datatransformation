package com.sellinall.listinglookup;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
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

import com.mudra.sellinall.util.NewHttpURLConnectionUtil;
import com.sellinall.listinglookup.category.CategoryMap;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.ebay.CategoryLookup;
import com.sellinall.listinglookup.product.ProductLookup;
import com.sun.jersey.api.client.ClientResponse;

public class Main {

	static Logger log = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {

		port(Integer.valueOf(System.getenv("PORT")));

		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8083";
		}

		Config.context = new ClassPathXmlApplicationContext("ConfigProperties.xml");

		get("/services/ebay/category/:countryCode/:categoryId", (request, response) -> {
			return CategoryLookup.getCategorySpecifics(request.params(":countryCode"), request.params(":categoryId"));
		});

		get("/services/ebay/category/categoryNamePath/:countryCode/:categoryId", (request, response) -> {
			return CategoryLookup.getCategoryNamePath(request.params(":countryCode"), request.params(":categoryId"));
		});

		get("/services/lazada/category/:countryCode/:categoryId",
				(request, response) -> {
					return com.sellinall.listinglookup.rocket.CategoryLookup.getCategorySpecifics(
							request.params(":countryCode"), request.params(":categoryId"));
				});

		get("/services/shopclues/category/:countryCode/:categoryId",
				(request, response) -> {
					return com.sellinall.listinglookup.shopclues.CategoryLookup.getCategorySpecifics(
							request.params(":countryCode"), request.params(":categoryId"));
				});

		get("/services/product/:searchParam",
				(request, response) -> {
					return ProductLookup.getMatchingProduct(request.params(":searchParam"),
							request.queryParams("countryCode"));
				});

		get("/services/categoryMap/:sourceChannel/:sourceCountryCode/:categoryId",
				(request, response) -> {
					return CategoryMap.getCategoryMap(request.params(":sourceChannel"),
							request.params("sourceCountryCode"), request.params("categoryId"),
							request.queryParams("targetChannel"), request.queryParams("targetCountryCode"));
				});

		put("/services/categoryMap", (request, response) -> {
			return CategoryMap.createMap(request.headers("Mudra"), request.body());
		});

		after((request, response) -> {
			setResponseHeaders(response);
		});

		before((request, response) -> {
			if (request.requestMethod().equals("OPTIONS")) {
				setResponseHeaders(response);
				halt(200);
			}
			if (request.requestMethod().equals("PUT")) {
				boolean isValidRequest = validate(request);
				if (!isValidRequest) {
					halt(401);
				}
			}
		});

	}

	private static boolean validate(Request request) {
		try {
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
				String userId = responseEntity.getString("userId");
				request.attribute("userId", userId);
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
