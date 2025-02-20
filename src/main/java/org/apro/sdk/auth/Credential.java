package org.apro.sdk.auth;

import lombok.AllArgsConstructor;

/**
 * Credential class for authentication
 */
@AllArgsConstructor
public class Credential {
  private final Auth signer;

  /**
   * Generate authorization header value
   * @param content Content to generate authorization for
   * @return Authorization header value
   * @throws Exception if authorization generation fails
   */
  public String generateAuthorization(String content) throws Exception {
    if (signer == null) {
      return "";
    }
    return signer.sign(content);
  }
}
