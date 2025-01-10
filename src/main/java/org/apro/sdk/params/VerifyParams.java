package org.apro.sdk.params;

import lombok.Getter;
import org.apro.sdk.util.Utils;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Sign;

import java.util.List;

import static org.apro.sdk.util.Utils.encodeSignaturesToString;

@Getter
public class VerifyParams {
  private String agent;
  private String settingsDigest;
  private String data;
  // If the payload to be verified is obtained from the APRO DATA pull service, you should set the converterAddress to the specified address,
  // And the dataHash in the verify payload should be calculated by the converterAddress.converter(data).
  private String dataHash;
  private List<Sign.SignatureData> signatures;
  private byte[] zkProofs;  // Not used yet, fill with null
  private byte[] merkleProofs;  // Not used yet, fill with null
  private MetaDataStruct metaDataStruct;

   public List<Type> toInputParameters() {
     Address agentAddress = new Address(this.getAgent());
     Bytes32 digest = new Bytes32(Utils.toBytes(this.getSettingsDigest()));
     Bytes32 dataHashBytes = new Bytes32(Utils.toBytes(this.getDataHash()));
     byte[] dataBytes = Utils.toBytes(this.getData());

     String signatureProof = encodeSignaturesToString(this.getSignatures());

     DynamicStruct proofs = new DynamicStruct(
         new DynamicBytes(zkProofs),
         new DynamicBytes(merkleProofs),
         new DynamicBytes(Utils.toBytes(signatureProof))
     );
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

  public VerifyParams setAgent(String agent) {
    if (!Utils.isValidEthereumAddress(agent)) {
      throw new IllegalArgumentException("Invalid Ethereum address: " + agent);
    }
    this.agent = agent;
    return this;
  }

  public VerifyParams setSettingsDigest(String settingsDigest) {
    this.settingsDigest = settingsDigest;
    return this;
  }

  public VerifyParams setData(String data) {
    this.data = data;
    return this;
  }

  public VerifyParams setDataHash(String dataHash) {
    if (dataHash.length() != 64 && dataHash.length() != 66) {
      throw new IllegalArgumentException("Invalid data hash: " + dataHash);
    }
    this.dataHash = dataHash;
    return this;
  }

  public VerifyParams setSignatures(List<Sign.SignatureData> signatures) {
    if (signatures.isEmpty()) {
      throw new IllegalArgumentException("Signatures must contain at least one signature");
    }
    this.signatures = signatures;
    return this;
  }

  public VerifyParams setMerkleProofs(byte[] merkleProofs) {
    this.merkleProofs = merkleProofs == null ? new byte[0] : merkleProofs;
    return this;
  }

  public VerifyParams setZkProofs(byte[] zkProofs) {
    this.zkProofs = zkProofs == null ? new byte[0] : zkProofs;
    return this;
  }

  public VerifyParams setMetaDataStruct(MetaDataStruct metaDataStruct) {
    this.metaDataStruct = metaDataStruct == null ?
        new MetaDataStruct("","","") : metaDataStruct;
    return this;
  }

}
