package org.apro.sdk.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Default implementation of Auth interface
 */
@Builder
@AllArgsConstructor
public class DefaultSigner implements Auth {
  private final String accessKey;
  private final String secretKey;

  @Override
  public String sign(String content) throws Exception {
    // Implement your signing logic here
    // This is just a placeholder implementation
    if (content == null || content.isEmpty()) {
      return "";
    }

    // Example format: "AccessKey=xxx,Signature=yyy"
    return String.format("AccessKey=%s,Signature=%s",
        accessKey,
        calculateSignature(content)
    );
  }

  private String calculateSignature(String content) {
    // Implement your actual signature calculation logic here
    // This might involve HMAC-SHA256 or other cryptographic functions
    return "signature-placeholder";
  }
}