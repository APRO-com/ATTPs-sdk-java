package org.apro.sdk.vrf.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VRFProofResponse extends BaseResponse {
  @JsonProperty("result")
  private VRFProof result;
}