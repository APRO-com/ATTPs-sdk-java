package org.apro.sdk;

import org.apro.sdk.params.AgentSettings;
import org.apro.sdk.params.VerifyParams;
import org.bouncycastle.util.encoders.Hex;
import org.apro.sdk.util.Utils;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apro.sdk.config.ChainConfig.BSC_TEST;

/**
 * The demo for register an agent and verify the message.
 */
public class AiAgentCliDemo {

  public void registerAgent() throws TransactionException, IOException {
    String address = "${EOA}"; // the user address
    String addressPrikey = "";
    String proxyAddress = ""; // the proxy address of the Apro ai agent
    AiAgentCli aiAgentCli = new AiAgentCli(BSC_TEST); // specify the rpc server
    BigInteger nonce = aiAgentCli.getNonce(address);

    List<String> signerList = new ArrayList<>();
    // add the signers
    signerList.add("${SIGNER1_ADDR}");
    signerList.add("${SIGNER2_ADDR}");
    // .....

    AgentSettings agentSettings = new AgentSettings();
    agentSettings
        .setSigners(new DynamicArray<>(Address.class, signerList.stream().map(Address::new).toList()))
        .setThreshold(new Uint8(2))
        // If the payload to be verified is obtained from the APRO DATA pull service, you should set the converterAddress to the specified address
        .setConverterAddress(new Address("0x0000000000000000000000000000000000000000"))
        .setVersion(new Utf8String("1.0"))
        .setMessageId(new Utf8String("333833c0-0b15-449c-815e-8040eff67c8d"))
        .setSourceAgentId(new Utf8String("2c167873-a6fc-4cee-b505-6c1ae2cd4763"))
        .setSourceAgentName(new Utf8String("sdk test"))
        .setTargetAgentId(new Utf8String("c1dd33c9-8196-4c7d-b035-1baab7966c73"))
        .setTimestamp(new Uint256(System.currentTimeMillis()/1000))
        .setMessageType(new Uint8(2))
        .setPriority(new Uint8(1))
        .setTtl(new Uint256(3600));

    RawTransaction rawTransaction = aiAgentCli.buildRegisterAgentTx(
        nonce,
        BigInteger.valueOf(5000000000L), // gas price
        BigInteger.valueOf(10000000),  // gas limit
        proxyAddress,
        agentSettings
    );

    byte[] signedTx = aiAgentCli.signTx(rawTransaction, addressPrikey);
    EthSendTransaction ethSendTransaction = aiAgentCli.broadcast(signedTx);
    if (ethSendTransaction.hasError()) {
      System.out.println(ethSendTransaction.getError().getMessage());
    }
    System.out.println(ethSendTransaction.getTransactionHash());
  }

  public void verifyMessage() throws TransactionException, IOException {
    String address = "${EOA}"; // the user address
    String addressPrikey = "";
    String proxyAddress = ""; // the proxy address of the Apro ai agent
    String agentAddress = "";  // The agent address that has been successfully registered
    String digest = "";  // The agent setting digest, obtained through the agent accept transaction log
    String data = Hex.toHexString("hello world".getBytes());
    // The dataHash is calculated by Keccak256 if the converterAddress is 0x0000000000000000000000000000000000000000.
    // But if the data is obtained from the APRO DATA pull service, you should set the converterAddress when register the agent,
    // The dataHash should be calculated by the converterAddress.converter(data).
    byte[] dataHashBytes = Utils.toKeccak256(data);
    String dataHash = Hex.toHexString(dataHashBytes);
    List<Sign.SignatureData> signatures = new ArrayList<>();
    Sign.SignatureData data1 = Sign.signMessage(Utils.toBytes(data), Credentials.create("${SIGNER1_ADDR}").getEcKeyPair());
    Sign.SignatureData data2 = Sign.signMessage(Utils.toBytes(data), Credentials.create("${SIGNER2_ADDR}").getEcKeyPair());
    signatures.add(data1);
    signatures.add(data2);

    AiAgentCli aiAgentCli = new AiAgentCli(BSC_TEST);
    BigInteger nonce = aiAgentCli.getNonce(address);

    VerifyParams verifyParams = new VerifyParams();
    verifyParams.setAgent(agentAddress)
        .setSettingsDigest(digest)
        .setData(data)
        .setDataHash(dataHash)
        .setSignatures(signatures)
        .setMetaDataStruct(null);

    RawTransaction rawTransaction = aiAgentCli.buildVerifyTx(
        nonce,
        BigInteger.valueOf(5000000000L),
        BigInteger.valueOf(10000000),
        proxyAddress,
        verifyParams
    );

    byte[] signedTx = aiAgentCli.signTx(rawTransaction, addressPrikey);
    EthSendTransaction ethSendTransaction = aiAgentCli.broadcast(signedTx);
    if (ethSendTransaction.hasError()) {
      System.out.println(ethSendTransaction.getError().getMessage());
    }
    System.out.println(ethSendTransaction.getTransactionHash());
  }
}