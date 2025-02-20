package org.apro.sdk.vrf;

import lombok.Getter;
import lombok.Setter;
import org.apro.sdk.util.HttpClient;

/**
 * ATTPs API v1 Java SDK Service Type
 */
@Getter
@Setter
public class Service {
  protected HttpClient client;

  public Service(HttpClient client) {
    this.client = client;
  }
}
