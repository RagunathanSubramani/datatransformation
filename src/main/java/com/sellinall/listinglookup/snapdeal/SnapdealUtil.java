package com.sellinall.listinglookup.snapdeal;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mudra.sellinall.util.HttpURLConnectionUtil;
import com.sellinall.listinglookup.CategoryLookup;
import com.sellinall.listinglookup.config.Config;

import freemarker.template.TemplateException;

public class SnapdealUtil {
	static Logger log = Logger.getLogger(CategoryLookup.class.getName());

	private static final Map<String, String> SnapdealApiHost = Collections
			.unmodifiableMap(new HashMap<String, String>() {

				private static final long serialVersionUID = 1L;

				{
					put("IN", Config.getConfig().getSnapdealUrl());

				}
			});

	public static JSONObject getCategorySpecifics(String countryCode, String categoryId) throws JSONException,
			IOException, TemplateException {
		String url = SnapdealApiHost.get(countryCode) + "/categories/" + categoryId + "/attributes";

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		headers.put("X-Auth-Token", Config.getConfig().getSnapdealAuthToken());
		headers.put("ClientId", Config.getConfig().getSnapdealClientId());

		JSONObject response = new JSONObject(HttpURLConnectionUtil.doGetWithHeader(url, headers));
		log.debug("response:" + response);
		return response.getJSONObject("payload");
	}
}
