package com.sellinall.listinglookup;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.SparkBase.port;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import spark.Response;

import com.sellinall.listinglookup.category.CategoryMap;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.ebay.CategoryLookup;
import com.sellinall.listinglookup.product.ProductLookup;

public class Main {

	public static void main(String[] args) {

		port(Integer.valueOf(System.getenv("PORT")));

		Config.context = new ClassPathXmlApplicationContext("ConfigProperties.xml");

		get("/services/ebay/category/:countryCode/:categoryId", (request, response) -> {
			return CategoryLookup.getCategorySpecifics(request.params(":countryCode"), request.params(":categoryId"));
		});

		get("/services/ebay/category/categoryNamePath/:countryCode/:categoryId", (request, response) -> {
			return CategoryLookup.getCategoryNamePath(request.params(":countryCode"), request.params(":categoryId"));
		});

		get("/services/lazada/category/:countryCode/:categoryId", (request, response) -> {
			return com.sellinall.listinglookup.rocket.CategoryLookup.getCategorySpecifics(
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

		after((request, response) -> {
			setResponseHeaders(response);
		});

		before((request, response) -> {
			if (request.requestMethod().equals("OPTIONS")) {
				setResponseHeaders(response);
				halt(200);
			}
		});

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
