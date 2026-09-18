package com.amazon.pay.api;
@org.junit.runner.RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@org.powermock.core.classloader.annotations.PrepareForTest({ com.amazon.pay.api.RequestSigner.class, com.amazon.pay.api.Util.class })
public class RequestSignerWithHeaderTest {
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

	private java.util.Map<java.lang.String, java.lang.String> postSignedWithHeadersMap;

	private java.util.Map<java.lang.String, java.lang.String> postSignedWithHeadersMapWithAlgorithm;

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
		payConfiguration.setRegion(com.amazon.pay.api.types.Region.NA).setPublicKeyId("ADGUHQIH9988").setPrivateKey(com.amazon.pay.api.ServiceConstants.privateKeyArray);
		payConfigurationWithAlgorithm.setRegion(com.amazon.pay.api.types.Region.NA).setPublicKeyId("ADGUHQIH9988").setPrivateKey(com.amazon.pay.api.ServiceConstants.privateKeyArray).setAlgorithm("AMZN-PAY-RSASSA-PSS-V2");
		setUpMockValues();
	}

	private void setUpMockValues() throws java.lang.Exception {
		authToken = "eyJhbGciOiJIbWFjU0hBMjU2IiwidHlwIjoiSldUIn0=";
		header = new java.util.HashMap<java.lang.String, java.lang.String>() {
			{
				put("x-amz-pay-idempotency-key", "23GGJHGB668344");
			}
		};
		uri = new java.net.URI("https://pay-api.amazon.com/sandbox/v2/refunds");
		payload = "payload";
		canonicalRequest = (((((((((("POST\n" + "/sandbox/v2/refunds\n") + "\n") + "accept:application/json\n") + "content-type:application/json\n") + "x-amz-pay-date:20190826T184058Z\n") + "x-amz-pay-host:pay-api.amazon.com\n") + "x-amz-pay-idempotency-key:23GGJHGB668344\n") + "x-amz-pay-region:EU\n") + "\n") + "accept;content-type;x-amz-pay-date;x-amz-pay-host;x-amz-pay-idempotency-key;x-amz-pay-region\n") + "81dd99309152d21f2cef921656d3f57830fe9c36fe193af1b62de504e806aceb";
		stringToSign = "AMZN-PAY-RSASSA-PSS\n" + "15322736b7e5a9056411168d070b1f3dcc289c46890692c06f07c62d3ef0721d";
		signature = "BsnrBn7R4QvpWqPzElKnxK8KLm7BzglICqRsWDcj7okwVpHrpZnoOm4D3v2+naryg2vIzP2iIWvscNm3MbX7vR3nClgcB+vVUQZLEu9yg0IJA4QCiybh9etgLHSRv2jwR9ByFe9U5FMdhr7omDG3Q1lAjvvxiPHt9UtL3h1LJ7rirOuQUWp/zL5QDWsIvTty3zEKksdRJuPeCGwijwo0LPuIf2plZGv9TJ5CJBxssw3+phj5Nvo9HWuzFRkJsC1jgknO0+eSTSn5RM6R2Px0mkz3qbd5ZpSX3tIoK937vkmNZALNm/euqYnIKjviGVuSEDo1ite84foCvSqpTmiVrg==";
		signedHeaderString = "accept;content-type;x-amz-pay-date;x-amz-pay-host;x-amz-pay-idempotency-key;x-amz-pay-region";
		authorizationHeader = "AMZN-PAY-RSASSA-PSS PublicKeyId=ADGUHQIH9988, SignedHeaders=accept;content-type;x-amz-pay-date;x-amz-pay-host;x-amz-pay-idempotency-key;x-amz-pay-region, Signature=BsnrBn7R4QvpWqPzElKnxK8KLm7BzglICqRsWDcj7okwVpHrpZnoOm4D3v2+naryg2vIzP2iIWvscNm3MbX7vR3nClgcB+vVUQZLEu9yg0IJA4QCiybh9etgLHSRv2jwR9ByFe9U5FMdhr7omDG3Q1lAjvvxiPHt9UtL3h1LJ7rirOuQUWp/zL5QDWsIvTty3zEKksdRJuPeCGwijwo0LPuIf2plZGv9TJ5CJBxssw3+phj5Nvo9HWuzFRkJsC1jgknO0+eSTSn5RM6R2Px0mkz3qbd5ZpSX3tIoK937vkmNZALNm/euqYnIKjviGVuSEDo1ite84foCvSqpTmiVrg==";
		authorizationHeaderWithAlgorithm = "AMZN-PAY-RSASSA-PSS-V2 PublicKeyId=ADGUHQIH9988, SignedHeaders=accept;content-type;x-amz-pay-date;x-amz-pay-host;x-amz-pay-idempotency-key;x-amz-pay-region, Signature=BsnrBn7R4QvpWqPzElKnxK8KLm7BzglICqRsWDcj7okwVpHrpZnoOm4D3v2+naryg2vIzP2iIWvscNm3MbX7vR3nClgcB+vVUQZLEu9yg0IJA4QCiybh9etgLHSRv2jwR9ByFe9U5FMdhr7omDG3Q1lAjvvxiPHt9UtL3h1LJ7rirOuQUWp/zL5QDWsIvTty3zEKksdRJuPeCGwijwo0LPuIf2plZGv9TJ5CJBxssw3+phj5Nvo9HWuzFRkJsC1jgknO0+eSTSn5RM6R2Px0mkz3qbd5ZpSX3tIoK937vkmNZALNm/euqYnIKjviGVuSEDo1ite84foCvSqpTmiVrg==";
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
		org.powermock.api.mockito.PowerMockito.whenNew(com.amazon.pay.api.SignatureHelper.class).withAnyArguments().thenReturn(com.amazon.pay.api.RequestSignerWithHeaderTest.signatureHelper);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerWithHeaderTest.signatureHelper.createCanonicalRequest(org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyString(), org.mockito.Mockito.anyMap(), org.mockito.Mockito.anyString(), org.mockito.Mockito.anyMap())).thenReturn(canonicalRequest);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerWithHeaderTest.signatureHelper.createStringToSign(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString())).thenReturn(stringToSign);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerWithHeaderTest.signatureHelper.generateSignature(org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyObject())).thenReturn(signature);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerWithHeaderTest.signatureHelper.getSignedHeadersString(org.mockito.Mockito.anyMap())).thenReturn(signedHeaderString);
		org.mockito.Mockito.when(com.amazon.pay.api.RequestSignerWithHeaderTest.signatureHelper.createPreSignedHeaders(org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyMap())).thenReturn(headers);
		postSignedWithHeadersMap = new java.util.HashMap<java.lang.String, java.lang.String>() {
			{
				put("accept", "application/json");
				put("content-type", "application/json");
				put("x-amz-pay-host", "pay-api.amazon.com");
				put("x-amz-pay-date", dateHeaderValue.get(0));
				put("x-amz-pay-region", "NA");
				put("authorization", authorizationHeader);
				put("x-amz-pay-idempotency-key", "23GGJHGB668344");
				put("user-agent", ("amazon-pay-api-sdk-java/" + com.amazon.pay.api.ServiceConstants.APPLICATION_LIBRARY_VERSION) + " (Java/1.8.0_172; Linux/4.9.93-0.1.ac.178.67.327.metal1.x86_64)");
			}
		};
		postSignedWithHeadersMapWithAlgorithm = new java.util.HashMap<java.lang.String, java.lang.String>() {
			{
				putAll(postSignedWithHeadersMap);
				put("authorization", authorizationHeaderWithAlgorithm);
			}
		};
	}

	@org.junit.Test
	public void signRequestWithHeader() throws com.amazon.pay.api.exceptions.AmazonPayClientException {
		signRequestWithHeader(payConfiguration, postSignedWithHeadersMap);
		signRequestWithHeader(payConfigurationWithAlgorithm, postSignedWithHeadersMapWithAlgorithm);
	}

	private void signRequestWithHeader(final com.amazon.pay.api.PayConfiguration payConfiguration, final java.util.Map<java.lang.String, java.lang.String> postSignedWithHeadersMap) throws com.amazon.pay.api.exceptions.AmazonPayClientException {
		com.amazon.pay.api.RequestSigner requestSigner = new com.amazon.pay.api.RequestSigner(payConfiguration);
		java.util.Map<java.lang.String, java.lang.String> actualHeaders = requestSigner.signRequest(uri, "POST", parameters, payload, header);
		org.junit.Assert.assertEquals(postSignedWithHeadersMap, actualHeaders);
		payConfiguration.setUserAgentRedaction(true);
		postSignedWithHeadersMap.put("user-agent", ("amazon-pay-api-sdk-java/" + com.amazon.pay.api.ServiceConstants.APPLICATION_LIBRARY_VERSION) + " (Java/Redacted; Redacted/Redacted)");
		actualHeaders = requestSigner.signRequest(uri, "POST", parameters, payload, header);
		org.junit.Assert.assertEquals(postSignedWithHeadersMap, actualHeaders);
	}
}