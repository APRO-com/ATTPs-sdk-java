package org.apro.sdk.vrf.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderResponse extends BaseResponse {
  @JsonProperty("result")
  private List<Provider> result;
}