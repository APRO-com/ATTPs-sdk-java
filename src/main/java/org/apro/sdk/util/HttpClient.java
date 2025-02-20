package org.apro.sdk.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import okhttp3.*;
import okio.Buffer;
import org.apro.sdk.auth.Credential;
import org.apro.sdk.vrf.constant.Constants;
import org.apro.sdk.models.APIError;
import org.apro.sdk.models.APIResult;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class HttpClient {
  private static final Pattern JSON_TYPE_PATTERN =
      Pattern.compile("(?i:(?:application|text)/(?:vnd\\.[^;]+\\+)?json)");
  private static final Pattern XML_TYPE_PATTERN =
      Pattern.compile("(?i:(?:application|text)/xml)");

  private final OkHttpClient httpClient;
  private final Credential credential;
  private final ObjectMapper objectMapper;

  public static HttpClient newDefaultHttpClient() {
    return new HttpClient(null, null);
  }

  public HttpClient(Credential credential, OkHttpClient httpClient) {
    this.credential = credential;
    this.httpClient = httpClient != null ? httpClient : createDefaultHttpClient();
    this.objectMapper = new ObjectMapper();
  }

  private OkHttpClient createDefaultHttpClient() {
    return new OkHttpClient.Builder()
        .connectTimeout(Constants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(Constants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(Constants.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
        .build();
  }

  /**
   * Send request with full control over HTTP method, path, headers, query parameters and body
   */
  public APIResult request(
      String method,
      String requestPath,
      Map<String, String> headers,
      Map<String, String> queryParams,
      Object postBody,
      String contentType
  ) throws Exception {
    // Build URL with query parameters
    HttpUrl.Builder urlBuilder = HttpUrl.parse(requestPath).newBuilder();
    if (queryParams != null) {
      for (Map.Entry<String, String> entry : queryParams.entrySet()) {
        urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
      }
    }

    // Build request body
    RequestBody body = null;
    String signBody = "";
    if (postBody != null) {
      if (contentType == null || contentType.isEmpty()) {
        contentType = Constants.APPLICATION_JSON;
      }
      body = buildRequestBody(postBody, contentType);
      signBody = bodyToString(body);
    }

    // Build request
    Request.Builder requestBuilder = new Request.Builder()
        .url(urlBuilder.build())
        .method(method, body);

    // Add headers
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        requestBuilder.addHeader(entry.getKey(), entry.getValue());
      }
    }

    // Add fixed headers
    requestBuilder.addHeader(Constants.ACCEPT, "*/*");
    requestBuilder.addHeader(Constants.CONTENT_TYPE, contentType);
    String userAgent = String.format(Constants.USER_AGENT_FORMAT,
        Constants.VERSION, System.getProperty("os.name"), System.getProperty("java.version"));
    requestBuilder.addHeader(Constants.USER_AGENT, userAgent);

    // Add auth header
    if (credential != null) {
      String authHeader = credential.generateAuthorization(signBody);
      if (authHeader != null && !authHeader.isEmpty()) {
        requestBuilder.addHeader(Constants.AUTHORIZATION, authHeader);
      }
    }

    // Execute request
    Request request = requestBuilder.build();

    Response response = httpClient.newCall(request).execute();

    // Check response
    checkResponse(response);

    return new APIResult(request, response);
  }

  private RequestBody buildRequestBody(Object body, String contentType) throws IOException, JAXBException {
    if (body instanceof String) {
      return RequestBody.create(MediaType.parse(contentType), (String) body);
    } else if (body instanceof byte[]) {
      return RequestBody.create(MediaType.parse(contentType), (byte[]) body);
    } else if (body instanceof File) {
      return RequestBody.create(MediaType.parse(contentType), (File) body);
    } else if (JSON_TYPE_PATTERN.matcher(contentType).matches()) {
      String json = objectMapper.writeValueAsString(body);
      return RequestBody.create(MediaType.parse(contentType), json);
    } else if (XML_TYPE_PATTERN.matcher(contentType).matches()) {
      StringWriter writer = new StringWriter();
      JAXBContext context = JAXBContext.newInstance(body.getClass());
      Marshaller marshaller = context.createMarshaller();
      marshaller.marshal(body, writer);
      return RequestBody.create(MediaType.parse(contentType), writer.toString());
    }
    throw new IllegalArgumentException("Unsupported body type: " + body.getClass());
  }

  private String bodyToString(RequestBody body) throws IOException {
    if (body == null) return "";
    Buffer buffer = new Buffer();
    body.writeTo(buffer);
    return buffer.readUtf8();
  }

  private void checkResponse(Response response) throws IOException {
    if (!response.isSuccessful()) {
      String responseBody = response.body().string();
      APIError apiError = new APIError(
          response.code(),
          response.headers().toMultimap(),
          responseBody
      );
      try {
        objectMapper.readerForUpdating(apiError).readValue(responseBody);
      } catch (IOException ignored) {
        // Ignore JSON parsing errors
      }
      throw apiError;
    }
  }
}