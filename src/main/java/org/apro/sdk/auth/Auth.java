package org.apro.sdk.auth;

/**
 * Auth interface for signing requests
 */
public interface Auth {
  /**
   * Generate signature for the given content
   * @param content Content to sign
   * @return Generated signature
   * @throws Exception if signing fails
   */
  String sign(String content) throws Exception;
}
