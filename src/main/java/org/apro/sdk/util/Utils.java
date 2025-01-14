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
    try {
      UUID.fromString(uuid);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
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
