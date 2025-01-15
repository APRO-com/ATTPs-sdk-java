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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

  private static final Pattern ETH_ADDRESS_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");
  public static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

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
    // UUID v4 must be 36 characters long
    if (uuid == null || uuid.length() != 36) {
      return false;
    }

    for (int i = 0; i < uuid.length(); i++) {
      char c = uuid.charAt(i);

      if (i == 8 || i == 13 || i == 18 || i == 23) {
        // These positions must contain '-'
        if (c != '-') {
          return false;
        }
      } else if (i == 14) {
        // The 14th character (index 13) must be '4'
        if (c != '4') {
          return false;
        }
      } else if (i == 19) {
        // The 19th character (index 18) must be '8', '9', 'a', or 'b'
        if (!(c == '8' || c == '9' || c == 'a' || c == 'b' || c == 'A' || c == 'B')) {
          return false;
        }
      } else {
        // All other characters must be a hexadecimal digit
        if (!isHexDigit(c)) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') ||
        (c >= 'a' && c <= 'f') ||
        (c >= 'A' && c <= 'F');
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
}
