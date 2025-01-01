package org.apro.sdk.params;

import org.apro.sdk.util.Utils;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.util.ArrayList;
import java.util.List;

public class AgentSettings {

  private DynamicArray<Address> signers;
  private Uint8 threshold;
  // If the payload to be verified is obtained from the APRO DATA pull service, you should set the converterAddress to the specified address,
  // And the dataHash in the verify payload should be calculated by the converterAddress.converter(data).
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

  public AgentSettings() {}

  public List<Type> toInputParameters() {
    List<Type> inputParameters = new ArrayList<>();
    DynamicStruct agentConfig = new DynamicStruct(version, messageId, sourceAgentId, sourceAgentName,
        targetAgentId, timestamp, messageType, priority, ttl);
    inputParameters.add(new DynamicStruct(signers, threshold, converterAddress, agentConfig));
    return inputParameters;
  }

  public DynamicArray<Address> getSigners() {
    return signers;
  }

  public AgentSettings setSigners(DynamicArray<Address> signers) {
    this.signers = signers;
    return this;
  }

  public Uint8 getThreshold() {
    return threshold;
  }

  public AgentSettings setThreshold(Uint8 threshold) {
    this.threshold = threshold;
    return this;
  }

  public Address getConverterAddress() {
    return converterAddress;
  }

  public AgentSettings setConverterAddress(Address converterAddress) {
    this.converterAddress = converterAddress;
    return this;
  }

  public Utf8String getVersion() {
    return version;
  }

  public AgentSettings setVersion(Utf8String version) {
    this.version = version;
    return this;
  }

  public Utf8String getMessageId() {
    return messageId;
  }

  public AgentSettings setMessageId(Utf8String messageId) {
    if (!Utils.checkUUID(messageId.getValue())) {
      throw new IllegalArgumentException("messageId is not a legal UUID");
    }
    this.messageId = messageId;
    return this;
  }

  public Utf8String getSourceAgentId() {
    return sourceAgentId;
  }

  public AgentSettings setSourceAgentId(Utf8String sourceAgentId) {
    if (!Utils.checkUUID(sourceAgentId.getValue())) {
      throw new IllegalArgumentException("sourceAgentId is not a legal UUID");
    }
    this.sourceAgentId = sourceAgentId;
    return this;
  }

  public Utf8String getSourceAgentName() {
    return sourceAgentName;
  }

  public AgentSettings setSourceAgentName(Utf8String sourceAgentName) {
    this.sourceAgentName = sourceAgentName;
    return this;
  }

  public Utf8String getTargetAgentId() {
    return targetAgentId;
  }

  public AgentSettings setTargetAgentId(Utf8String targetAgentId) {
    this.targetAgentId = targetAgentId;
    return this;
  }

  public Uint256 getTimestamp() {
    return timestamp;
  }

  public AgentSettings setTimestamp(Uint256 timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public Uint8 getMessageType() {
    return messageType;
  }

  public AgentSettings setMessageType(Uint8 messageType) {
    this.messageType = messageType;
    return this;
  }

  public Uint8 getPriority() {
    return priority;
  }

  public AgentSettings setPriority(Uint8 priority) {
    this.priority = priority;
    return this;
  }

  public Uint256 getTtl() {
    return ttl;
  }

  public AgentSettings setTtl(Uint256 ttl) {
    this.ttl = ttl;
    return this;
  }
}