package com.sellinall.listinglookup.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.sellinall.listinglookup.config.Config;

@Configuration
public class LookupCfg {
	public @Bean DB db() throws Exception {
		MongoClientURI uri = new MongoClientURI(Config.getConfig().getLookupDBURI());
		MongoClient mongoClient = new MongoClient(uri);
		DB db = mongoClient.getDB(Config.getConfig().getLookupDBName());
		return db;
	}
}
