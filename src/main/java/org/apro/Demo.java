package org.apro;

import org.bouncycastle.util.encoders.Hex;
import org.tx.TransactionManager;
import org.util.Utils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The demo for register an agent & verify the message.
 */
public class Demo {
  public static void main(String[] args) {
    System.out.println("Hello world!");
  }

  public void registerAgent() throws TransactionException, IOException {
    String address = "${EOA}"; // the user address
    String proxyAddress = ""; // the proxy address of the Apro ai agent
    AiAgentCli aiAgentCli = new AiAgentCli("${CHAIN_RPC}"); // specify the rpc server
    BigInteger nonce = aiAgentCli.getNonce(address);

    List<String> signerList = new ArrayList<>();
    // add the signers
    signerList.add("${SIGNER1_ADDR}");
    signerList.add("${SIGNER2_ADDR}");
    // .....

    RawTransaction rawTransaction = TransactionManager.buildRegisterAgentTx(
        nonce,
        BigInteger.valueOf(100000000), // gas price
        BigInteger.valueOf(10000000),  // gas limit
        proxyAddress,
        signerList,
        2,
        "0x0000000000000000000000000000000000000000", // if the payload data is obtained from the DATA pull service of the APRO, change this to the specified address
        "1.0",
        "333833c0-0b15-449c-815e-8040eff67c8d",  // uuid
        "2c167873-a6fc-4cee-b505-6c1ae2cd4763",
        "sdk test",
        "",
        System.currentTimeMillis()/1000,
        0,
        1,
        3600
    );

    byte[] signedTx = TransactionManager.signTx(rawTransaction, address);
    EthSendTransaction ethSendTransaction = aiAgentCli.broadcast(signedTx);
    if (ethSendTransaction.hasError()) {
      System.out.println(ethSendTransaction.getError().getMessage());
    }
    System.out.println(ethSendTransaction.getTransactionHash());
  }

  public void verifyMessage() throws TransactionException, IOException {
    String address = "${EOA}"; // the user address
    String proxyAddress = ""; // the proxy address of the Apro ai agent
    String agentAddress = "";  // The agent address that has been successfully registered
    String digest = "";  // The agent setting digest, obtained through the agent accept transaction log
    String data = Hex.toHexString("hello world".getBytes());
    byte[] dataHashBytes = Utils.toKeccak256(data);
    String dataHash = Hex.toHexString(dataHashBytes);
    List<Sign.SignatureData> signatures = new ArrayList<>();
    Sign.SignatureData data1 = Sign.signMessage(Utils.toBytes(data), Credentials.create("${SIGNER1_ADDR}").getEcKeyPair());
    Sign.SignatureData data2 = Sign.signMessage(Utils.toBytes(data), Credentials.create("${SIGNER2_ADDR}").getEcKeyPair());
    signatures.add(data1);
    signatures.add(data2);

    AiAgentCli aiAgentCli = new AiAgentCli("${CHAIN_RPC}");
    BigInteger nonce = aiAgentCli.getNonce(address);

    RawTransaction rawTransaction = TransactionManager.buildVerifyTx(
        nonce,
        BigInteger.valueOf(100000000),
        BigInteger.valueOf(10000000),
        proxyAddress,
        agentAddress,
        digest,
        data,
        dataHash,
        signatures
    );

    byte[] signedTx = TransactionManager.signTx(rawTransaction, address);
    EthSendTransaction ethSendTransaction = aiAgentCli.broadcast(signedTx);
    if (ethSendTransaction.hasError()) {
      System.out.println(ethSendTransaction.getError().getMessage());
    }
    System.out.println(ethSendTransaction.getTransactionHash());
  }
}