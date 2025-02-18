package org.apro.sdk.vrf.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apro.sdk.vrf.VRF;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;

/**
 * Proof represents a proof that Gamma was constructed from the Seed
 * according to the process mandated by the PublicKey.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Proof {

  public ECPoint PublicKey;
  public ECPoint Gamma;
  public BigInteger C;
  public BigInteger S;
  public  BigInteger Seed;
  public BigInteger Output;

  @Override
  public String toString() {
    return String.format(
        "vrf.Proof{PublicKey: {x:%x, y:%x}, Gamma: {x:%x, y:%x}, C:%x, S:%x, Seed:%x, Output:%x}",
        PublicKey.getRawXCoord().toBigInteger(),
        PublicKey.getRawYCoord().toBigInteger(),
        Gamma.getRawXCoord().toBigInteger(),
        Gamma.getRawYCoord().toBigInteger(),
        C,
        S,
        Seed,
        Output);
  }

  //WellFormed is true if proof's attributes satisfy basic domain checks
  public boolean wellFormed() {
    BigInteger groupOrder = VRF.groupOrder;
    if (!PublicKey.isValid()) {
      return false;
    }
    if (!Gamma.isValid()) {
      return false;
    }
    if (!(C.compareTo(groupOrder) < 0)) {
      return false;
    }
    if (!(S.compareTo(groupOrder) < 0)) {
      return false;
    }
    if (!(BigIntegers.asUnsignedByteArray(Output).length <= VRF.HashLength)) {
      return false;
    }
    return true;
  }
}
