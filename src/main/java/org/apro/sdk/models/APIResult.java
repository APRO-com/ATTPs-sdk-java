package org.apro.sdk.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * API request result containing both request and response
 */
@Getter
@AllArgsConstructor
public class APIResult {
  /**
   * The HTTP request used for this API call
   */
  private final Request request;

  /**
   * The HTTP response received from this API call
   */
  private final Response response;

  /**
   * Get response body as string. This will consume the response body.
   *
   * @return Response body as string
   * @throws IOException if reading the response body fails
   */
  public String getBodyAsString() throws IOException {
    if (response.body() == null) {
      return "";
    }
    return response.body().string();
  }

  /**
   * Parse response body into the specified type
   *
   * @param responseType Class of the response type
   * @return Parsed response object
   * @throws IOException if parsing fails
   */
  public <T> T parseBody(Class<T> responseType) throws IOException {
    if (response.body() == null) {
      return null;
    }

    String bodyString = response.body().string();
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(bodyString, responseType);
  }
}