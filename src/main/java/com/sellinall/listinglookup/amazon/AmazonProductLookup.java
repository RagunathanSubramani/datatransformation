package com.sellinall.listinglookup.amazon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class AmazonProductLookup {
	public static JSONObject getProductFromSite(String searchParamType, String searchParam, String countryCode) {
		JSONObject newJsonObject = new JSONObject();
		String amazonresponseXML = AmazonUtil.getProduct(searchParamType, searchParam, countryCode);
		JSONObject amazonProduct = XML.toJSONObject(amazonresponseXML);
		JSONObject amazonItems = amazonProduct.getJSONObject("ItemLookupResponse").getJSONObject("Items");
		if (!searchParamType.equals("ASIN")) {
			JSONArray amazonUPCItemArray = makeArray(amazonItems.get("Item"));
			JSONObject amazonUPCItem = (JSONObject) amazonUPCItemArray.get(0);
			String tempASIN = amazonUPCItem.getString("ASIN");
			return getProductFromSite("ASIN", tempASIN, countryCode);
		}
		if (amazonItems.getJSONObject("Request").has("Errors")) {

			// create empty array when NameRecommendation is not present in
			// the response from Amazon.

			return amazonItems.getJSONObject("Request").getJSONObject("Errors");
		}
		JSONObject amazonItem = amazonItems.getJSONObject("Item");
		JSONObject parentAmazonItems = amazonItems;// initializing to parent
													// ASIN we will correct to
													// actual asin after
													// validating
		JSONObject parentAmazonItem = amazonItem;
		if (amazonItem.has("ParentASIN")) {// This is parent ASIN route
			// Check if ASIN passed is child ASIN else get the Parent obj
			String parentASIN = amazonItem.getString("ParentASIN");
			if (!searchParam.equals(parentASIN)) {
				String parentAmazonResponseXML = AmazonUtil.getProduct(searchParamType, parentASIN, countryCode);
				JSONObject parentAmazonProduct = XML.toJSONObject(parentAmazonResponseXML);
				parentAmazonItems = parentAmazonProduct.getJSONObject("ItemLookupResponse").getJSONObject("Items");
				parentAmazonItem = parentAmazonItems.getJSONObject("Item");

			}
			extractTitleAndDescription(newJsonObject, parentAmazonItem);

			if (parentAmazonItem.has("Variations")) {
				JSONObject variationsObject = parentAmazonItem.getJSONObject("Variations");
				appendChild(variationsObject, newJsonObject);
				newJsonObject.put("parentASIN", parentASIN);
			} else {
				// This is a temporary fix
				populateNormalRecord(searchParam, newJsonObject, amazonItem);
			}
		} else {
			// This is no variant route
			extractTitleAndDescription(newJsonObject, amazonItem);
			populateNormalRecord(searchParam, newJsonObject, amazonItem);
		}

		return newJsonObject;

	}

	private static void populateNormalRecord(String searchParam, JSONObject newJsonObject, JSONObject amazonItem) {
		HashSet<String> jsonImageArray = extractImageSet(amazonItem);
		newJsonObject.put("imageSet", jsonImageArray);
		newJsonObject.append("ASIN", searchParam);
	}

	private static void appendChild(JSONObject variationsObject, JSONObject newJsonObject) {
		// Get VariationDimension
		Object variantionDimensionArrayObject = variationsObject.getJSONObject("VariationDimensions").get(
				"VariationDimension");
		JSONArray variantionDimensionArray = makeArray(variantionDimensionArrayObject);
		Map<String, HashSet<String>> variants = new HashMap<String, HashSet<String>>();
		HashSet<String> jsonImageHashSet = new HashSet<String>();
		for (int i = 0; i < variantionDimensionArray.length(); i++) {
			String variantionDimension = (String) variantionDimensionArray.get(i);
			variants.put(variantionDimension, new HashSet<String>());
		}

		Object variantionitemArrayObject = variationsObject.get("Item");
		JSONArray variantionitemArray = makeArray(variantionitemArrayObject);
		for (int i = 0; i < variantionitemArray.length(); i++) {
			JSONObject item = (JSONObject) variantionitemArray.get(i);
			// newJsonObject.append("ImageSet", jsonImageArray);
			HashSet<String> variantImageSet = extractImageSet(item);
			jsonImageHashSet.addAll(variantImageSet);
			newJsonObject.append("ASIN", item.getString("ASIN"));

			JSONObject newItemObject = new JSONObject();
			newItemObject.put("imageSet", variantImageSet);
			newItemObject.put("ASIN", item.getString("ASIN"));

			Object jsonvariantionArrayObject = item.getJSONObject("VariationAttributes").get("VariationAttribute");
			JSONArray jsonvariantionArray = makeArray(jsonvariantionArrayObject);
			for (int j = 0; j < jsonvariantionArray.length(); j++) {
				JSONObject variationAttribute = (JSONObject) jsonvariantionArray.get(j);
				String variantName = variationAttribute.getString("Name");
				String variantValue = variationAttribute.get("Value").toString();

				// This is for child
				JSONObject childVariantDetail = new JSONObject();
				childVariantDetail.put("title", variantName);
				childVariantDetail.put("name", variantValue);
				newItemObject.append("variantDetails", childVariantDetail);

				// This is for parent
				HashSet<String> values = variants.get(variantName);
				values.add(variantValue);
			}
			newJsonObject.append("item", newItemObject);
		}
		newJsonObject.put("imageSet", jsonImageHashSet);

		insertVariants(newJsonObject, variants);
	}

	private static void insertVariants(JSONObject newJsonObject, Map<String, HashSet<String>> variants) {

		variants.forEach((key, value) -> {
			JSONObject variant = new JSONObject();
			variant.put("title", key);
			variant.put("names", value);
			newJsonObject.append("variants", variant);
		});
	}

	private static JSONArray makeArray(Object object) {
		JSONArray objArray = new JSONArray();
		if (object instanceof JSONArray) {
			objArray = (JSONArray) object;
		} else {
			objArray.put(object);
		}
		return objArray;
	}

	private static HashSet<String> extractImageSet(JSONObject amazonItems) {
		HashSet<String> extractedImages = new HashSet<String>();
		if (amazonItems.has("ImageSets")) {
			Object imageSetArrayObject = amazonItems.getJSONObject("ImageSets").get("ImageSet");
			JSONArray imageSetArray = makeArray(imageSetArrayObject);
			for (int i = 0; i < imageSetArray.length(); i++) {
				JSONObject imageSet = (JSONObject) imageSetArray.get(i);
				extractedImages.add(imageSet.getJSONObject("LargeImage").getString("URL"));
			}
		}
		return extractedImages;
	}

	private static void extractTitleAndDescription(JSONObject newJsonObject, JSONObject amazonItem) {
		// TODO Auto-generated method stub
		JSONObject allAttributes = amazonItem.getJSONObject("ItemAttributes");
		String title = allAttributes.getString("Title");
		if (amazonItem.has("EditorialReviews")) {
			String description = amazonItem.getJSONObject("EditorialReviews").getJSONObject("EditorialReview")
					.getString("Content");
			newJsonObject.put("itemDescription", description);
		}
		newJsonObject.put("itemTitle", title);
		newJsonObject.put("itemAttributes", allAttributes);
		extractCategory(newJsonObject, amazonItem);
	}

	private static void extractCategory(JSONObject newJsonObject, JSONObject amazonItem) {
		if (amazonItem.has("BrowseNodes")) {
			JSONObject browseNodes = amazonItem.getJSONObject("BrowseNodes");

			Object object = browseNodes.get("BrowseNode");
			JSONArray browseNodeArray = getJSONArray(object);

			JSONArray categoryIds = new JSONArray();
			for (int i = 0; i < browseNodeArray.length(); i++) {
				JSONObject browseNode = browseNodeArray.getJSONObject(i);
				categoryIds.put(String.valueOf(browseNode.getLong("BrowseNodeId")));
			}
			JSONArray categoryNames = getCategoryNames(browseNodeArray);
			for (int i = 0; i < categoryNames.length(); i++) {
				String categoryName = categoryNames.getString(i);
				categoryName = categoryName.replaceFirst("->Categories", "");
				categoryNames.put(i, categoryName);
			}
			newJsonObject.put("categoryNames", categoryNames);
			newJsonObject.put("categoryIds", categoryIds);
		}
	}

	private static JSONArray getJSONArray(Object object) {
		JSONArray jsonArray = new JSONArray();
		if (object instanceof JSONObject) {
			jsonArray.put(object);
		} else {
			jsonArray = (JSONArray) object;
		}
		return jsonArray;
	}

	private static JSONArray getCategoryNames(JSONArray browseNodeArray) {
		JSONArray categoryNames = new JSONArray();
		for (int i = 0; i < browseNodeArray.length(); i++) {
			JSONObject browseNode = browseNodeArray.getJSONObject(i);
			if (!browseNode.has("Ancestors")) {
				categoryNames.put(browseNode.getString("Name"));
			} else {
				Object innerNode = browseNode.getJSONObject("Ancestors").getJSONObject("BrowseNode");
				JSONArray innerBrowseNodeArray = getJSONArray(innerNode);
				JSONArray innerNames = getCategoryNames(innerBrowseNodeArray);
				for (int j = 0; j < innerNames.length(); j++) {
					categoryNames.put(innerNames.get(j) + "->" + browseNode.getString("Name"));
				}
			}
		}
		return categoryNames;
	}

}
