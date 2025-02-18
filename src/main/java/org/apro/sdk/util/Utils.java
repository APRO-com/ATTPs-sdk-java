package org.apro.sdk.util;

import cn.hutool.core.util.HexUtil;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

  private static final Pattern ETH_ADDRESS_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");
  public static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
  private static final Pattern HEX_PATTERN = Pattern.compile("^0x[0-9a-fA-F]*$");
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final String ALPHANUMERIC = "0123456789ABCDEF";


  public static byte[] toBytes(String hex) {
    String hexWithoutPrefix = Numeric.cleanHexPrefix(hex);
    return Numeric.hexStringToByteArray(hexWithoutPrefix);
  }

  public static byte[] toKeccak256(String input) {
    String hexWithoutPrefix = Numeric.cleanHexPrefix(input);
    return Hash.sha3(HexUtil.decodeHex(hexWithoutPrefix));
  }

  public static boolean isValidEthereumAddress(String address) {
    if (address == null || address.isEmpty()) {
      return false;
    }
    return ETH_ADDRESS_PATTERN.matcher(address).matches();
  }

  public static boolean checkUUID(String uuid) {
    return UUID_REGEX.matcher(uuid).matches();
  }

  public static String generateUUID() {
     return UUID.randomUUID().toString();
  }

  public static String encodeSignaturesToString(List<Sign.SignatureData> signatures) {
    DynamicArray<Bytes32> rsArray = new DynamicArray<>(
        Bytes32.class,
        signatures.stream()
            .map(r -> new Bytes32(r.getR()))
            .collect(Collectors.toList())
    );

    DynamicArray<Bytes32> ssArray = new DynamicArray<>(
        Bytes32.class,
        signatures.stream()
            .map(s -> new Bytes32(s.getS()))
            .collect(Collectors.toList())
    );

    DynamicArray<Uint8> vArray = new DynamicArray<>(
        Uint8.class,
        signatures.stream()
            .map(v -> new Uint8(Long.parseLong(
                Hex.toHexString(v.getV()), 16)-27))
            .collect(Collectors.toList())
    );

    List<Type> parameters = Arrays.asList(rsArray, ssArray, vArray);
    return FunctionEncoder.encodeConstructor(parameters);
  }

  public static byte[] longToBytes(long x) {
    byte[] result = new byte[8];
    for (int i = 7; i >= 0; i--) {
      result[i] = (byte)(x & 0xFF);
      x >>= 8;
    }
    return result;
  }

  public static byte[] concat(byte[] a, byte[] b) {
    byte[] result = Arrays.copyOf(a, a.length + b.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  public static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  public static byte[] hexStringToBytes(String hex) {
    if (hex.startsWith("0x")) {
      hex = hex.substring(2);
    }
    int len = hex.length();
    byte[] result = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
    }
    return result;
  }

  public static boolean isHexString(String str) {
    return str != null && HEX_PATTERN.matcher(str).matches();
  }

  /**
   * Generate a secure random string of specified length
   */
  public static String secureRandomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
    }
    return sb.toString();
  }

}
