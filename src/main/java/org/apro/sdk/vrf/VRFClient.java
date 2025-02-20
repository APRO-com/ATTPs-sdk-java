package org.apro.sdk.vrf;

import lombok.extern.slf4j.Slf4j;
import org.apro.sdk.util.HttpClient;
import org.apro.sdk.util.Utils;
import org.apro.sdk.vrf.constant.Constants;
import org.apro.sdk.models.APIResult;
import org.apro.sdk.vrf.models.*;
import org.web3j.crypto.Hash;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class VRFClient extends Service {

  VRF vrf = new VRF();

  public VRFClient(HttpClient client) {
    super(client);
  }

  /**
   * Get a list of VRF random providers
   *
   * @return List of providers
   * @throws Exception if the request fails
   */
  public List<Provider> getProviders() throws Exception {
    // Setup HTTP method and path
    String httpMethod = "GET";
    String path = Constants.API_BASE_SERVER + "/api/vrf/provider";

    // Initialize headers
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    // Perform HTTP request
    APIResult result = getClient().request(
        httpMethod,
        path,
        headers,
        null,  // no query params
        null,  // no body
        "application/json"
    );

    // Parse response
    ProviderResponse response = result.parseBody(ProviderResponse.class);

    // Check response status
    if (response.getCode() != 0) {
      throw new IllegalStateException(
          response.getMessage() != null ? response.getMessage() : "Unknown error"
      );
    }

    // Return provider list
    return response.getResult();
  }

  /**
   * Query VRF random proof by request ID
   *
   * @param requestId The request ID to query
   * @return VRF proof data
   * @throws Exception if the query fails or proof verification fails
   */
  public VRFProof queryProof(String requestId) throws Exception {
    // Setup HTTP method and path
    String httpMethod = "GET";
    String path = Constants.API_BASE_SERVER + "/api/vrf/query";

    // Initialize headers
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    // Setup query parameters
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("request_id", requestId);

    // Perform HTTP request
    APIResult result = getClient().request(
        httpMethod,
        path,
        headers,
        queryParams,
        null,  // no body
        "application/json"
    );

    // Parse response
    VRFProofResponse response = result.parseBody(VRFProofResponse.class);

    // Check response status
    if (response.getCode() != 0) {
      throw new IllegalStateException(
          response.getMessage() != null ? response.getMessage() : "Unknown error"
      );
    }

    // Verify proof
    boolean status = this.vrf.verifyVRFProof(response.getResult().convert());
    if (!status) {
      throw new IllegalStateException("Invalid proof");
    }

    return response.getResult();
  }


  /**
   * Request a VRFClient random number and verify the returned proof data
   */
  public VRFResponse request(VRFRequest req) throws Exception {
    String httpMethod = "POST";
    String path = Constants.API_BASE_SERVER + "/api/vrf/request";
    Map<String, String> headers = new HashMap<>();

    // Check request parameters
    checkRequestParams(req);

    // Set content type
    headers.put("Content-Type", "application/json");

    // Perform HTTP request
    APIResult result = getClient().request(
        httpMethod,
        path,
        headers,
        null,  // no query params
        req,
        "application/json"
    );

    return result.parseBody(VRFResponse.class);
  }

  /**
   * Calculate request ID based on input parameters
   */
  public String calculateRequestId(long version, String targetAgentId, String customerFeed,
                                   long requestTimestamp, String callbackUri) {
    // Concatenate byte arrays
    byte[] combined = Utils.longToBytes(version);
    combined = Utils.concat(combined, targetAgentId.getBytes(StandardCharsets.UTF_8));
    combined = Utils.concat(combined, Utils.hexStringToBytes(customerFeed));
    combined = Utils.concat(combined, Utils.longToBytes(requestTimestamp));
    combined = Utils.concat(combined, callbackUri.getBytes(StandardCharsets.UTF_8));

    // Compute Keccak256 hash
    byte[] hash = Hash.sha3(combined);
    return Utils.bytesToHex(hash);
  }

  private void checkRequestParams(VRFRequest req) throws IllegalArgumentException {
    if (req.getVersion() != Constants.VRF_VERSION) {
      throw new IllegalArgumentException(
          String.format("VRFClient version mismatch, must be %d", Constants.VRF_VERSION));
    }

    if (!Utils.checkUUID(req.getTargetAgentId())) {
      throw new IllegalArgumentException(
          String.format("Invalid target agent id: %s", req.getTargetAgentId()));
    }

    String requestId = calculateRequestId(
        req.getVersion(),
        req.getTargetAgentId(),
        req.getClientSeed(),
        req.getRequestTimestamp(),
        req.getCallbackUri()
    );

    if (!req.getRequestId().equals(requestId)) {
      throw new IllegalArgumentException(
          String.format("Invalid request ID: %s", req.getRequestId()));
    }
  }
}
