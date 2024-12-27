package org.util;

import cn.hutool.core.util.HexUtil;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.util.UUID;

public class Utils {
  public static byte[] toBytes(String hex) {
    String hexWithoutPrefix = Numeric.cleanHexPrefix(hex);
    return Numeric.hexStringToByteArray(hexWithoutPrefix);
  }

  public static byte[] toKeccak256(String input) {
    return Hash.sha3(HexUtil.decodeHex(input));
  }

  public static boolean checkUUID(String input) {
    try {
      UUID.fromString(input);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
