package com.sellinall.listinglookup.category;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sellinall.listinglookup.database.DbUtilities;

public class CategoryMap {

	public static BasicDBObject getCategoryMap(String sourceChannel, String sourceCountryCode, String categoryId,
			String targetChannel, String targetCountryCode) {
		BasicDBObject map = getMapFromDB(sourceChannel, sourceCountryCode, categoryId, targetChannel, targetCountryCode);

		if (map != null) {
			return map;
		}
		return new BasicDBObject();
	}

	private static BasicDBObject getMapFromDB(String sourceChannel, String sourceCountryCode, String categoryId,
			String targetChannel, String targetCountryCode) {
		String sourceChannelAndCountry = sourceChannel + "-" + sourceCountryCode;

		BasicDBObject searchQuery = new BasicDBObject(sourceChannelAndCountry + ".categoryId", categoryId);

		DBObject fields = new BasicDBObject();
		fields.put("_id", 0);
		if (targetChannel != null && targetCountryCode != null) {
			String targetChannelAndCountry = targetChannel + "-" + targetCountryCode;
			fields.put(sourceChannelAndCountry, 1);
			fields.put(targetChannelAndCountry, 1);
		}

		DBCollection table = DbUtilities.getLookupDBCollection("categoryMap");
		BasicDBObject map = (BasicDBObject) table.findOne(searchQuery, fields);
		return map;
	}

}
