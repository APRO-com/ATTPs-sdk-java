package org.apro.sdk.vrf.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Provider {
  @JsonProperty("address")
  private String address;

  @JsonProperty("keyHash")
  private String keyHash;
}