package org.apro.sdk.params;

import jakarta.validation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apro.sdk.util.Utils;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Sign;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

import static org.apro.sdk.util.Utils.encodeSignaturesToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyParams {

  public static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  public static Validator validator = factory.getValidator();

  @NotNull
  private String agent;

  @NotNull
  private String settingsDigest;

  @NotNull(message = "Data cannot be empty")
  private String data;

  @NotNull
  private String dataHash;

  @NotNull(message = "Signatures must contain at least one signature")
  private List<Sign.SignatureData> signatures;

  private byte[] zkProofs;

  private byte[] merkleProofs;

  private MetaDataStruct metaDataStruct;

  public List<Type> toInputParameters() {

    Set<ConstraintViolation<VerifyParams>> violations = validator.validate(this);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }

    Address agentAddress = new Address(this.getAgent());
    Bytes32 digest = new Bytes32(Utils.toBytes(this.getSettingsDigest()));
    Bytes32 dataHashBytes = new Bytes32(Utils.toBytes(this.getDataHash()));
    byte[] dataBytes = Utils.toBytes(this.getData());

    String signatureProof = encodeSignaturesToString(this.getSignatures());

    DynamicStruct proofs = new DynamicStruct(
        new DynamicBytes(zkProofs != null ? zkProofs : new byte[0]),
        new DynamicBytes(merkleProofs != null ? merkleProofs : new byte[0]),
        new DynamicBytes(Utils.toBytes(signatureProof))
    );
    if (metaDataStruct == null) {
      metaDataStruct = new MetaDataStruct("","","");
    }
    DynamicStruct metadata = new DynamicStruct(
        new Utf8String(metaDataStruct.getContentType()),
        new Utf8String(metaDataStruct.getEncoding()),
        new Utf8String(metaDataStruct.getCompression())
    );
    DynamicStruct messagePayload = new DynamicStruct(
        new DynamicBytes(dataBytes),
        dataHashBytes,
        proofs,
        metadata
    );

    return List.of(agentAddress, digest, messagePayload);
  }
}
