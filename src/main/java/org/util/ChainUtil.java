package org.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.HexUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import com.github.arteam.simplejsonrpc.client.exception.JsonRpcException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.RawTransaction;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ChainUtil {

    public JsonRpcClient client;

    public ChainUtil(JsonRpcClient client) {
        this.client = client;
    }

    public ChainUtil(String rpc) {
        this.client = defaultClient(rpc);
    }

    public List<Type> queryFunction(String contract, Function function) {
        return queryFunction(new Address(contract), function);
    }

    public String queryFunctionString(Address contract, Function function, boolean memoryReturn) {
        Map<String, Object> jsonObj = new HashMap<>();
        jsonObj.put("to", contract.getValue());
        jsonObj.put("data", Numeric.prependHexPrefix((Numeric.cleanHexPrefix(calcFuncHash(function)))));
        try {
            String result = client.createRequest()
                    .id(1)
                    .method("eth_call")
                    .params(jsonObj, "latest")
                    .returnAs(String.class)
                    .execute();
            if (memoryReturn) {
                result = Numeric.prependHexPrefix(result.substring(66));
            }
            return result;
        } catch (Exception e) {
            if (e instanceof JsonRpcException &&
                    ((JsonRpcException) e).getErrorMessage().getMessage() != null) {
                log.info(((JsonRpcException) e).getErrorMessage().getMessage());
            }
            throw e;
        }

    }

    public List<Type> queryFunction(Address contract, Function function) {
        Map<String, Object> jsonObj = new HashMap<>();
        jsonObj.put("to", contract.getValue());
        jsonObj.put("data", Numeric.prependHexPrefix((Numeric.cleanHexPrefix(calcFuncHash(function)))));
        try {
            String result = client.createRequest()
                    .id(1)
                    .method("eth_call")
                    .params(jsonObj, "latest")
                    .returnAs(String.class)
                    .execute();
            return FunctionReturnDecoder.decode(result, function.getOutputParameters());
        } catch (Exception e) {
            if (e instanceof JsonRpcException &&
                    ((JsonRpcException) e).getErrorMessage().getMessage() != null) {
                log.info(((JsonRpcException) e).getErrorMessage().getMessage());
            }
            throw e;
        }
    }

    public static JsonRpcClient defaultClient(String rpc) {
        OkHttpClient oc = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(10))
                .build();

        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new JsonRpcClient(request -> {
            Response res = oc.newCall(new Request.Builder()
                    .url(rpc)
                    .post(RequestBody.create(request, MediaType.parse("application/json; charset=utf-8")))
                    .build()).execute();

            return res.body().string();
        }, om);
    }

    public static String calcFuncHash(Function function) {
        return FunctionEncoder.encode(function);
    }

    /*
      input example: Transfer(address,address,uint256)
    */
    public static String calcFuncHash(String funcDeclare) {
        return FunctionEncoder.encode(new Function(funcDeclare, Collections.emptyList(), Collections.emptyList()));
    }

    public static RawTransaction buildEthTransaction(
            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit,
            String to, BigInteger value) {
        return RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit,
                to, value);
    }

    public static RawTransaction buildTransaction(
            BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit,
            String to, String data) {
        return RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, data);
    }

    public static RawTransaction buildTransaction(
            long chainId, BigInteger nonce, BigInteger gasLimit,
            String to, BigInteger value, String data,
            BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas) {
        return RawTransaction.createTransaction(chainId, nonce, gasLimit, to, value, data,
                maxPriorityFeePerGas, maxFeePerGas);
    }

    public static String broadcast(JsonRpcClient client, String signedTx) {
        return client.createRequest()
                .id(1)
                .method("eth_sendRawTransaction")
                .params(signedTx)
                .returnAs(String.class)
                .execute();
    }

    public static BigInteger estimateGas(
            JsonRpcClient client, Address from, Address to, BigInteger value, String data) {
        Map<String, Object> jsonObj = new HashMap<>();
        jsonObj.put("from", from.getValue());
        jsonObj.put("to", to.getValue());
        jsonObj.put("value", Numeric.toHexStringWithPrefix(value));
        jsonObj.put("data", Numeric.prependHexPrefix(Numeric.cleanHexPrefix(data)));

        String result = client.createRequest()
                .id(1)
                .method("eth_estimateGas")
                .params(jsonObj)
                .returnAs(String.class)
                .execute();

        if (CharSequenceUtil.isEmpty(data)) {
            return HexUtil.toBigInteger(result);
        }
        return (new BigDecimal(HexUtil.toBigInteger(result)).multiply(new BigDecimal("1.2"))).toBigInteger();
    }




}

