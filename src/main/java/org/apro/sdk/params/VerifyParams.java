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
  private String dataHash;
  private List<Sign.SignatureData> signatures;
  private byte[] merkleProofs;  // Not used yet, fill with null
  private byte[] zkProofs;  // Not used yet, fill with null
  private MetaDataStruct metaDataStruct;

   public List<Type> toInputParameters() {
     Address agentAddress = new Address(this.getAgent());
     Bytes32 digest = new Bytes32(Utils.toBytes(this.getSettingsDigest()));
     Bytes32 dataHashBytes = new Bytes32(Utils.toBytes(this.getDataHash()));
     byte[] dataBytes = Utils.toBytes(this.getData());

     String signatureProof = encodeSignaturesToString(this.getSignatures());

     DynamicStruct proofs = new DynamicStruct(
         new DynamicBytes(new byte[]{}), // todo: fill with zkProof
         new DynamicBytes(new byte[]{}),  // todo: fill with merkleProof
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
    this.dataHash = dataHash;
    return this;
  }

  public VerifyParams setSignatures(List<Sign.SignatureData> signatures) {
    this.signatures = signatures;
    return this;
  }

  public VerifyParams setMerkleProofs(byte[] merkleProofs) {
    this.merkleProofs = merkleProofs;
    return this;
  }

  public VerifyParams setZkProofs(byte[] zkProofs) {
    this.zkProofs = zkProofs;
    return this;
  }

  public VerifyParams setMetaDataStruct(MetaDataStruct metaDataStruct) {
    this.metaDataStruct = metaDataStruct;
    return this;
  }

}
