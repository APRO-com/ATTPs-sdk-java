package org.apro.sdk.config;

import org.apro.sdk.params.AgentSettingsStruct;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;

import java.util.Arrays;

public class Constants {

  // chain info
  public static final long BSC_TEST_CHAINID = 97;
  public static final String BSC_TEST_RPC = "https://bsc-testnet-rpc.publicnode.com";
  public static final String BSC_TEST_PROXY_ADDRESS = "0x5E787A4131Cf9fC902C99235df5C8314C816DA11";

  public static final long BSC_MAIN_CHAINID = 56;
  public static final String BSC_MAIN_RPC = "https://binance.llamarpc.com";
  public static final String BSC_MAIN_PROXY_ADDRESS = "";


  // function name
  public static final String REGISTER_AGENT_FUNCTION_NAME = "createAndRegisterAgent";
  public static final String VERIFY_FUNCTION_NAME = "verify";
  public static final String CONVERTER_FUNCTION_NAME = "converter";
  public static final String AGENT_FACTORY_FUNCTION_NAME = "agentFactory";
  public static final String AGENT_MANAGER_FUNCTION_NAME = "agentManager";
  public static final String AGENT_VERSION_FUNCTION_NAME = "agentVersion";
  public static final String IS_VALID_SOURCE_AGENT_ID_FUNCTION_NAME = "isValidSourceAgentId";


  // event AgentRegistered(address indexed agent, Common.AgentSettings agentSettings);
  public static Event AgentRegistered = new Event("AgentRegistered",
          Arrays.asList(
                  new TypeReference<Address>(true) {
                  },
                  new TypeReference<AgentSettingsStruct>(false) {
                  }
          ));
}
