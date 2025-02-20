package org.apro.sdk.vrf.constant;

/**
 * Constants used in the SDK
 */
public class Constants {
  // SDK related information
  public static final String VERSION = "0.0.1";
  public static final String USER_AGENT_FORMAT = "ATTPs-Java/%s (%s) JAVA/%s";
  public static final String API_BASE_SERVER = "http://10.0.54.95:8888";

  // HTTP request message Header related constants
  public static final String ACCEPT = "Accept";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String CONTENT_LENGTH = "Content-Length";
  public static final String USER_AGENT = "User-Agent";
  public static final String AUTHORIZATION = "Authorization";

  // Common ContentType
  public static final String APPLICATION_JSON = "application/json";
  public static final String IMAGE_JPG = "image/jpg";
  public static final String IMAGE_PNG = "image/png";
  public static final String VIDEO_MP4 = "video/mp4";

  // Time related constants
  public static final int FIVE_MINUTE = 5 * 60;
  public static final long DEFAULT_TIMEOUT = 30_000; // 30 seconds in milliseconds

  // VRFClient related constants
  public static final int VRF_VERSION = 1;

  // Private constructor to prevent instantiation
  private Constants() {
    throw new UnsupportedOperationException("Utility class");
  }
}