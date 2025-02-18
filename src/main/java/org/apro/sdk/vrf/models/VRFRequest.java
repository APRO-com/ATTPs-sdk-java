package org.apro.sdk.vrf.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VRFRequest {
  @JsonProperty("version")
  private Long version;

  @JsonProperty("target_agent_id")
  private String targetAgentId;

  @JsonProperty("client_seed")
  private String clientSeed;

  @JsonProperty("key_hash")
  private String keyHash;

  @JsonProperty("request_timestamp")
  private Long requestTimestamp;

  @JsonProperty("request_id")
  private String requestId;

  @JsonProperty("callback_uri")
  private String callbackUri;
}