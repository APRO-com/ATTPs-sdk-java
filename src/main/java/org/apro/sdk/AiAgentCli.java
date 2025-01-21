package org.apro.sdk;

import lombok.Getter;
import org.apro.sdk.config.ChainConfig;
import org.apro.sdk.config.Constants;
import org.apro.sdk.params.AgentSettingsParams;
import org.apro.sdk.params.VerifyParams;
import org.apro.sdk.util.ChainUtil;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apro.sdk.config.Constants.*;

@Getter
public class AiAgentCli {

    private final ChainConfig config;
    private final Web3j web3j;

    public AiAgentCli(ChainConfig config) {
        this.config = config;
        this.web3j = Web3j.build(new HttpService(config.getServerUrl()));
    }

    public RawTransaction buildRegisterAgentTx(
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            AgentSettingsParams agentSettingsParams
    ) throws IOException {
        if (!checkTxBaseParams(nonce, gasPrice, gasLimit)) {
            throw new IllegalArgumentException("nonce|gasPrice|gasLimit must be less than zero");
        }
        String version = this.getAgentVersion(to);
        if (agentSettingsParams.getVersion() == null) {
            agentSettingsParams.setVersion(new Utf8String(version));
        } else if (!version.equals(agentSettingsParams.getVersion().toString())){
            throw new IllegalArgumentException("Agent version is not the same as the proxy agent's version");
        }

        List<Type> inputParameters = agentSettingsParams.toInputParameters();
        if (!isValidSourceAgentId(to, agentSettingsParams.getSourceAgentId().getValue())) {
            throw new IllegalArgumentException("Agent source id is already existed");
        }

        Function registerAgent = new Function(
            Constants.REGISTER_AGENT_FUNCTION_NAME,
            inputParameters,
            Collections.emptyList());
        return RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                to, BigInteger.ZERO, FunctionEncoder.encode(registerAgent));
    }

    public RawTransaction buildVerifyTx(
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            VerifyParams verifyParams
    ) {
        if (!checkTxBaseParams(nonce, gasPrice, gasLimit)) {
            throw new IllegalArgumentException("nonce|gasPrice|gasLimit must be less than zero");
        }
        Function verify = new Function(Constants.VERIFY_FUNCTION_NAME,
                verifyParams.toInputParameters(),
                Collections.emptyList());
        return RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                to, BigInteger.ZERO, FunctionEncoder.encode(verify));
    }

    /**
     * get the agent address through the transactionReceipt.
     * the txHash should be the transaction that accepted the agent
     *
     * @param txHash: the transaction that accepted the agent
     * @throws IOException
     */
    public String getAgentAddress(String txHash) throws IOException {
        EthGetTransactionReceipt transactionReceipt =
                this.web3j.ethGetTransactionReceipt(txHash).send();
        if (transactionReceipt.getTransactionReceipt().isPresent()) {
            List<Log> logs = transactionReceipt.getTransactionReceipt().get().getLogs();
            for (Log log : logs) {
                if (log.getTopics().contains(EventEncoder.encode(AgentRegistered))) {
                    return Numeric.toHexStringWithPrefixZeroPadded(Numeric.toBigInt(log.getTopics().get(1)), 40);
                }
            }
        }
        return "Transaction not found or still pending.";
    }

    public String converter(String converterAddress, String data) throws IOException {
        Function function = new Function(
            CONVERTER_FUNCTION_NAME,
            Arrays.asList(new DynamicBytes(Numeric.hexStringToByteArray(data))),
            Collections.singletonList(new TypeReference<DynamicBytes>() {})
        );

        List<Type> decoded = ChainUtil.getResult(this.web3j, converterAddress, function);
        DynamicBytes result = (DynamicBytes) decoded.get(0);
        return Numeric.toHexString(result.getValue());
    }

    public String getFactory(String proxy) throws IOException {
        Function function = new Function(
            AGENT_FACTORY_FUNCTION_NAME,
            Arrays.asList(),
            Collections.singletonList(new TypeReference<Address>() {})
        );
        List<Type> decoded = ChainUtil.getResult(this.web3j, proxy, function);
        Address result = (Address) decoded.get(0);
        return result.getValue();
    }

    public String getManager(String proxy) throws IOException {
        Function function = new Function(
            AGENT_MANAGER_FUNCTION_NAME,
            Arrays.asList(),
            Collections.singletonList(new TypeReference<Address>() {})
        );
        List<Type> decoded = ChainUtil.getResult(this.web3j, proxy,function);
        Address result = (Address) decoded.get(0);
        return result.getValue();
    }

    public String getAgentVersion(String proxy) throws IOException {
        Function function = new Function(
            AGENT_VERSION_FUNCTION_NAME,
            Arrays.asList(),
            Collections.singletonList(new TypeReference<Utf8String>() {})
        );
        String manager = getManager(proxy);
        List<Type> decoded = ChainUtil.getResult(this.web3j, manager, function);
        Utf8String result = (Utf8String) decoded.get(0);
        return result.getValue();
    }

    public boolean isValidSourceAgentId(String proxy, String agentId) throws IOException {
        Function function = new Function(
            IS_VALID_SOURCE_AGENT_ID_FUNCTION_NAME,
            Arrays.asList(new Utf8String(agentId)),
            Collections.singletonList(new TypeReference<Bool>() {})
        );
        String manager = getManager(proxy);
        List<Type> decoded = ChainUtil.getResult(this.web3j, manager, function);
        Bool result = (Bool) decoded.get(0);
        return result.getValue();
    }

    private boolean checkTxBaseParams(BigInteger nonce, BigInteger gasLimit, BigInteger gasPrice) {
      return nonce.compareTo(BigInteger.ZERO) >= 0
          && gasLimit.compareTo(BigInteger.ZERO) >= 0
          && gasPrice.compareTo(BigInteger.ZERO) >= 0;
    }
}
