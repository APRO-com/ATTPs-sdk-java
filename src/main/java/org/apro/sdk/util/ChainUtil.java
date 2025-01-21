package org.apro.sdk.util;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class ChainUtil {

  public static byte[] signTx(RawTransaction tx, long chainId, String priKey) {
    return TransactionEncoder.signMessage(tx, chainId, Credentials.create(priKey));
  }

  public static byte[] signTx(RawTransaction tx, long chainId, Credentials credentials) {
    return TransactionEncoder.signMessage(tx, chainId, credentials);
  }

  public static EthSendTransaction broadcast(String hexSignedTransaction, Web3j web3j)
      throws IOException {
    return web3j.ethSendRawTransaction(hexSignedTransaction).send();
  }

  public static EthSendTransaction broadcast(byte[] signedTransaction, Web3j web3j)
      throws IOException {
    String hexValue = Numeric.toHexString(signedTransaction);
    return broadcast(hexValue, web3j);
  }

  public static BigInteger getNonce(String address, Web3j web3j) {
    try {
      EthGetTransactionCount response = web3j.ethGetTransactionCount(address,
          DefaultBlockParameterName.LATEST).send();
      return response.getTransactionCount();
    } catch (IOException e) {
      throw new RuntimeException("io error", e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Type> getResult(Web3j web3j, String to, Function function)
      throws IOException {
    String encodedFunction = FunctionEncoder.encode(function);
    Transaction transaction = Transaction.createEthCallTransaction(
        Address.DEFAULT.getValue(),
        to,
        encodedFunction
    );

    EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

    if (response.getError() != null) {
      throw new RuntimeException(response.getError().getMessage());
    } else {
      String value = response.getValue();
      return FunctionReturnDecoder.decode(value, function.getOutputParameters());
    }
  }
}
