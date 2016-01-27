package com.sellinall.listinglookup.database;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.mongodb.DB;
import com.mongodb.DBCollection;

public class DbUtilities {
	static ApplicationContext lookupContext = new AnnotationConfigApplicationContext(LookupCfg.class);
	static DB lookupDB = (DB) lookupContext.getBean("db");

	public static DBCollection getLookupDBCollection(String collectionName) {
		return lookupDB.getCollection(collectionName);
	}

}
