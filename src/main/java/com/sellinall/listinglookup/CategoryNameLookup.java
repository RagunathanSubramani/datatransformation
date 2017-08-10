package com.sellinall.listinglookup;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;

import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.NewHttpURLConnectionUtil;
import com.sun.jersey.api.client.ClientResponse;

public class CategoryNameLookup {
	static Logger log = Logger.getLogger(CategoryNameLookup.class.getName());

	public static Object getCategoryNameFromSIA(String countryCode, String categoryID, String channelName) {
		String categoryName = "";
		String url = Config.getConfig().getSIACategoryNameUrl() + "?channelName=" + channelName + "&countryCode="
				+ countryCode + "&categoryID=" + categoryID;
		ClientResponse responseObj = NewHttpURLConnectionUtil.doGet(url);
		if (responseObj.getStatus() == HttpStatus.OK_200) {
			categoryName = responseObj.getEntity(String.class);
		} else {
			log.error("SIAadmin server returned" + responseObj.getStatus() + "for URL " + url);
		}
		JSONObject response = new JSONObject();
		response.put("categoryNamePath", categoryName);
		return response;
	}
}
