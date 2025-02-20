package org.apro.sdk.vrf;

import lombok.extern.slf4j.Slf4j;
import org.apro.sdk.vrf.models.Proof;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;
import org.web3j.crypto.Hash;

import java.math.BigInteger;
import java.util.Arrays;

@Slf4j
public class VRF {

  public static final X9ECParameters params = SECNamedCurves.getByName("secp256k1");
  public static final ECDomainParameters CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
  public static final ECParameterSpec CURVE_SPEC = new ECParameterSpec(params.getCurve(), params.getG(), params.getN(), params.getH());

  public static final ECPoint generator = CURVE_SPEC.getG();
  // field num on Fp
  public static final BigInteger fieldSize =
      CURVE_SPEC.getCurve().getField().getCharacteristic();
  // point num on Eclipse Curve
  public static final BigInteger groupOrder = CURVE_SPEC.getN();

  //some predefine constant
  public final static int HashLength = 32;
  private final static BigInteger zero, one, two, three, four, seven, eulerCriterionPower, sqrtPower;
  // some prefix, byte[32]
  private final static byte[] hashToCurveHashPrefix; //1
  private final static byte[] scalarFromCurveHashPrefix; //2
  public final static byte[] vrfRandomOutputHashPrefix; //3

  public static final int ProofLength = 64 + // PublicKey
      64 + // Gamma
      32 + // C
      32 + // S
      32 + // Seed
      32 + // uWitness (gets padded to 256 bits, even though it's only 160)
      64 + // cGammaWitness
      64 + // sHashWitness
      32; // zInv  (Leave Output out, because that can be efficiently calculated)


  static {
    zero = BigInteger.valueOf(0);
    one = BigInteger.valueOf(1);
    two = BigInteger.valueOf(2);
    three = BigInteger.valueOf(3);
    four = BigInteger.valueOf(4);
    seven = BigInteger.valueOf(7);

    eulerCriterionPower = fieldSize.subtract(one).divide(two); //  (p-1)/2
    sqrtPower = fieldSize.add(one).divide(four); // (p+1)/4

    hashToCurveHashPrefix = bytesToHash(one.toByteArray());
    scalarFromCurveHashPrefix = bytesToHash(two.toByteArray());
    vrfRandomOutputHashPrefix = bytesToHash(three.toByteArray());
  }


  public static ECPoint createPoint(BigInteger x, BigInteger y) {
    return CURVE_SPEC.getCurve().createPoint(x, y);
  }

  /**
   * get last HashLength byte of b; if len(b) < HashLength, padding with 0 before.
   * equal to function BigToHash in go module.
   */
  public static byte[] bytesToHash(byte[] b) {
    byte[] hash = new byte[HashLength];
    if (b.length > HashLength) {
      hash = Arrays.copyOfRange(b, b.length - HashLength, b.length);
    } else {
      System.arraycopy(b, 0, hash, HashLength - b.length, b.length);
    }
    return hash;
  }

  /**
   * linearCombination of scalar and EcPoint, [c]·p1 + [s]·p2
   */
  public ECPoint linearCombination(BigInteger c, ECPoint p1, BigInteger s, ECPoint p2) {
    ECPoint p11 = p1.multiply(c.mod(groupOrder)).normalize();
    ECPoint p22 = p2.multiply(s.mod(groupOrder)).normalize();
    return p11.add(p22).normalize();
  }

  /**
   * represent one ECPoint with byte array. ECPoint must be normalized already.
   */
  public static byte[] longMarshal(ECPoint p1) {
    byte[] x = BigIntegers.asUnsignedByteArray(32, p1.getRawXCoord().toBigInteger());
    byte[] y = BigIntegers.asUnsignedByteArray(32, p1.getRawYCoord().toBigInteger());
    byte[] merged = new byte[x.length + y.length];
    System.arraycopy(x, 0, merged, 0, x.length);
    System.arraycopy(y, 0, merged, x.length, y.length);
    return merged;
  }

  /**
   * MustHash returns the keccak256 hash, or panics on failure, 32 byte
   */
  public static byte[] mustHash(byte[] in) {
    return Hash.sha3(in);
  }

