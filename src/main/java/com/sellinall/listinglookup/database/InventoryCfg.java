package com.sellinall.listinglookup.database;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.sellinall.listinglookup.config.Config;

public class InventoryCfg {
	public @Bean DB db() throws Exception {
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		String[] hostNames = Config.getConfig().getInventoryCollectionHostName().split(",");
		String[] ports = Config.getConfig().getInventoryCollectionPort().split(",");
		for (int i = 0; i < hostNames.length; i++) {
			seeds.add(new ServerAddress(hostNames[i], Integer.parseInt(ports[i])));
		}

		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		credentials.add(MongoCredential.createScramSha1Credential(Config.getConfig().getDbUserName(), Config
				.getConfig().getInventoryCollectionDBName(), Config.getConfig().getDbPassword().toCharArray()));

		MongoClient mongoClient = new MongoClient(seeds, credentials);
		DB db = mongoClient.getDB(Config.getConfig().getInventoryCollectionDBName());
		return db;
	}
	public @Bean DB dbRO() throws Exception {
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		String[] hostNames = Config.getConfig().getInventoryCollectionHostName().split(",");
		String[] ports = Config.getConfig().getInventoryCollectionPort().split(",");
		for (int i = 0; i < hostNames.length; i++) {
			seeds.add(new ServerAddress(hostNames[i], Integer.parseInt(ports[i])));
		}

		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		credentials.add(MongoCredential.createScramSha1Credential(Config.getConfig().getDbUserName(), Config
				.getConfig().getInventoryCollectionDBName(), Config.getConfig().getDbPassword().toCharArray()));

		ReadPreference readPreference = ReadPreference.secondaryPreferred();
		MongoClientOptions.Builder builder =  MongoClientOptions.builder();
		builder.readPreference(readPreference);
		MongoClientOptions options = builder.build();
		MongoClient mongoClient = new MongoClient(seeds, credentials,options  );
		DB db = mongoClient.getDB(Config.getConfig().getInventoryCollectionDBName());
		return db;
	}
}
