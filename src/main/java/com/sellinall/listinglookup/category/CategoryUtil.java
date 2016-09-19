package com.sellinall.listinglookup.category;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class CategoryUtil {
	public static final String DEFAULT_ACCOUNT_NUMBER = "default";

	private static final Map<String, List<String>> accountAndSiteSpecificFieldsMap = Collections
			.unmodifiableMap(new HashMap<String, List<String>>() {
				{
					put("eBay", Arrays.asList("storeFront.storeCategoryID"));
					put("etsy", Arrays.asList("shopSectionId"));
				}
			});

	public static boolean isFieldAccountAndSiteSpecific(String site, String field) {
		return (accountAndSiteSpecificFieldsMap.containsKey(site) && accountAndSiteSpecificFieldsMap.get(site)
				.contains(field));
	}

	public static JSONObject getJSONObjectFromDotNotation(String field, Object value) {
		String[] fields = field.split("\\.", 2);
		JSONObject result = new JSONObject();
		if (fields.length == 2) {
			result.put(fields[0], getJSONObjectFromDotNotation(fields[1], value));
		} else {
			result.put(fields[0], value);
		}
		return result;
	}
}
