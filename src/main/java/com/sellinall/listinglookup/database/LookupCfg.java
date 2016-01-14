package com.sellinall.listinglookup.database;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.sellinall.listinglookup.config.Config;

@Configuration
public class LookupCfg {
	public @Bean DB db() throws Exception {
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		seeds.add(new ServerAddress(Config.getConfig().getLookupCollectionHostName(), Integer.parseInt(Config
				.getConfig().getLookupCollectionPort())));

		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		credentials.add(MongoCredential.createScramSha1Credential(Config.getConfig().getDbUserName(), Config
				.getConfig().getLookupCollectionDBName(), Config.getConfig().getDbPassword().toCharArray()));

		MongoClient mongoClient = new MongoClient(seeds, credentials);
		DB db = mongoClient.getDB(Config.getConfig().getLookupCollectionDBName());
		return db;
	}
}
