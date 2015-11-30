package com.sellinall.listinglookup.amazon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.sellinall.listinglookup.amazon.AmazonUtil;
import com.sellinall.listinglookup.database.DbUtilities;

public class AmazonProductLookup {
	private static final long thirtyDays = 30 * 24 * 60 * 60;



	private static String findSearchParamType(String searchParam) {
		// TODO Auto-generated method stub
		//Need to write code to find whether it is ASIN or UPC or EAN
		return "ASIN";
	}

	

	public static JSONObject getProductFromSite(String searchParamType, String searchParam) {
		JSONObject newJsonObject = new JSONObject();
		String amazonresponseXML = AmazonUtil.getProduct(searchParamType, searchParam);
		
		JSONObject amazonProduct = XML.toJSONObject(amazonresponseXML);
		JSONObject amazonItems = amazonProduct.getJSONObject("ItemLookupResponse").getJSONObject("Items");
		JSONObject amazonItem = amazonItems.getJSONObject("Item");
		JSONObject parentAmazonItems = amazonItems;//initiasing to parent ASIN we will correct to actual asin after validating
		JSONObject parentAmazonItem = amazonItem;
		if (amazonItems.has("Errors")) {
		
			// create empty array when NameRecommendation is not present in
			// the response from eBay.
			
			return amazonItems.getJSONObject("Errors");
		}
		if(amazonItem.has("ParentASIN")){//This is parent ASIN route
			//Check if ASIN passed is child ASIN else get the Parent obj
			String parentASIN = amazonItem.getString("ParentASIN");
			if(!searchParam.equals(parentASIN)){
				String parentAmazonResponseXML = AmazonUtil.getProduct(searchParamType, parentASIN);
				JSONObject parentAmazonProduct = XML.toJSONObject(parentAmazonResponseXML);
				parentAmazonItems = parentAmazonProduct.getJSONObject("ItemLookupResponse").getJSONObject("Items");
				parentAmazonItem = parentAmazonItems.getJSONObject("Item");
				
			}
			JSONObject variationsObject = parentAmazonItem.getJSONObject("Variations");
			appendChild(variationsObject,newJsonObject);
			newJsonObject.put("parentASIN", parentASIN);
		}else{
			//This is no variant route 
			HashSet<String> jsonImageArray = extractImageSet(amazonItem);
			System.out.println(jsonImageArray);
			newJsonObject.append("imageSet", jsonImageArray);
			newJsonObject.append("ASIN", searchParam);
		}

		return newJsonObject;
	
	}

	private static void appendChild(JSONObject variationsObject,
			JSONObject newJsonObject) {
		//Get VariationDimension
		Object variantionDimensionArrayObject = variationsObject.getJSONObject("VariationDimensions").get("VariationDimension");
		JSONArray variantionDimensionArray = makeArray(variantionDimensionArrayObject);
		Map<String,HashSet<String>> variants = new HashMap<String,HashSet<String>>();
		HashSet<String> jsonImageHashSet = new HashSet<String>();
		for(int i = 0;i<variantionDimensionArray.length();i++){
			String variantionDimension = (String) variantionDimensionArray.get(i);
			variants.put(variantionDimension, new HashSet<String>());
		}		
		
		
		Object variantionitemArrayObject = variationsObject.get("Item");
		JSONArray variantionitemArray = makeArray(variantionitemArrayObject);
		JSONObject newVariationObject = new JSONObject();
		for(int i = 0;i<variantionitemArray.length();i++){
			JSONObject item = (JSONObject) variantionitemArray.get(i);
			//newJsonObject.append("ImageSet", jsonImageArray);
			HashSet<String> variantImageSet = extractImageSet(item);
			jsonImageHashSet.addAll(variantImageSet);
			newJsonObject.append("ASIN",item.getString("ASIN"));
			
			JSONObject newItemObject = new JSONObject();
			newItemObject.put("imageSet", variantImageSet);
			newItemObject.put("ASIN", item.getString("ASIN"));
			
			Object jsonvariantionArrayObject = item.getJSONObject("VariationAttributes").get("VariationAttribute");
			JSONArray jsonvariantionArray = makeArray(jsonvariantionArrayObject);
			for(int j = 0;j<jsonvariantionArray.length();j++){
				JSONObject variationAttribute = (JSONObject) jsonvariantionArray.get(j);
				String variantName = variationAttribute.getString("Name");
				String variantValue = variationAttribute.getString("Value");
				
				//This is for child
				JSONObject childVariantDetail = new JSONObject();
				childVariantDetail.put("title", variantName);
				childVariantDetail.put("name", variantValue);
				newItemObject.append("variantDetails",childVariantDetail);
				
				//This is for parent
				HashSet<String> values = variants.get(variantName);
				values.add(variantValue);
			}
			newJsonObject.append("item", newItemObject);
		}
		newJsonObject.put("imageSet", jsonImageHashSet);
		
		insertVariants(newJsonObject, variants);
	}



	private static void insertVariants(JSONObject newJsonObject,
			Map<String, HashSet<String>> variants) {

		variants.forEach((key,value) -> 
			{
				JSONObject variant = new JSONObject();
				variant.put("title", key);
				variant.put("names", value);
				newJsonObject.append("variants", variant);
			});
	}

	private static JSONArray makeArray(Object object) {
		JSONArray objArray = new JSONArray();
		if(object instanceof JSONArray){
			objArray = (JSONArray) object;
		}else{
			objArray.put(object);
		}
		return objArray;
	}

	private static HashSet<String> extractImageSet(JSONObject amazonItems) {
		// TODO Auto-generated method stub
		Object imageSetArrayObject = amazonItems.getJSONObject("ImageSets").get("ImageSet");
		JSONArray imageSetArray = makeArray(imageSetArrayObject);
		HashSet<String> extractedImages = new HashSet<String>();
		for(int i = 0;i<imageSetArray.length();i++){
			JSONObject imageSet = (JSONObject) imageSetArray.get(i);
			extractedImages.add(imageSet.getJSONObject("LargeImage").getString("URL"));
		}
		return extractedImages;
	}



	private static BasicDBObject persistToDB(String countryCode, String categoryId, JSONObject itemSpecifics,
			JSONObject categoryFeatures) {
		BasicDBObject filterField1 = new BasicDBObject("countryCode", countryCode);
		BasicDBObject filterField2 = new BasicDBObject("categoryId", categoryId);
		BasicDBList and = new BasicDBList();
		and.add(filterField1);
		and.add(filterField2);

		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("$and", and);

		long expriyTime = (System.currentTimeMillis() / 1000L) + thirtyDays;
		BasicDBObject updateData = new BasicDBObject();
		updateData.put("expiryTime", expriyTime);
		updateData.put("itemSpecifics", JSON.parse(itemSpecifics.toString()));
		updateData.put("features", JSON.parse(categoryFeatures.toString()));

		BasicDBObject setObject = new BasicDBObject("$set", updateData);
		DBCollection table = DbUtilities.getLookupDBCollection("ebayCategoryLookup");
		table.update(searchQuery, setObject, true, false);
		updateData.removeField("expiryTime");
		return updateData;
	}

}
