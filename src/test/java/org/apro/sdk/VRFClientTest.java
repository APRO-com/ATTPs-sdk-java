package org.apro.sdk;

import org.apro.sdk.util.HttpClient;
import org.apro.sdk.util.Utils;
import org.apro.sdk.vrf.models.Proof;
import org.apro.sdk.vrf.VRF;
import org.apro.sdk.vrf.VRFClient;
import org.apro.sdk.vrf.models.VRFProof;
import org.apro.sdk.vrf.models.VRFRequest;
import org.apro.sdk.vrf.models.VRFResponse;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

import static org.apro.sdk.vrf.VRF.CURVE_SPEC;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class VRFClientTest {

  @Test
  void testVRFRequest() throws Exception {
    VRFClient client = new VRFClient(HttpClient.newDefaultHttpClient());

    var providers = client.getProviders();
    assertFalse(providers.isEmpty(), "No providers available");

    // Prepare request parameters
    long version = 1L;
    String targetAgentId = Utils.generateUUID();
    String customerSeed = Utils.secureRandomString(4);
    long requestTimestamp = System.currentTimeMillis() / 1000;
    String keyHash = providers.get(0).getKeyHash();
    String callbackUri = "http://127.0.0.1:8888/api/vrf/proof";

    // Calculate request ID
    String requestId = client.calculateRequestId(
        version,
        targetAgentId,
        customerSeed,
        requestTimestamp,
        callbackUri
    );
    System.out.println(requestId);

    // Create and send request
    VRFRequest request = new VRFRequest();
    request.setVersion(version);
    request.setTargetAgentId(targetAgentId);
    request.setClientSeed(customerSeed);
    request.setKeyHash(keyHash);
    request.setRequestTimestamp(requestTimestamp);
    request.setRequestId(requestId);
    request.setCallbackUri(callbackUri);

    VRFResponse random = client.request(request);
    assertNotNull(random, "Response should not be null");


    // Query proof
    VRFProof proof = client.queryProof(requestId);
    assertNotNull(proof, "Proof should not be null");
    System.out.printf("Proof: %s", proof.marshal());
  }

  @Test
  void testVerifyVRFProof() throws Exception {
    VRF vrf = new VRF();

    // Create test proof
    Proof proof = new Proof();
    ECPoint publicK = CURVE_SPEC.getCurve().createPoint(
        new BigInteger("ed3bace23c5e17652e174c835fb72bf53ee306b3406a26890221b4cef7500f88", 16),
        new BigInteger("e57a6f571288ccffdcda5e8a7a1f87bf97bd17be084895d0fce17ad5e335286e", 16)
    );
    ECPoint gamma = CURVE_SPEC.getCurve().createPoint(
        new BigInteger("7ce22e7667f955f5dcc805a5bae7f78d21d0cb04eb5190f3b8e20b68a45d0b87", 16),
        new BigInteger("c8f9d9e8d5e4eb22adf379df733a8b1ce4edf26a2ca9a4a3d8a07cb3e3dffd9", 16)
    );
//    proof.setPublicX("0xed3bace23c5e17652e174c835fb72bf53ee306b3406a26890221b4cef7500f88");
//    proof.setPublicY("0xe57a6f571288ccffdcda5e8a7a1f87bf97bd17be084895d0fce17ad5e335286e");
//    proof.setGammaX("0x7ce22e7667f955f5dcc805a5bae7f78d21d0cb04eb5190f3b8e20b68a45d0b87");
//    proof.setGammaY("0xc8f9d9e8d5e4eb22adf379df733a8b1ce4edf26a2ca9a4a3d8a07cb3e3dffd9");
    proof.setPublicKey(publicK);
    proof.setGamma(gamma);
    proof.setC(new BigInteger("45945e1b7362a7026df893d39496eb838b6d85264f56899182269be4d53d6fe", 16));
    proof.setS(new BigInteger("7ebf871ad068ce4bbe04cd726e359334581881b4e78da352b7ac413ebcf90a2", 16));
    proof.setSeed(new BigInteger("d3ea21873da2909f9f732966278cc022d523006ea574a58b324b83c0c08a5346", 16));
    proof.setOutput(new BigInteger("11449014f7e3fb46f190149f5c147242300ccdb0e77a58fa53e01972939a3f14", 16));

    boolean status = vrf.verifyVRFProof(proof);
    assertTrue(status, "Proof verification should succeed");
  }

  @Test
  void testCalculateRequestId() throws Exception {
    VRFClient client = new VRFClient(HttpClient.newDefaultHttpClient());

    // Test parameters
    String benchmarkRequestId = "6f71619f1e6ea42616c9bbdc8fe001511e0c37b72373dc259857b29c1e61597c";
    long version = 1L;
    String targetAgentId = "f2464336-fbcf-4603-bda5-ce65c0318fb6";
    String customerSeed = "0x1234";
    String callbackUri = "http://127.0.0.1:8888/api/vrf/proof";
    long requestTimestamp = 1739265192L;

    String requestId = client.calculateRequestId(
        version,
        targetAgentId,
        customerSeed,
        requestTimestamp,
        callbackUri
    );

    assertEquals(benchmarkRequestId, requestId,
        String.format("Request ID mismatch. Expected: %s, Got: %s",
            benchmarkRequestId, requestId));
  }
}