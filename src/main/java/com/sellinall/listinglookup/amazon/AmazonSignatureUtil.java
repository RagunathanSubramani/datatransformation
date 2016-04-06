package com.sellinall.listinglookup.amazon;

/**
 * 
 */

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * @author Vikraman
 * @company sellinall
 *
 * This class will be used as a common utility for amzon realted stuff
 * It can be broken up into many pieces later based on usecases
 */
public class AmazonSignatureUtil {
	static Logger log = Logger.getLogger(AmazonSignatureUtil.class.getName());
    private static String DEFAULT_ENCODING = "UTF-8";

    
    /**
     * @param parameters
     * @param key
     * @param serviceURL
     * @return
     * @throws SignatureException
     * 
     * This is the only public function of this class
     * it will sign the parameters and give a string which can be used in signature
     * 
     */
    public static String signParameters(Map<String, String> parameters, String key, String serviceURL)
            throws  SignatureException {

        String signatureVersion = parameters.get("SignatureVersion");
        String algorithm = "HmacSHA256";
        String stringToSign = null;
        if ("2".equals(signatureVersion)) {
            algorithm = parameters.get("SignatureMethod");
            parameters.put("SignatureMethod", algorithm);
            stringToSign = calculateStringToSignV2(parameters, serviceURL);
        } else {
            throw new SignatureException("Invalid Signature Version specified");
        }
        return sign(stringToSign, key, algorithm);
    }
    
    public static String signProductAdvertisingAPIParameters(Map<String, String> parameters, String key, String serviceURL)
            throws  SignatureException {

        String algorithm = "HmacSHA256";
        String stringToSign = null;
        stringToSign = calculateStringToSignV2ForGET(parameters, serviceURL);
        return sign(stringToSign, key, algorithm);
    }
    
	public static String createContentMD5(String body) {

	     try {
             MessageDigest instance = MessageDigest.getInstance("MD5");
             return new String(Base64.encodeBase64(instance.digest(body.getBytes())));
         } catch (NoSuchAlgorithmException e) {
             // Errorhandling. Should not happen though!
         }
	     return  null;
	}
	
    /**
     * Calculate String to Sign for SignatureVersion 2
     * @param parameters request parameters
     * @param serviceURL 
     * @return String to Sign
     * @throws java.security.SignatureException
     */
    private static String calculateStringToSignV2ForGET(Map<String, String> parameters, String serviceURL)
            throws SignatureException {
        StringBuilder data = new StringBuilder();
        data.append("GET");
        data.append("\n");
        URI endpoint = null;
        try {
            endpoint = new URI(serviceURL);
        } catch (URISyntaxException ex) {
            log.error("URI Syntax Exception"+ ex);
            throw new SignatureException("URI Syntax Exception thrown " +
                    "while constructing string to sign", ex);
        }
        data.append(endpoint.getHost());
        if (!usesAStandardPort(serviceURL)) {
            data.append(":");
            data.append(endpoint.getPort());
        }
        data.append("\n");
        String uri = endpoint.getPath();
        if (uri == null || uri.length() == 0) {
            uri = "/";
        }
        data.append(uri);
        data.append("\n");
        Map<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(parameters);
        Iterator<Map.Entry<String, String>> pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            String key = pair.getKey();
            data.append(urlEncode(key));
            data.append("=");
            String value = pair.getValue();
            data.append(urlEncode(value));
            if (pairs.hasNext()) {
                data.append("&");
            }
        }
        return data.toString();
    }
    
    /**
     * Calculate String to Sign for SignatureVersion 2
     * @param parameters request parameters
     * @param serviceURL 
     * @return String to Sign
     * @throws java.security.SignatureException
     */
    private static String calculateStringToSignV2(Map<String, String> parameters, String serviceURL)
            throws SignatureException {
        StringBuilder data = new StringBuilder();
        data.append("POST");
        data.append("\n");
        URI endpoint = null;
        try {
            endpoint = new URI(serviceURL);
        } catch (URISyntaxException ex) {
            log.error("URI Syntax Exception"+ ex);
            throw new SignatureException("URI Syntax Exception thrown " +
                    "while constructing string to sign", ex);
        }
        data.append(endpoint.getHost());
        if (!usesAStandardPort(serviceURL)) {
            data.append(":");
            data.append(endpoint.getPort());
        }
        data.append("\n");
        String uri = endpoint.getPath();
        if (uri == null || uri.length() == 0) {
            uri = "/";
        }
        data.append(uri);
        data.append("\n");
        Map<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(parameters);
        Iterator<Map.Entry<String, String>> pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            String key = pair.getKey();
            data.append(urlEncode(key));
            data.append("=");
            String value = pair.getValue();
            data.append(urlEncode(value));
            if (pairs.hasNext()) {
                data.append("&");
            }
        }
        return data.toString();
    }
    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     */
    private static String sign(String data, String key, String algorithm) throws SignatureException {
        byte [] signature;
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.getBytes(), algorithm));
            signature = Base64.encodeBase64(mac.doFinal(data.getBytes(DEFAULT_ENCODING)));
        } catch (Exception e) {
            throw new SignatureException("Failed to generate signature: " + e.getMessage(), e);
        }

        return new String(signature);
    }
    
    private static boolean usesHttps(String url){
        URL urlToCheck;
        try {
            urlToCheck = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        if (urlToCheck.getProtocol().equals("https")){
            return true;
        }else
        {
            return false;
        }
    }

    private static int extractPortNumber(String url, boolean usesHttps) {
        URL urlToCheck;
        try {
            urlToCheck = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("not a URL", e);
        }
        
        int portNumber = urlToCheck.getPort();
        if (portNumber == -1){
            // no port was specified
            if (usesHttps){
                // it uses https, so we should return the standard https port number
                return 443;
            }else
            {
                // it uses http, so we should return the standard http port number
                return 80;
            }
        }else
        {
            return portNumber;
        }
    }

    private static boolean usesAStandardPort(String url) {
        boolean usesHttps = usesHttps(url);
        int portNumber = extractPortNumber(url, usesHttps);
        return usesHttps && portNumber == 443
            || !usesHttps && portNumber == 80;
    }

    private static String urlEncode(String rawValue) {
        String value = rawValue==null ? "" : rawValue;
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, DEFAULT_ENCODING)
                                        .replace("+", "%20")
                                        .replace("*", "%2A")
                                        .replace("%7E","~");
        } catch (UnsupportedEncodingException ex) {
            log.error("Unsupported Encoding Exception"+ ex);
            throw new RuntimeException(ex);
        }
        return encoded;
    }

}