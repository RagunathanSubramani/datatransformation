package com.sellinall.listinglookup.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.sellinall.listinglookup.config.Config;

@Configuration
public class LookupCfg {
	public @Bean MongoDbFactory dbFactory() throws Exception {
		return new SimpleMongoDbFactory(new MongoClient(Config.getConfig().getLookupCollectionHostName(),
				Integer.parseInt(Config.getConfig().getLookupCollectionPort())), Config.getConfig()
				.getLookupCollectionDBName());
	}

	public @Bean MongoTemplate mongoTemplate() throws Exception {
		MongoTemplate mongoTemplate = new MongoTemplate(dbFactory());
		return mongoTemplate;
	}
}
