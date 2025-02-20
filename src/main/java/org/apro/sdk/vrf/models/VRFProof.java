package org.apro.sdk.vrf.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apro.sdk.vrf.VRF;

import java.math.BigInteger;

@Data
public class VRFProof {
  @JsonProperty("requestId")
  private String requestId;

  @JsonProperty("proof")
  private ProofOrigin proof;

  public Proof convert() {
    return new Proof(
        VRF.createPoint(this.proof.getPublicXBigInteger(), this.proof.getPublicYBigInteger()),
        VRF.createPoint(this.proof.getGammaXBigInteger(), this.proof.getGammaYBigInteger()),
        this.proof.getCBigInteger(),
        this.proof.getSBigInteger(),
        this.proof.getSeedBigInteger(),
        this.proof.getOutputBigInteger()
    );
  }

  /**
   * Convert the proof to JSON string
   *
   * @return JSON string representation of the proof
   * @throws JsonProcessingException if JSON serialization fails
   */
  public String marshal() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }

  @Data
  public static class ProofOrigin {
    private String publicX;
    private String publicY;
    private String gammaX;
    private String gammaY;
    private String c;
    private String s;
    private String seed;
    private String output;

    public BigInteger getPublicXBigInteger() {
      return new BigInteger(publicX.replace("0x", ""), 16);
    }

    public BigInteger getPublicYBigInteger() {
      return new BigInteger(publicY.replace("0x", ""), 16);
    }

    public BigInteger getGammaXBigInteger() {
      return new BigInteger(gammaX.replace("0x", ""), 16);
    }

    public BigInteger getGammaYBigInteger() {
      return new BigInteger(gammaY.replace("0x", ""), 16);
    }

    public BigInteger getCBigInteger() {
      return new BigInteger(c.replace("0x", ""), 16);
    }

    public BigInteger getSBigInteger() {
      return new BigInteger(s.replace("0x", ""), 16);
    }

    public BigInteger getSeedBigInteger() {
      return new BigInteger(seed.replace("0x", ""), 16);
    }

    public BigInteger getOutputBigInteger() {
      return new BigInteger(output.replace("0x", ""), 16);
    }
  }
}