  /**
   * concat the 1,2,3,5th point which has the form of p.x||p.y, join uWitness, at last sha3 the result
   */
  public BigInteger scalarFromCurvePoints(ECPoint hash, ECPoint pk, ECPoint gamma, byte[] uWitness,
      ECPoint v) throws VRFException {
    if (!(hash.isValid() && pk.isValid() && gamma.isValid() && v.isValid())) {
      throw new VRFException("bad arguments to vrf.ScalarFromCurvePoints");
    }

    byte[] merged = new byte[32 + 64 + 64 + 64 + 64 + 20];
    System.arraycopy(scalarFromCurveHashPrefix, 0, merged, 0, 32);
    System.arraycopy(longMarshal(hash), 0, merged, 32, 64);
    System.arraycopy(longMarshal(pk), 0, merged, 96, 64);
    System.arraycopy(longMarshal(gamma), 0, merged, 160, 64);
    System.arraycopy(longMarshal(v), 0, merged, 224, 64);
    System.arraycopy(uWitness, 0, merged, 288, 20);
    byte[] mustHash = mustHash(merged);

    return new BigInteger(1, mustHash);
  }

  /**
   * convert sha3(message) to the field element on Fp
   */
  public BigInteger fieldHash(byte[] message) {
    byte[] hashResult = mustHash(message);
    BigInteger rv = new BigInteger(1, bytesToHash(hashResult));

    while (rv.compareTo(fieldSize) >= 0) {
      byte[] shortRV = bytesToHash(BigIntegers.asUnsignedByteArray(rv));
      rv = new BigInteger(1, mustHash(shortRV));
    }
    return rv;
  }

  /**
   * left pad byte 0 of slice to length l
   */
  public byte[] leftPadBytes(byte[] slice, int l) {
    if (slice.length >= l) {
      return slice;
    }

    byte[] newSlice = new byte[l];
    System.arraycopy(slice, 0, newSlice, l - slice.length, slice.length);
    return newSlice;
  }

  /**
   * convert uint256 to byte array, without sign byte
   */
  public byte[] uint256ToBytes32(BigInteger uint256) throws VRFException {
    if (BigIntegers.asUnsignedByteArray(uint256).length > HashLength) { //256=HashLength*8
      throw new VRFException("vrf.uint256ToBytes32: too big to marshal to uint256");
    }
    return leftPadBytes(BigIntegers.asUnsignedByteArray(uint256), HashLength);
  }

  /**
   * x => x^3 + 7，
   */
  public BigInteger ySquare(BigInteger x) {
    return x.modPow(three, fieldSize).add(seven).mod(fieldSize);
  }

  /**
   * check whether a BigInteger is the square of some element on Fp.
   */
  public boolean isSquare(BigInteger x) {
    return x.modPow(eulerCriterionPower, fieldSize).compareTo(one) == 0;
  }

  /**
   * check whether one BigInteger can be the x coordinate of Curve
   */
  public boolean isCurveXOrdinate(BigInteger x) {
    return isSquare(ySquare(x));
  }

  /**
   * SquareRoot returns a s.t. a^2=x, as long as x is a square
   */
  public BigInteger squareRoot(BigInteger x) {
    return x.modPow(sqrtPower, fieldSize);
  }

  public BigInteger neg(BigInteger f) {
    return fieldSize.subtract(f);
  }

  // projectiveSub(x1, z1, x2, z2) is the projective coordinates of x1/z1 - x2/z2
  public BigInteger[] projectiveSub(BigInteger x1, BigInteger z1, BigInteger x2, BigInteger z2) {
    BigInteger num1 = z2.multiply(x1);
    BigInteger num2 = neg(z1.multiply(x2));
    return new BigInteger[] {num1.add(num2).mod(fieldSize), z1.multiply(z2).mod(fieldSize)};
  }

  // projectiveMul(x1, z1, x2, z2) is projective coordinates of (x1/z1)×(x2/z2)
  public BigInteger[] projectiveMul(BigInteger x1, BigInteger z1, BigInteger x2, BigInteger z2) {
    return new BigInteger[] {x1.multiply(x2), z1.multiply(z2)};
  }

  /**
   * create an uncompressed ECPoint with coordinate x,y on Curve
   */
  public ECPoint setCoordinates(BigInteger x, BigInteger y) throws VRFException {
//    ECPoint rv = ECKey.CURVE_SPEC.getCurve().createPoint(x, y);
    ECPoint rv = CURVE_SPEC.getCurve().createPoint(x, y);
    if (!rv.isValid()) {
      throw new VRFException("point requested from invalid coordinates");
    }
    return rv;
  }

