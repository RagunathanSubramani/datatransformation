package com.sellinall.listinglookup.category;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryUtil {
	public static final String DEFAULT_ACCOUNT_NUMBER = "default";

	private static final Map<String, List<String>> accountAndSiteSpecificFieldsMap = Collections
			.unmodifiableMap(new HashMap<String, List<String>>() {
				{
					put("eBay", Arrays.asList("storeFront.storeCategoryID"));
				}
			});

	public static boolean isFieldAccountAndSiteSpecific(String site, String field) {
		return (accountAndSiteSpecificFieldsMap.containsKey(site) && accountAndSiteSpecificFieldsMap.get(site)
				.contains(field));
	}
}
