package org.tx;

import lombok.Getter;
import lombok.Setter;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RegisterAgentParam {

  private Address agent;
  private DynamicArray<Address> signers;
  private Uint8 threshold;
  private Address converterAddress;
  private Utf8String version;
  private Utf8String messageId;
  private Utf8String sourceAgentId;
  private Utf8String sourceAgentName;
  private Utf8String targetAgentId;
  private Uint256 timestamp;
  private Uint8 messageType; // request/response/event
  private Uint8 priority;   // high/medium/low
  private Uint256 ttl;

  public RegisterAgentParam(DynamicArray<Address> signers,
                            Uint8 threshold,
                            Address converterAddress,
                            Utf8String version,
                            Utf8String messageId,
                            Utf8String sourceAgentId,
                            Utf8String sourceAgentName,
                            Utf8String targetAgentId,
                            Uint256 timestamp,
                            Uint8 messageType,
                            Uint8 priority,
                            Uint256 ttl) {
    this.signers = signers;
    this.threshold = threshold;
    this.converterAddress = converterAddress;
    this.version = version;
    this.messageId = messageId;
    this.sourceAgentId = sourceAgentId;
    this.sourceAgentName = sourceAgentName;
    this.targetAgentId = targetAgentId;
    this.timestamp = timestamp;
    this.messageType = messageType;
    this.priority = priority;
    this.ttl = ttl;
  }

  public List<Type> toInputParameters() {
    List<Type> inputParameters = new ArrayList<>();
    DynamicStruct agentConfig = new DynamicStruct(version, messageId, sourceAgentId, sourceAgentName,
        targetAgentId, timestamp, messageType, priority, ttl);
    inputParameters.add(new DynamicStruct(signers, threshold, converterAddress, agentConfig));
    return inputParameters;
  }

}