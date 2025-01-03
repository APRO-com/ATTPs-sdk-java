package org.apro.sdk.util;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ChainUtil {

  public static List<Type> getResult(Web3j web3j, String proxy, String method,
                                     List<Type> inputParameters, List<TypeReference<?>> outputParameters)
      throws IOException {
    Function function = new Function(
        method,
        inputParameters,
        outputParameters
    );

    String encodedFunction = FunctionEncoder.encode(function);
    Transaction transaction = Transaction.createEthCallTransaction(
        null,
        proxy,
        encodedFunction
    );

    EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

    if (response.getError() != null) {
      throw new IOException(response.getError().getMessage());
    } else {
      String value = response.getValue();
      return FunctionReturnDecoder.decode(value, function.getOutputParameters());
    }
  }
}
