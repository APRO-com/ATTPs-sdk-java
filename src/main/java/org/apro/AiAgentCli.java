package org.apro;

import org.tx.Constants;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.util.Utils.toBytes;

public class AiAgentCli {

  protected final Web3j web3j;

  public AiAgentCli(String url) {
    this.web3j = Web3j.build(new HttpService(url));
  }

  public EthSendTransaction broadcast(String hexSignedTransaction)
      throws IOException, TransactionException {
    EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexSignedTransaction).send();
    if (ethSendTransaction.hasError()) {
      throw new TransactionException(ethSendTransaction.getError().getMessage());
    }
    return ethSendTransaction;
  }

  public EthSendTransaction broadcast(byte[] signedTransaction)
      throws IOException, TransactionException {
    String hexValue = Numeric.toHexString(signedTransaction);
    return broadcast(hexValue);
  }

  /**
   * get the agent address through the transactionReceipt.
   * the {@txHash} should be the transaction that accepted the agent
   * @param txHash: the transaction that accepted the agent
   * @return: the agent address
   * @throws IOException
   */
  public String getAgentAddress(String txHash) throws IOException {
      EthGetTransactionReceipt transactionReceipt =
          this.web3j.ethGetTransactionReceipt(txHash).send();
      if (transactionReceipt.getTransactionReceipt().isPresent()) {
        List<Log> logs = transactionReceipt.getTransactionReceipt().get().getLogs();
        for (Log log : logs) {
          if (log.getTopics().contains(Constants.AGENT_REGISTER_TOPIC)) {
            return "0x"+log.getTopics().get(2).substring(26);
          }
        }
      }
      return "Transaction not found or still pending.";
  }

  public BigInteger getNonce(String addr) {
    try {
      EthGetTransactionCount getNonce = web3j.ethGetTransactionCount(addr, DefaultBlockParameterName.PENDING).send();
      if (getNonce == null){
        throw new RuntimeException("net error");
      }
      return getNonce.getTransactionCount();
    } catch (IOException e) {
      throw new RuntimeException("net error");
    }
  }

  public byte[] callConverter(String converterAddress, String fullReport)
      throws ExecutionException, InterruptedException {
    Function converter = new Function(Constants.CONVERTER_FUNCTION_NAME,
        List.of(new DynamicBytes(toBytes(fullReport))),
        List.of(new TypeReference<DynamicBytes>() {
        }));

    String encodedFunction = FunctionEncoder.encode(converter);
    org.web3j.protocol.core.methods.response.EthCall response = this.web3j.ethCall(
            Transaction.createEthCallTransaction(null, converterAddress, encodedFunction),
            DefaultBlockParameterName.LATEST)
        .sendAsync().get();

    List<Type> results = FunctionReturnDecoder.decode(response.getValue(), converter.getOutputParameters());
    System.out.println(results);
    byte[] value = ((DynamicBytes) results.get(0)).getValue();
    return Hash.sha3(value);
//    Utf8String preValue = (Utf8String) results.get(0);
//    System.out.println(preValue.getValue());
  }

}
