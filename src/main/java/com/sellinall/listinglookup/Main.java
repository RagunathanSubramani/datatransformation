package com.sellinall.listinglookup;

import static spark.Spark.get;
import static spark.SparkBase.port;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.listinglookup.ebay.CategoryLookup;

public class Main {

	public static void main(String[] args) {

		port(Integer.valueOf(System.getenv("PORT")));

		Config.context = new ClassPathXmlApplicationContext("ConfigProperties.xml");

		get("/services/ebay/category/:countryCode/:categoryId", (request, response) -> {
			return CategoryLookup.getCategorySpecifics(request.params(":countryCode"), request.params(":categoryId"));
		});

	}

}
