package org.tx;

import org.bouncycastle.util.encoders.Hex;
import org.util.Utils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.*;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionManager {

  public static RawTransaction buildRegisterAgentTx(
      BigInteger nonce,
      BigInteger gasPrice,
      BigInteger gasLimit,
      String proxy,
      List<String> signers,
      int threshold,
      String converterAddress,
      String version,
      String messageId,
      String sourceAgentId,
      String sourceAgentName,
      String targetAgentId,
      long timestamp,
      int messageType,
      int priority,
      long ttl
  ) {

    if (!Utils.checkUUID(messageId) || !Utils.checkUUID(sourceAgentId)) {
      throw new IllegalArgumentException("messageId or sourceAgentId is not a legal UUID");
    }

    RegisterAgentParam registerAgentParam = new RegisterAgentParam(
        new DynamicArray<>(Address.class, signers.stream().map(Address::new).toList()),
        new Uint8(threshold),
        new Address(converterAddress),
        new Utf8String(version),
        new Utf8String(messageId),
        new Utf8String(sourceAgentId),
        new Utf8String(sourceAgentName),
        new Utf8String(targetAgentId),
        new Uint256(timestamp),
        new Uint8(messageType),
        new Uint8(priority),
        new Uint256(ttl)
    );
    Function registerAgent = new Function(
        Constants.REGISTER_AGENT_FUNCTION_NAME,
        registerAgentParam.toInputParameters(),
        Collections.emptyList());
    return createTransaction(nonce, gasPrice, gasLimit,
        proxy, BigInteger.ZERO, FunctionEncoder.encode(registerAgent));
  }

  public static RawTransaction buildVerifyTx(
      BigInteger nonce,
      BigInteger gasPrice,
      BigInteger gasLimit,
      String to,
      String agent,
      String settingsDigest,
      String data,
      String dataHash,
      List<Sign.SignatureData> signatures
  ) {
    Address agentAddress = new Address(agent);
    Bytes32 digest = new Bytes32(Utils.toBytes(settingsDigest));
    Bytes32 dataHashBytes = new Bytes32(Utils.toBytes(dataHash));
    byte[] dataBytes = Utils.toBytes(data);

    String signatureProof = encodeReportSignatures(signatures);

    DynamicStruct proofs = new DynamicStruct(
        new DynamicBytes(new byte[]{}),
        new DynamicBytes(new byte[]{}),
        new DynamicBytes(Utils.toBytes(signatureProof))
    );
    DynamicStruct metadata = new DynamicStruct(
        new Utf8String(""),
        new Utf8String(""),
        new Utf8String("")
    );
    DynamicStruct messagePayload = new DynamicStruct(
        new DynamicBytes(dataBytes),
        dataHashBytes,
        proofs,
        metadata
    );

    Function verify = new Function(Constants.VERIFY_FUNCTION_NAME,
        List.of(agentAddress, digest, messagePayload),
        Collections.emptyList());

    return createTransaction(nonce, gasPrice, gasLimit,
        to, BigInteger.ZERO, FunctionEncoder.encode(verify));
  }


  public static byte[] signTx(RawTransaction tx, String priKey) {
    return signTx(tx, Constants.CHAINID, priKey);
  }

  public static byte[] signTx(RawTransaction tx, long chainId, String priKey) {
    return TransactionEncoder.signMessage(tx, chainId, Credentials.create(priKey));
  }

  /**
   * Using for signing the specified message, such as {payload.data}
   * @param message
   * @param priKey
   * @return
   */
  public static Sign.SignatureData signMessage(byte[] message, String priKey) {
    Credentials credentials = Credentials.create(priKey);
    return Sign.signMessage(message, credentials.getEcKeyPair());
  }

  private static String encodeReportSignatures(List<Sign.SignatureData> signatures) {
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

  public static RawTransaction createTransaction(
      BigInteger nonce,
      BigInteger gasPrice,
      BigInteger gasLimit,
      String to,
      String data) {
    return createTransaction(nonce, gasPrice, gasLimit, to, BigInteger.ZERO, data);
  }

  public static RawTransaction createTransaction(
      BigInteger nonce,
      BigInteger gasPrice,
      BigInteger gasLimit,
      String to,
      BigInteger value,
      String data) {
    return RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
        to, value, data);
  }

  /*private static byte[] getReportHash(String converterAddr, String fullReport) {
    Function converter = new Function(CONVERTER_FUNCTION_NAME,
        List.of(new DynamicBytes(toBytes(fullReport))),
        List.of(new TypeReference<DynamicBytes>() {
        }));

    List<Type> types = new ChainUtil(BITLAYER_RPC).queryFunction(converterAddr, converter);
    byte[] value = ((DynamicBytes) types.get(0)).getValue();
    return Hash.sha3(value);
  }

  public static byte[] callConverter(Web3j web3j, String converterAddress, String fullReport)
      throws ExecutionException, InterruptedException {
    Function converter = new Function(CONVERTER_FUNCTION_NAME,
        List.of(new DynamicBytes(toBytes(fullReport))),
        List.of(new TypeReference<DynamicBytes>() {
        }));

    String encodedFunction = FunctionEncoder.encode(converter);
    org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
            Transaction.createEthCallTransaction(null, converterAddress, encodedFunction),
            DefaultBlockParameterName.LATEST)
        .sendAsync().get();

    List<Type> results = FunctionReturnDecoder.decode(response.getValue(), converter.getOutputParameters());
    byte[] value = ((DynamicBytes) results.get(0)).getValue();
    return Hash.sha3(value);
//    Utf8String preValue = (Utf8String) results.get(0);
//    System.out.println(preValue.getValue());
  }*/

}
