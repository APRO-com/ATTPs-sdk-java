package org.apro.sdk.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apro.sdk.util.Utils;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentSettingsParams {

  @NotNull(message = "Signers must not be null")
  @NotEmpty(message = "Signers must not be empty")
  private DynamicArray<Address> signers;

  @NotNull(message = "Threshold must not be null")
  private Uint8 threshold;

  private Address converterAddress;

  @NotNull(message = "Version must not be null")
  private Utf8String version;

  @NotNull(message = "MessageId must not be null")
  @Pattern(regexp = Utils.UUID_REGEX, message = "MessageId is not a valid UUID")
  private Utf8String messageId;

  @NotNull(message = "SourceAgentId must not be null")
  @Pattern(regexp = Utils.UUID_REGEX, message = "SourceAgentId is not a valid UUID")
  private Utf8String sourceAgentId;

  private Utf8String sourceAgentName;

  @NotNull(message = "TargetAgentId must not be null")
  @Pattern(regexp = Utils.UUID_REGEX, message = "TargetAgentId is not a valid UUID")
  private Utf8String targetAgentId;

  @NotNull(message = "Timestamp must not be null")
  @PositiveOrZero(message = "Timestamp must be greater than or equal to zero")
  private Uint256 timestamp;

  @NotNull(message = "MessageType must not be null")
  private Uint8 messageType;

  @NotNull(message = "Priority must not be null")
  private Uint8 priority;

  @NotNull(message = "TTL must not be null")
  @PositiveOrZero(message = "TTL must be greater than or equal to zero")
  private Uint256 ttl;

  public List<Type> toInputParameters() {
    if (this.signers.getValue().isEmpty()) {
      throw new IllegalArgumentException("signers must not be empty");
    }
    if (this.messageId == null || this.messageId.getValue().isEmpty()) {
      this.messageId = new Utf8String(Utils.generateUUID());
    }
    if (this.sourceAgentId == null || this.sourceAgentId.getValue().isEmpty()) {
      this.sourceAgentId = new Utf8String(Utils.generateUUID());
    }
    if (this.targetAgentId == null || this.targetAgentId.getValue().isEmpty()) {
      throw new IllegalArgumentException("targetAgentId must not be empty");
    }
    if (!Utils.checkUUID(this.messageId.getValue()) || !Utils.checkUUID(this.sourceAgentId.getValue())
        || !Utils.checkUUID(this.targetAgentId.getValue())) {
      throw new IllegalArgumentException("messageId|sourceAgentId|targetAgentId is not a valid UUID");
    }
    int messageTypeInt = this.messageType.getValue().intValue();
    if (messageTypeInt != 0 && messageTypeInt != 1 && messageTypeInt != 2) {
      throw new IllegalArgumentException("messageType is not a valid message type");
    }
    int priorityTypeInt = this.priority.getValue().intValue();
    if (priorityTypeInt != 0 && priorityTypeInt != 1 && priorityTypeInt != 2) {
      throw new IllegalArgumentException("priority is not a valid type");
    }
    if (this.ttl.getValue().compareTo(BigInteger.ZERO) < 0) {
      throw new IllegalArgumentException("TTL must be greater than or equal to zero");
    }
    if (this.timestamp == null) {
      this.timestamp = new Uint256(BigInteger.valueOf(System.currentTimeMillis() / 1000));
    }
    if (this.timestamp.getValue().toString().matches("^\\d{13}$")) {
      this.timestamp = new Uint256(this.timestamp.getValue().divide(BigInteger.valueOf(1000)));
    }
    List<Type> inputParameters = new ArrayList<>();
    DynamicStruct agentConfig = new DynamicStruct(version, messageId, sourceAgentId, sourceAgentName,
        targetAgentId, timestamp, messageType, priority, ttl);
    inputParameters.add(new DynamicStruct(signers, threshold, converterAddress, agentConfig));
    return inputParameters;
  }
}