package org.apro.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Getter
public class APIError extends IOException {
  @JsonIgnore
  private final int statusCode;

  @JsonIgnore
  private final Map<String, List<String>> header;

  @JsonIgnore
  private final String body;

  private String code;
  private String message;

  public APIError(int statusCode, Map<String, List<String>> header, String body) {
    super(String.format("Status Code: %d, Body: %s", statusCode, body));
    this.statusCode = statusCode;
    this.header = header;
    this.body = body;
  }
}