package com.amazon.pay.api;
public class UtilTest {








































	@org.junit.Test
	public void urlEncode() throws java.lang.Exception {
		java.lang.String expectedUrl = com.amazon.pay.api.Util.urlEncode("/live/v2/in-store/refund", true);
		org.junit.Assert.assertEquals(expectedUrl, "/live/v2/in-store/refund");

		java.lang.String expectedUrl1 = com.amazon.pay.api.Util.urlEncode("/live/v2/in-store/refund", false);
		org.junit.Assert.assertEquals(expectedUrl1, "%2Flive%2Fv2%2Fin-store%2Frefund");
	}

	@org.junit.Test
	public void uriWithASpaceTest() throws java.lang.Exception {
		java.lang.String expectedUrl = com.amazon.pay.api.Util.urlEncode("/ /foo", true);
		org.junit.Assert.assertEquals(expectedUrl, "/%20/foo");
	}

	@org.junit.Test
	public void uriWithRedundantSlashTest() throws java.lang.Exception {
		java.lang.String expectedUrl = com.amazon.pay.api.Util.urlEncode("//", true);
		org.junit.Assert.assertEquals(expectedUrl, "/");
	}

	@org.junit.Test
	public void uriWithRedundantSlashesTest() throws java.lang.Exception {
		java.lang.String expectedUrl = com.amazon.pay.api.Util.urlEncode("///foo//", true);
		org.junit.Assert.assertEquals(expectedUrl, "/foo/");
	}