  /**
   * get a ECPoint whose coordinate x = hash(p.x||p.y||seed)
   */
  public ECPoint hashToCurve(ECPoint p, BigInteger seed) throws VRFException {
    if (!(p.isValid() && seed.toByteArray().length <= 256 && seed.compareTo(zero) >= 0)) {
      throw new VRFException("bad input to vrf.HashToCurve");
    }
    byte[] inputTo32Byte = uint256ToBytes32(seed);

    byte[] merged = new byte[32 + 64 + 32];
    System.arraycopy(hashToCurveHashPrefix, 0, merged, 0, 32);
    System.arraycopy(longMarshal(p), 0, merged, 32, 64);
    System.arraycopy(inputTo32Byte, 0, merged, 96, 32);

    BigInteger x = fieldHash(merged);

    while (!isCurveXOrdinate(x)) { // Hash recursively until x^3+7 is a square
      x = fieldHash(bytesToHash(BigIntegers.asUnsignedByteArray(x)));
    }
    BigInteger y_2 = ySquare(x);
    BigInteger y = squareRoot(y_2);
    ECPoint rv = setCoordinates(x, y);

    // Negate response if y odd
    if (y.mod(two).compareTo(one) == 0) {
      rv = rv.negate();
    }
    return rv;
  }

  /**
   * check if [c]·gamma ≠ [s]·hash as required by solidity verifier
   *
   * @return false if [c]·gamma ≠ [s]·hash else true
   */
  public boolean checkCGammaNotEqualToSHash(BigInteger c, ECPoint gamma, BigInteger s,
      ECPoint hash) {
    ECPoint p1 = gamma.multiply(c.mod(groupOrder)).normalize();
    ECPoint p2 = hash.multiply(s.mod(groupOrder)).normalize();
    return !p1.equals(p2);
  }

  /**
   * get last 160 bit of sha3(p.x||p.y)，equal to function EthereumAddress in go module
   */
  public byte[] getLast160BitOfPoint(ECPoint point) {
    byte[] sha3Result = mustHash(longMarshal(point));

    byte[] cv = new byte[20];
    System.arraycopy(sha3Result, 12, cv, 0, 20);
    return cv;
  }

  /**
   * VerifyVRFProof is true iff gamma was generated in the mandated way from the
   * given publicKey and seed, and no error was encountered
   */
  public boolean verifyVRFProof(Proof proof) throws ErrCGammaEqualsSHash, VRFException {
    if (!proof.wellFormed()) {
      throw new VRFException("badly-formatted proof");
    }
    ECPoint h = hashToCurve(proof.PublicKey, proof.Seed).normalize();

    boolean notEqual = checkCGammaNotEqualToSHash(proof.C, proof.getGamma(), proof.S, h);
    if (!notEqual) {
      throw new ErrCGammaEqualsSHash("c*γ = s*hash (disallowed in solidity verifier)");
    }

    ECPoint uPrime = linearCombination(proof.C, proof.PublicKey, proof.S, generator);
    ECPoint vPrime = linearCombination(proof.C, proof.getGamma(), proof.S, h);

    byte[] uWitness = getLast160BitOfPoint(uPrime);
    BigInteger cPrime = scalarFromCurvePoints(h, proof.PublicKey, proof.getGamma(), uWitness,
        vPrime);
    byte[] gammaRepresent = longMarshal(proof.getGamma());

    byte[] prefixAndGamma = new byte[vrfRandomOutputHashPrefix.length + gammaRepresent.length];
    System.arraycopy(vrfRandomOutputHashPrefix, 0, prefixAndGamma, 0,
        vrfRandomOutputHashPrefix.length);
    System.arraycopy(gammaRepresent, 0, prefixAndGamma, vrfRandomOutputHashPrefix.length,
        gammaRepresent.length);
    byte[] output = mustHash(prefixAndGamma);

    // check if point proof.β == point cPrime
    if (!(proof.C.compareTo(cPrime) == 0)) {
      return false;
    }
    // check if proof.Output == output
    if (!(proof.Output.compareTo(new BigInteger(1, output)) == 0)) {
      return false;
    }
    return true;
  }
}
