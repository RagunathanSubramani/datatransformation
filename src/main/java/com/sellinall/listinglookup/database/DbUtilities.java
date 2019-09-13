package com.sellinall.listinglookup.database;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.mongodb.DB;
import com.mongodb.DBCollection;

public class DbUtilities {
	static ApplicationContext userContext = new AnnotationConfigApplicationContext(UserCfg.class);
	static DB userDB = (DB) userContext.getBean("db");
	
	static ApplicationContext lookupContext = new AnnotationConfigApplicationContext(LookupCfg.class);
	static DB lookupDB = (DB) lookupContext.getBean("db");

	static ApplicationContext inventoryContext = new AnnotationConfigApplicationContext(InventoryCfg.class);
	static DB inventoryDB = (DB) inventoryContext.getBean("db");
	static DB inventoryRODB = (DB) inventoryContext.getBean("dbRO");

	public static DBCollection getLookupDBCollection(String collectionName) {
		return lookupDB.getCollection(collectionName);
	}
	
	public static DBCollection getUserDBCollection(String collectionName) {
		return userDB.getCollection(collectionName);
	}

	public static DBCollection getInventoryDBCollection(String collectionName) {
		return inventoryDB.getCollection(collectionName);
	}

	public static DBCollection getROInventoryDBCollection(String collectionName) {
		return inventoryRODB.getCollection(collectionName);
	}

}