	@org.junit.Test
	public void uriWithUnreservedCharactersTest() throws java.lang.Exception {
		java.lang.String expectedUrl = com.amazon.pay.api.Util.urlEncode("/-._~0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", true);
		org.junit.Assert.assertEquals(expectedUrl, "/-._~0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
	}

	@org.junit.Test
	public void uriWithUTF8CharactersTest() throws java.lang.Exception {
		java.lang.String expectedUrl = com.amazon.pay.api.Util.urlEncode("/ሴ", true);
		org.junit.Assert.assertEquals(expectedUrl, "/%E1%88%B4");
	}

	@org.junit.Test
	public void lowerCase() throws java.lang.Exception {
		java.lang.String str1 = "Signature";
		java.lang.String str2 = "signature";
		java.lang.String str3 = "SIGNATURE";

		org.junit.Assert.assertEquals(com.amazon.pay.api.Util.lowerCase(str1), "signature");
		org.junit.Assert.assertEquals(com.amazon.pay.api.Util.lowerCase(str2), "signature");
		org.junit.Assert.assertEquals(com.amazon.pay.api.Util.lowerCase(str3), "signature");
	}

	@org.junit.Test
	public void getServiceURI() throws java.lang.Exception {
		// Environment is Sandbox
		final com.amazon.pay.api.PayConfiguration payConfiguration1 = new com.amazon.pay.api.PayConfiguration().setPublicKeyId(
		"XXXXXXXXXXXXXXXXXXXXXXXX").setRegion(
		com.amazon.pay.api.types.Region.EU).setEnvironment(
		com.amazon.pay.api.types.Environment.SANDBOX);

		java.net.URI expectedURL = new java.net.URI("https://pay-api.amazon.eu/sandbox/v2/in-store/merchantScan");
		java.net.URI actualURL = com.amazon.pay.api.Util.getServiceURI(payConfiguration1, com.amazon.pay.api.ServiceConstants.INSTORE_MERCHANT_SCAN);
		org.junit.Assert.assertEquals(expectedURL, actualURL);

		// Environment is Live
		final com.amazon.pay.api.PayConfiguration payConfiguration2 = new com.amazon.pay.api.PayConfiguration().setPublicKeyId(
		"XXXXXXXXXXXXXXXXXXXXXXXX").setRegion(
		com.amazon.pay.api.types.Region.EU).setEnvironment(
		com.amazon.pay.api.types.Environment.LIVE);
		expectedURL = new java.net.URI("https://pay-api.amazon.eu/live/v2/in-store/refund");
		actualURL = com.amazon.pay.api.Util.getServiceURI(payConfiguration2, com.amazon.pay.api.ServiceConstants.INSTORE_REFUND);
		org.junit.Assert.assertEquals(expectedURL, actualURL);

		// Environment is Null (i.e Default value is live)
		final com.amazon.pay.api.PayConfiguration payConfiguration3 = new com.amazon.pay.api.PayConfiguration().setPublicKeyId(
		"XXXXXXXXXXXXXXXXXXXXXXXX").setRegion(
		com.amazon.pay.api.types.Region.EU);

		expectedURL = new java.net.URI("https://pay-api.amazon.eu/live/v2/in-store/refund");
		actualURL = com.amazon.pay.api.Util.getServiceURI(payConfiguration3, com.amazon.pay.api.ServiceConstants.INSTORE_REFUND);
		org.junit.Assert.assertEquals(expectedURL, actualURL);

		// With Algorithm set in payConfiguration
		final com.amazon.pay.api.PayConfiguration payConfigurationWithAlgorithm = new com.amazon.pay.api.PayConfiguration().setPublicKeyId(
		"XXXXXXXXXXXXXXXXXXXXXXXX").setRegion(
		com.amazon.pay.api.types.Region.EU).setAlgorithm(
		"AMZN-PAY-RSASSA-PSS-V2");

		actualURL = com.amazon.pay.api.Util.getServiceURI(payConfigurationWithAlgorithm, com.amazon.pay.api.ServiceConstants.INSTORE_REFUND);
		org.junit.Assert.assertEquals(expectedURL, actualURL);
	}

	@org.junit.Test
	public void getServiceURIForEnvironmentSpecificKeys() throws java.lang.Exception {
		final com.amazon.pay.api.PayConfiguration payConfiguration1 = new com.amazon.pay.api.PayConfiguration().setPublicKeyId(
		"LIVE-XXXXXXXXXXXXXXXXXXXXXXXX").setRegion(
		com.amazon.pay.api.types.Region.EU);

		java.net.URI expectedURL = new java.net.URI("https://pay-api.amazon.eu/v2/in-store/merchantScan");
		java.net.URI actualURL = com.amazon.pay.api.Util.getServiceURI(payConfiguration1, com.amazon.pay.api.ServiceConstants.INSTORE_MERCHANT_SCAN);
		org.junit.Assert.assertEquals(expectedURL, actualURL);

		final com.amazon.pay.api.PayConfiguration payConfiguration2 = new com.amazon.pay.api.PayConfiguration().setPublicKeyId(
		"SANDBOX-XXXXXXXXXXXXXXXXXXXXXXXX").setRegion(
		com.amazon.pay.api.types.Region.EU);

		expectedURL = new java.net.URI("https://pay-api.amazon.eu/v2/in-store/refund");
		actualURL = com.amazon.pay.api.Util.getServiceURI(payConfiguration2, com.amazon.pay.api.ServiceConstants.INSTORE_REFUND);
		org.junit.Assert.assertEquals(expectedURL, actualURL);

		// With Algorithm set in payConfiguration
		final com.amazon.pay.api.PayConfiguration payConfigurationWithAlgorithm = new com.amazon.pay.api.PayConfiguration().setPublicKeyId(
		"SANDBOX-XXXXXXXXXXXXXXXXXXXXXXXX").setRegion(
		com.amazon.pay.api.types.Region.EU).setAlgorithm(
		"AMZN-PAY-RSASSA-PSS-V2");

		actualURL = com.amazon.pay.api.Util.getServiceURI(payConfigurationWithAlgorithm, com.amazon.pay.api.ServiceConstants.INSTORE_REFUND);
		org.junit.Assert.assertEquals(expectedURL, actualURL);
	}

	@org.junit.Test
	public void updateHeader() throws java.lang.Exception {
		com.amazon.pay.api.PayConfiguration payConfiguration1 = new com.amazon.pay.api.PayConfiguration().setRegion(
		com.amazon.pay.api.types.Region.EU).setEnvironment(
		com.amazon.pay.api.types.Environment.SANDBOX);

		java.util.Map<java.lang.String, java.lang.String> header = new java.util.HashMap<java.lang.String, java.lang.String>();
		java.util.Map<java.lang.String, java.lang.String> actualHeader = com.amazon.pay.api.Util.updateHeader(header);
		org.junit.Assert.assertTrue(!actualHeader.isEmpty());

		header = com.amazon.pay.api.Util.updateHeader(null);
		org.junit.Assert.assertNotNull(header);

	}

	@org.junit.Test
	public void testGetHttpUriRequestForValidHttpMethod() throws java.io.UnsupportedEncodingException, com.amazon.pay.api.exceptions.AmazonPayClientException, java.net.URISyntaxException {
		final java.util.List<java.lang.String> httpMethods = new java.util.ArrayList<java.lang.String>() {
			{
				add("GET");
				add("POST");
				add("PUT");
				add("PATCH");
				add("HEAD");
				add("DELETE");
				add("OPTIONS");
				add("TRACE");
			}
		};
		for (java.lang.String httpMethodName : httpMethods) {
			org.apache.http.client.methods.HttpUriRequest httpUriRequest = com.amazon.pay.api.Util.getHttpUriRequest(new java.net.URI(org.apache.commons.lang.StringUtils.EMPTY), httpMethodName, 
			org.apache.commons.lang.StringUtils.EMPTY);
			org.junit.Assert.assertEquals(httpMethodName, httpUriRequest.getMethod());
		}
	}

	@org.junit.Test(expected = com.amazon.pay.api.exceptions.AmazonPayClientException.class)
	public void testGetHttpUriRequestForInvalidHttpMethod() throws java.io.UnsupportedEncodingException, com.amazon.pay.api.exceptions.AmazonPayClientException, java.net.URISyntaxException {
		com.amazon.pay.api.Util.getHttpUriRequest(new java.net.URI(org.apache.commons.lang.StringUtils.EMPTY), "Invalid", org.apache.commons.lang.StringUtils.EMPTY);
	}

	@org.junit.Test
	public void testGetCloseableHttpClientWithProxyMethod() {
		final java.lang.String proxyhost = "host";
		final java.lang.Integer proxyPort = 8080;
		final java.lang.String proxyUser = "user";
		final char[] proxyPassword = new char[]{ 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };
		final com.amazon.pay.api.ProxySettings proxySettings = new com.amazon.pay.api.ProxySettings().setProxyHost(
		proxyhost).setProxyPort(
		proxyPort).setProxyUser(
		proxyUser).setProxyPassword(
		proxyPassword);
		final com.amazon.pay.api.PayConfiguration payConfiguration = new com.amazon.pay.api.PayConfiguration().setProxySettings(
		proxySettings).setClientConnections(
		com.amazon.pay.api.ServiceConstants.MAX_CLIENT_CONNECTIONS);
		final org.apache.http.impl.client.CloseableHttpClient httpClient = com.amazon.pay.api.Util.getCloseableHttpClientWithProxy(proxySettings, payConfiguration);
		// Assertions
		org.junit.Assert.assertEquals(proxyhost, payConfiguration.getProxySettings().getProxyHost());
		org.junit.Assert.assertEquals(proxyPort, payConfiguration.getProxySettings().getProxyPort());
		org.junit.Assert.assertEquals(proxyUser, payConfiguration.getProxySettings().getProxyUser());
		org.junit.Assert.assertEquals(proxyPassword, payConfiguration.getProxySettings().getProxyPassword());
		assertClientConnections(payConfiguration);
		org.junit.Assert.assertNotNull(httpClient);
	}

	@org.junit.Test
	public void testEnhanceResponseWithShippingAddressList() throws com.amazon.pay.api.exceptions.AmazonPayClientException, org.json.JSONException {
		org.json.JSONObject testShippingAddressListResponse = new org.json.JSONObject();
		testShippingAddressListResponse.put("shippingAddressList", new org.json.JSONArray().put("{\"addressId\":\"amzn1.address.ABC\",\"name\":\"DEF\",\"addressLine1\":\"GHI\",\"addressLine2\":\"JKL\",\"addressLine3\":null,\"city\":null,\"county\":null,\"district\":null,\"stateOrRegion\":\"MNO\",\"postalCode\":\"123-4567\",\"countryCode\":\"JP\",\"phoneNumber\":\"8910111213\"}"));

		final com.amazon.pay.api.AmazonPayResponse testCheckoutSessionResponse = new com.amazon.pay.api.AmazonPayResponse();
		testCheckoutSessionResponse.setResponse(testShippingAddressListResponse);

		final com.amazon.pay.api.AmazonPayResponse actualCheckoutSessionResponseAfterEnhancing = com.amazon.pay.api.Util.enhanceResponseWithShippingAddressList(testCheckoutSessionResponse);

		final org.json.JSONObject expectedShippingAddress = new fr.inria.astor.approaches.flakyseeding.utils.ShuffledJSON(new org.json.JSONObject());
		expectedShippingAddress.put("stateOrRegion", "MNO");
		expectedShippingAddress.put("phoneNumber", "8910111213");
		expectedShippingAddress.put("city", org.json.JSONObject.NULL);
		expectedShippingAddress.put("countryCode", "JP");
		expectedShippingAddress.put("district", org.json.JSONObject.NULL);
		expectedShippingAddress.put("postalCode", "123-4567");
		expectedShippingAddress.put("name", "DEF");
		expectedShippingAddress.put("county", org.json.JSONObject.NULL);
		expectedShippingAddress.put("addressLine1", "GHI");
		expectedShippingAddress.put("addressLine2", "JKL");
		expectedShippingAddress.put("addressLine3", org.json.JSONObject.NULL);
		expectedShippingAddress.put("addressId", "amzn1.address.ABC");

		org.json.JSONObject expectedShippingAddressListResponse = new org.json.JSONObject();
		expectedShippingAddressListResponse.put("shippingAddressList", new org.json.JSONArray().put(expectedShippingAddress));

		com.amazon.pay.api.AmazonPayResponse expectedCheckoutSessionResponse = new com.amazon.pay.api.AmazonPayResponse();
		expectedCheckoutSessionResponse.setResponse(expectedShippingAddressListResponse);
		expectedCheckoutSessionResponse.setRawResponse(expectedShippingAddressListResponse.toString());

		org.junit.Assert.assertEquals(expectedCheckoutSessionResponse.getRawResponse(), actualCheckoutSessionResponseAfterEnhancing.getRawResponse());
	}

	@org.junit.Test
	public void testGetHttpClientWithConnectionPool() {
		final com.amazon.pay.api.PayConfiguration payConfiguration = new com.amazon.pay.api.PayConfiguration().setClientConnections(
		com.amazon.pay.api.ServiceConstants.MAX_CLIENT_CONNECTIONS);
		final org.apache.http.impl.client.CloseableHttpClient httpClient = com.amazon.pay.api.Util.getHttpClientWithConnectionPool(payConfiguration);
		// Assertions
		assertClientConnections(payConfiguration);
		org.junit.Assert.assertNotNull(httpClient);
	}

	public void assertClientConnections(final com.amazon.pay.api.PayConfiguration payConfiguration) {
		org.junit.Assert.assertEquals(com.amazon.pay.api.ServiceConstants.MAX_CLIENT_CONNECTIONS, payConfiguration.getClientConnections());
	}}