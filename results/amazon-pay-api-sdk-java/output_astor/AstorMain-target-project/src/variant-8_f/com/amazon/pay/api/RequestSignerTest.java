package com.amazon.pay.api;
@org.junit.runner.RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@org.powermock.core.classloader.annotations.PrepareForTest({ com.amazon.pay.api.RequestSigner.class, com.amazon.pay.api.Util.class })
public class RequestSignerTest {
	private static final com.amazon.pay.api.SignatureHelper signatureHelper = org.mockito.Mockito.mock(com.amazon.pay.api.SignatureHelper.class);

	private com.amazon.pay.api.PayConfiguration payConfiguration = new com.amazon.pay.api.PayConfiguration();

	private com.amazon.pay.api.PayConfiguration payConfigurationWithAlgorithm = new com.amazon.pay.api.PayConfiguration();

	private java.net.URI uri;

	private java.lang.String payload;

	private java.util.Map<java.lang.String, java.util.List<java.lang.String>> parameters = new java.util.HashMap<>();

	private java.lang.String canonicalRequest;

	private java.lang.String stringToSign;

	private java.lang.String signature;

	private java.lang.String signedHeaderString;

	private java.lang.String authorizationHeader;

	private java.lang.String authorizationHeaderWithAlgorithm;

	private java.util.Map<java.lang.String, java.util.List<java.lang.String>> headers;

	private java.util.Map<java.lang.String, java.lang.String> postSignedHeadersMap;

	private java.util.Map<java.lang.String, java.lang.String> postSignedHeadersMapWithAlgorithm;

	private java.lang.String authToken;

	private java.util.Map<java.lang.String, java.lang.String> header;

	@org.junit.Before
	public void setUp() throws java.lang.Exception {
		java.lang.System.setProperty("os.version", "4.9.93-0.1.ac.178.67.327.metal1.x86_64");
		java.lang.System.setProperty("java.version", "1.8.0_172");
		java.lang.System.setProperty("os.name", "Linux");
		java.security.PrivateKey mockKey = org.mockito.Mockito.mock(java.security.PrivateKey.class);
		org.powermock.api.mockito.PowerMockito.mockStatic(com.amazon.pay.api.Util.class);
		org.mockito.Mockito.when(com.amazon.pay.api.Util.buildPrivateKey(org.mockito.Mockito.any(char[].class))).thenReturn(mockKey);
		payConfiguration.setRegion(com.amazon.pay.api.types.Region.EU).setPublicKeyId("ADGUHQIH9988").setPrivateKey(com.amazon.pay.api.ServiceConstants.privateKeyArray);
		payConfigurationWithAlgorithm.setRegion(com.amazon.pay.api.types.Region.EU).setPublicKeyId("ADGUHQIH9988").setPrivateKey(com.amazon.pay.api.ServiceConstants.privateKeyArray).setAlgorithm("AMZN-PAY-RSASSA-PSS-V2");
		setUpMockValues();
	}

