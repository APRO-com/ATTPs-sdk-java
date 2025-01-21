package org.apro.sdk.params;

import jakarta.validation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apro.sdk.util.Utils;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentSettingsParams {

  public static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  public static Validator validator = factory.getValidator();

  @NotNull(message = "Signers must not be null")
  private DynamicArray<Address> signers;

  @NotNull(message = "Threshold must not be null")
  private Uint8 threshold;

  private Address converterAddress;

  private Utf8String version;

  private Utf8String messageId;

  private Utf8String sourceAgentId;

  @NotNull(message = "sourceAgentName must not be null")
  private Utf8String sourceAgentName;

  @NotNull(message = "TargetAgentId must not be null")
  private Utf8String targetAgentId;

  private Uint256 timestamp;

  @NotNull(message = "MessageType must not be null")
  private Uint8 messageType;

  @NotNull(message = "Priority must not be null")
  private Uint8 priority;

  @NotNull(message = "TTL must not be null")
  private Uint256 ttl;

  public List<Type> toInputParameters() {
    Set<ConstraintViolation<AgentSettingsParams>> violations = validator.validate(this);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
    if (this.signers.getValue().isEmpty()) {
      throw new IllegalArgumentException("signers must not be empty");
    }
    if (this.signers.getValue().size() < threshold.getValue().intValue()) {
      throw new IllegalArgumentException("threshold must be less than or equal to signers length");
    }
    if (this.messageId == null || this.messageId.getValue().isEmpty()) {
      this.messageId = new Utf8String(Utils.generateUUID());
    }
    if (this.sourceAgentId == null || this.sourceAgentId.getValue().isEmpty()) {
      this.sourceAgentId = new Utf8String(Utils.generateUUID());
    }
    if (this.targetAgentId.getValue().isEmpty()) {
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