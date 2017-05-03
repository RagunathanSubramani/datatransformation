package com.sellinall.listinglookup.etsy;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.sellinall.listinglookup.CategoryLookup;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.HttpURLConnectionUtil;

import freemarker.template.TemplateException;

public class EtsyUtil {
	static Logger log = Logger.getLogger(CategoryLookup.class.getName());

	private static final Map<String, String> EtsyApiHost = Collections.unmodifiableMap(new HashMap<String, String>() {

		private static final long serialVersionUID = 1L;

		{
			put("US", Config.getConfig().getEtsyUrl());
		}
	});

	public static JSONObject getCategorySpecifics(BasicDBObject credentialInfo, String countryCode, String categoryId)
			throws JSONException, IOException, TemplateException {
		String url = EtsyApiHost.get(countryCode) + categoryId + "/properties";
		BasicDBObject etsy = (BasicDBObject) credentialInfo.get("etsy");
		BasicDBObject postHelper = (BasicDBObject) credentialInfo.get("postHelper");
		String consumerKey = Config.getConfig().getEtsyConsumerKey();
		String consumerSecret = Config.getConfig().getEtsyConsumerSecret();
		String oauthToken = postHelper.getString("oauthToken");
		String oauthTokenSecret = postHelper.getString("oauthTokenSecret");
		return HttpURLConnectionUtil.doGetWithOauth(url, consumerKey, consumerSecret, null, oauthToken,
				oauthTokenSecret);
	}
}
