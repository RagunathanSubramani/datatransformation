package com.sellinall.listinglookup.etsy;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.sellinall.listinglookup.CategoryLookup;
import com.sellinall.listinglookup.config.Config;
import com.sellinall.util.HttpURLConnectionUtil;

import freemarker.template.TemplateException;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;

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

	public static JSONObject getJSONResponse(HttpResponse httpResponse) throws IOException, JSONException {
		InputStream inputStream = httpResponse.getEntity().getContent();
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		String output = writer.toString();
		JSONObject response = null;
		if (output.startsWith("{")) {
			response = new JSONObject(output);
		} else {
			response = new JSONObject();
			response.put("failureReason", output);
		}
		return response;
	}

	public static HttpResponse submitGetRequest(String url, String consumerKey, String consumerSecret,
			String oauthVerifier, String tempOauthToken, String tempOauthTokenSecret) {

		OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
		oAuthConsumer.setTokenWithSecret(tempOauthToken, tempOauthTokenSecret);

		if (oauthVerifier != null) {
			HttpParameters params = new HttpParameters();
			params.put("oauth_verifier", oauthVerifier);
			oAuthConsumer.setAdditionalParameters(params);
		}

		oAuthConsumer.setSigningStrategy(new AuthorizationHeaderSigningStrategy());

		CloseableHttpClient client = HttpClientBuilder.create().build();

		HttpRequestBase httpRequest = null;
		URI uri = null;

		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		httpRequest = new HttpGet(uri);
		httpRequest.setHeader("http.protocol.content-charset", "UTF-8");

		try {
			oAuthConsumer.sign(httpRequest);
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		}

		HttpResponse httpResponse = null;
		try {
			HttpHost target = new HttpHost(uri.getHost(), -1, uri.getScheme());
			httpResponse = client.execute(target, httpRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return httpResponse;
	}
}
