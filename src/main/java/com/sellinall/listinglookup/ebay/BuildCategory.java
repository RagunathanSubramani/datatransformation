package com.sellinall.listinglookup.ebay;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class BuildCategory {
	public static ArrayList<String> categoryList = new ArrayList<String>();

	public static ArrayList<String> buildNewCategiryList(String countryCode) {
		categoryList = new ArrayList<String>();
		String readParent = EBayUtil.getParentCategory(EBayUtil.getSiteId(countryCode));
		System.out.println("XML file" + readParent);
		JSONObject firstLevel = XML.toJSONObject(readParent);
		JSONArray categories = firstLevel.getJSONObject("GetCategoriesResponse").getJSONObject("CategoryArray")
				.getJSONArray("Category");
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			processChildCategories("" + category.getInt("CategoryID"), EBayUtil.getSiteId(countryCode));
		}
		return categoryList;
	}

	// Process all child categories
	private static void processChildCategories(String parentCategoryID, String siteId) throws JSONException {
		String readChildren = EBayUtil.getChildCategory(parentCategoryID, siteId);
		JSONObject firstLevel = XML.toJSONObject(readChildren);
		JSONObject categoryArray = firstLevel.getJSONObject("GetCategoriesResponse").getJSONObject("CategoryArray");
		JSONArray list = createJSONArray(categoryArray, "Category");
		HashMap<String, JSONArray> map = new HashMap<String, JSONArray>();
		JSONObject topParent = (JSONObject) list.get(0);
		for (int i = 0; i < list.length(); i++) {
			JSONObject category = list.getJSONObject(i);
			mapParentToChildren(category, map);
		}
		processChildren(topParent.getString("CategoryName"), topParent.getInt("CategoryID"), map);
	}

	private static void mapParentToChildren(JSONObject category, HashMap<String, JSONArray> map) throws JSONException {
		int parentID = category.getInt("CategoryParentID");
		int childrenID = category.getInt("CategoryID");
		if (parentID == childrenID) {
			// This one for Top node like level =1
			// No need add this one to map
			return;
		}
		JSONObject child = new JSONObject();
		child.put("name", "" + category.get("CategoryName"));
		child.put("id", childrenID);
		JSONArray children = new JSONArray();
		if (map.containsKey("" + parentID)) {
			children = map.get("" + parentID);
		}
		children.put(child);
		map.put("" + parentID, children);
	}

	private static void processChildren(String name, int categoryId, HashMap<String, JSONArray> map)
			throws JSONException {
		if (map.containsKey("" + categoryId)) {
			JSONArray children = map.get("" + categoryId);
			for (int i = 0; i < children.length(); i++) {
				JSONObject child = children.getJSONObject(i);
				processChildren(name + " / " + child.getString("name"), child.getInt("id"), map);
			}
		} else {
			categoryList.add("\"" + name + " ## " + categoryId + "\"");
		}
	}

	public static JSONArray createJSONArray(JSONObject jsonObject, String key) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		if (jsonObject.get(key) instanceof JSONArray) {
			jsonArray = jsonObject.getJSONArray(key);
		} else {
			jsonArray.put(jsonObject.get(key));
		}
		return jsonArray;
	}

}
