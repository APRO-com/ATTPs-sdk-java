package org.apro.sdk.config;

import lombok.Getter;
import lombok.Setter;

import static org.apro.sdk.config.Constants.*;

@Getter
@Setter
public class ChainConfig {

  private String serverUrl;
  private long chainId;
  private String proxyAddress;

  public ChainConfig(String serverUrl, long chainId, String proxyAddress) {
    this.serverUrl = serverUrl;
    this.chainId = chainId;
    this.proxyAddress = proxyAddress;
  }

  public static ChainConfig BSC_MAIN = new ChainConfig(
      BSC_MAIN_RPC,
      BSC_MAIN_CHAINID,
      BSC_TEST_PROXY_ADDRESS
  );

  public static ChainConfig BSC_TEST = new ChainConfig(
      BSC_TEST_RPC,
      BSC_TEST_CHAINID,
      BSC_MAIN_PROXY_ADDRESS
  );
}