	private void setUpMockValues() throws java.lang.Exception {
		authToken = "eyJhbGciOiJIbWFjU0hBMjU2IiwidHlwIjoiSldUIn0=";
		header = new java.util.HashMap<java.lang.String, java.lang.String>();
		uri = new java.net.URI("https://pay-api.amazon.eu/sandbox/v2/in-store/refund");
		payload = "payload";
		canonicalRequest = ((((((((("POST\n" + "/sandbox/v2/in-store/refund\n") + "\n") + "accept:application/json\n") + "content-type:application/json\n") + "x-amz-pay-date:20180524T223710Z\n") + "x-amz-pay-host:pay-api.amazon.eu\n") + "x-amz-pay-region:EU\n") + "\n") + "accept;content-type;x-amz-pay-date;x-amz-pay-host;x-amz-pay-region\n") + "81dd99309152d21f2cef921656d3f57830fe9c36fe193af1b62de504e806aceb";
		stringToSign = "AMZN-PAY-RSASSA-PSS\n" + "227f8d4a6974e65a62ebe6648fab8666fe25f10dc2ec41fba9c439e633ba4b94";
		signature = "c062NjivoUW+TcHegKebFamCX8Cpmpmy6EiPmKwdpEuZZIpOHJYO";
		signedHeaderString = "accept;content-type;x-amz-pay-date;x-amz-pay-host;x-amz-pay-region";
		authorizationHeader = "AMZN-PAY-RSASSA-PSS PublicKeyId=ADGUHQIH9988, SignedHeaders=accept;content-type;x-amz-pay-date;x-amz-pay-host;x-amz-pay-region, Signature=c062NjivoUW+TcHegKebFamCX8Cpmpmy6EiPmKwdpEuZZIpOHJYO";
		authorizationHeaderWithAlgorithm = "AMZN-PAY-RSASSA-PSS-V2 PublicKeyId=ADGUHQIH9988, SignedHeaders=accept;content-type;x-amz-pay-date;x-amz-pay-host;x-amz-pay-region, Signature=c062NjivoUW+TcHegKebFamCX8Cpmpmy6EiPmKwdpEuZZIpOHJYO";
		headers = new java.util.HashMap<>();
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
				add(payConfiguration.getRegion().toString());
			}
		};
		headers.put("x-amz-pay-region", regionHeaderValue);
		java.util.List<java.lang.String> dateHeaderValue = new java.util.ArrayList<java.lang.String>() {
			{
				add(com.amazon.pay.api.Util.getFormattedTimestamp());
			}
		};
		headers.put("x-amz-pay-date", dateHeaderValue);
		java.util.List<java.lang.String> hostHeaderValue = new java.util.ArrayList<java.lang.String>() {
			{
				add(uri.getHost());
			}
		};
		headers.put("x-amz-pay-host", hostHeaderValue);
		for (java.util.Map.Entry<java.lang.String, java.lang.String> entry : header.entrySet()) {
			final java.util.List<java.lang.String> authHeaderValue = new java.util.ArrayList<>();
			authHeaderValue.add(entry.getValue());
			headers.put(entry.getKey(), authHeaderValue);
		}
		org.powermock.api.mockito.PowerMockito.whenNew(com.amazon.pay.api.SignatureHelper.class).withAnyArguments().thenReturn(com.amazon.pay.api.RequestSignerTest.signatureHelper);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerTest.signatureHelper.createCanonicalRequest(org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyString(), org.mockito.Mockito.anyMap(), org.mockito.Mockito.anyString(), org.mockito.Mockito.anyMap())).thenReturn(canonicalRequest);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerTest.signatureHelper.createStringToSign(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString())).thenReturn(stringToSign);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerTest.signatureHelper.generateSignature(org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyObject())).thenReturn(signature);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerTest.signatureHelper.getSignedHeadersString(org.mockito.Mockito.anyMap())).thenReturn(signedHeaderString);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerTest.signatureHelper.createPreSignedHeaders(org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyMap())).thenReturn(headers);
		postSignedHeadersMap = new java.util.HashMap<java.lang.String, java.lang.String>() {
			{
				put("accept", "application/json");
				put("content-type", "application/json");
				put("x-amz-pay-host", "pay-api.amazon.eu");
				put("x-amz-pay-date", dateHeaderValue.get(0));
				put("x-amz-pay-region", "EU");
				put("authorization", authorizationHeader);
				put("user-agent", ("amazon-pay-api-sdk-java/" + com.amazon.pay.api.ServiceConstants.APPLICATION_LIBRARY_VERSION) + " (Java/1.8.0_172; Linux/4.9.93-0.1.ac.178.67.327.metal1.x86_64)");
			}
		};
		postSignedHeadersMapWithAlgorithm = new java.util.HashMap<java.lang.String, java.lang.String>() {
			{
				putAll(postSignedHeadersMap);
				put("authorization", authorizationHeaderWithAlgorithm);
			}
		};
	}

	@org.junit.Test
	public void signRequestWithEmptyHeader() throws java.lang.Exception {
		signRequestWithEmptyHeader(payConfiguration, postSignedHeadersMap);
		signRequestWithEmptyHeader(payConfigurationWithAlgorithm, postSignedHeadersMapWithAlgorithm);
	}

	private void signRequestWithEmptyHeader(final com.amazon.pay.api.PayConfiguration payConfiguration, final java.util.Map<java.lang.String, java.lang.String> postSignedHeadersMap) throws java.lang.Exception {
		com.amazon.pay.api.RequestSigner requestSigner = new com.amazon.pay.api.RequestSigner(payConfiguration);
		java.util.Map<java.lang.String, java.lang.String> header = new java.util.HashMap<java.lang.String, java.lang.String>();
		java.util.Map<java.lang.String, java.lang.String> actualHeaders = requestSigner.signRequest(uri, "POST", parameters, payload, header);
		org.junit.Assert.assertEquals(postSignedHeadersMap, actualHeaders);
		payConfiguration.setUserAgentRedaction(true);
		postSignedHeadersMap.put("user-agent", ("amazon-pay-api-sdk-java/" + com.amazon.pay.api.ServiceConstants.APPLICATION_LIBRARY_VERSION) + " (Java/Redacted; Redacted/Redacted)");
		actualHeaders = requestSigner.signRequest(uri, "POST", parameters, payload, header);
		org.junit.Assert.assertEquals(postSignedHeadersMap, actualHeaders);
	}
}