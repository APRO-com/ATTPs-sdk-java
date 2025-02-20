package org.apro.sdk.vrf.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Base Response Details
 */
@Data
public class BaseResponse {
  @JsonProperty("message")
  private String message;

  @JsonProperty("code")
  private Long code;

  @JsonProperty("responseEnum")
  private String responseEnum;
}