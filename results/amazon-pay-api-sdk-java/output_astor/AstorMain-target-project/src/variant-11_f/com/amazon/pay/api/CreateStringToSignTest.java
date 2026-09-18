package com.amazon.pay.api;
public class CreateStringToSignTest {
	private static final java.lang.String TEST_FILE = java.lang.System.getProperty("user.dir") + "/tst/com/amazon/pay/api/testdata.js";

	private static final java.util.List<org.json.JSONObject> TEST_CASES = new java.util.ArrayList<>();

	private static final com.amazon.pay.api.PayConfiguration payConfiguration = new com.amazon.pay.api.PayConfiguration().setRegion(com.amazon.pay.api.types.Region.EU).setPublicKeyId("");

	private static final com.amazon.pay.api.SignatureHelper signatureHelper = new com.amazon.pay.api.SignatureHelper(com.amazon.pay.api.CreateStringToSignTest.payConfiguration);

	@org.junit.BeforeClass
	public static void readTestCasesFromFile() throws java.lang.Throwable {
		java.lang.String fileTestContent = new java.lang.String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(com.amazon.pay.api.CreateStringToSignTest.TEST_FILE)), java.nio.charset.StandardCharsets.UTF_8);
		org.json.JSONArray allTestCases = new org.json.JSONArray(fileTestContent);
		for (int i = 0; i < allTestCases.length(); i++)
			com.amazon.pay.api.CreateStringToSignTest.TEST_CASES.add(((org.json.JSONObject) (allTestCases.getJSONObject(i))));

	}

	@org.junit.Test
	public void ValidateAllTestCases() throws java.net.URISyntaxException, java.security.NoSuchAlgorithmException, com.amazon.pay.api.exceptions.AmazonPayClientException, org.json.JSONException {
		for (org.json.JSONObject testCase : com.amazon.pay.api.CreateStringToSignTest.TEST_CASES) {
			java.lang.String name = testCase.optString("name");
			java.lang.String method = testCase.optString("method");
			java.net.URI uri = new java.net.URI(testCase.optString("uri"));
			java.util.Map<java.lang.String, java.util.List<java.lang.String>> queryParams = getParameters(((org.json.JSONObject) (testCase.get("parameters"))));
			java.lang.String payload = testCase.optString("payload");
			java.lang.String stringToSign = testCase.optString("stringToSign");
			java.util.Map<java.lang.String, java.util.List<java.lang.String>> preSignedHeaders = mockedPreSignedHeaders(uri);
			java.lang.String algorithm = (stringToSign.startsWith("AMZN-PAY-RSASSA-PSS-V2")) ? stringToSign.substring(0, 22) : stringToSign.substring(0, 19);
			java.lang.String actualCanonicalRequest = com.amazon.pay.api.CreateStringToSignTest.signatureHelper.createCanonicalRequest(uri, method, queryParams, payload, preSignedHeaders);
			java.lang.String expectedCanonicalRequest = testCase.optString("canonicalRequest");
			java.lang.String actualStringToSign = com.amazon.pay.api.CreateStringToSignTest.signatureHelper.createStringToSign(actualCanonicalRequest, algorithm);
			java.lang.String expectedStringToSign = testCase.optString("stringToSign");
			org.junit.Assert.assertEquals("Test Case Name : " + name, expectedCanonicalRequest, actualCanonicalRequest);
			org.junit.Assert.assertEquals("Test Case Name : " + name, expectedStringToSign, actualStringToSign);
		}
	}

	private java.util.Map<java.lang.String, java.util.List<java.lang.String>> mockedPreSignedHeaders(final java.net.URI uri) throws java.net.URISyntaxException {
		java.util.Map<java.lang.String, java.util.List<java.lang.String>> headers = new java.util.HashMap<>();
		java.util.List<java.lang.String> acceptHeaderValue = new java.util.ArrayList<java.lang.String>() {
			{
				add("application/json");
			}
		};
		headers.put("accept", acceptHeaderValue);
		java.util.List<java.lang.String> contentHeaderValue = new java.util.ArrayList<java.lang.String>() {
			{
				add("application/json");
			}
		};
		headers.put("content-type", contentHeaderValue);
		java.util.List<java.lang.String> regionHeaderValue = new java.util.ArrayList<java.lang.String>() {
			{
				add(com.amazon.pay.api.types.Region.EU.toString());
			}
		};
		headers.put("x-amz-pay-region", regionHeaderValue);
		java.util.List<java.lang.String> dateHeaderValue = new java.util.ArrayList<java.lang.String>() {
			{
				add("20180524T223710Z");
			}
		};
		headers.put("x-amz-pay-date", dateHeaderValue);
		java.util.List<java.lang.String> hostHeaderValue = new java.util.ArrayList<java.lang.String>() {
			{
				add("pay-api.amazon.eu");
			}
		};
		headers.put("x-amz-pay-host", hostHeaderValue);
		return headers;
	}

	private java.util.HashMap<java.lang.String, java.util.List<java.lang.String>> getParameters(final org.json.JSONObject jsonObject) throws org.json.JSONException {
		java.util.HashMap<java.lang.String, java.util.List<java.lang.String>> parameters = new java.util.HashMap<>();
		java.util.Iterator<java.lang.String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			java.lang.String key = keys.next();
			org.json.JSONArray vals = jsonObject.getJSONArray(((java.lang.String) (key)));
			java.util.ArrayList<java.lang.String> l = new java.util.ArrayList<>();
			for (int i = 0; i < vals.length(); i++) {
				l.add(vals.getString(i));
			}
			parameters.put(((java.lang.String) (key)), l);
		} 
		return parameters;
	}
}