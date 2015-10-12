package com.sellinall.listinglookup.database;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;

import com.mongodb.DBCollection;
import com.sellinall.listinglookup.config.Config;

public class DbUtilities {
	static ApplicationContext ctx = new AnnotationConfigApplicationContext(LookupCfg.class);
	static MongoOperations mongoOperation = (MongoOperations) ctx.getBean("mongoTemplate");

	public static DBCollection getLookupDBCollection(String collectionName) {
		DBCollection table = mongoOperation.getCollection(collectionName);
		Boolean boo = table.getDB().authenticate(Config.getConfig().getDbUserName(),
				Config.getConfig().getDbPassword().toCharArray());
		if (boo) {
			return table;
		}
		return table;
	}

}
