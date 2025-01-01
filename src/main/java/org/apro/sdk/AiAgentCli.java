package org.apro.sdk;

import org.apro.sdk.config.ChainConfig;
import org.apro.sdk.config.Constants;
import org.apro.sdk.params.AgentSettings;
import org.apro.sdk.params.VerifyParams;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.apro.sdk.config.Constants.AgentRegistered;

public class AiAgentCli {

    public ChainConfig config;
    public Web3j web3j;

    public AiAgentCli(ChainConfig config) {
        this.config = config;
        this.web3j = Web3j.build(new HttpService(config.getServerUrl()));
    }

    public static RawTransaction buildRegisterAgentTx(
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            AgentSettings agentSettings
    ) {
        Function registerAgent = new Function(
                Constants.REGISTER_AGENT_FUNCTION_NAME,
                agentSettings.toInputParameters(),
                Collections.emptyList());
        return RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                to, BigInteger.ZERO, FunctionEncoder.encode(registerAgent));
    }

    public static RawTransaction buildVerifyTx(
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            VerifyParams verifyParams
    ) {
        Function verify = new Function(Constants.VERIFY_FUNCTION_NAME,
                verifyParams.toInputParameters(),
                Collections.emptyList());
        return RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                to, BigInteger.ZERO, FunctionEncoder.encode(verify));
    }

    public byte[] signTx(RawTransaction tx, String priKey) {
        return signTx(tx, this.config.getChainId(), priKey);
    }

    public byte[] signTx(RawTransaction tx, long chainId, String priKey) {
        return TransactionEncoder.signMessage(tx, chainId, Credentials.create(priKey));
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

    public BigInteger getNonce(String address) {
        try {
            EthGetTransactionCount response = web3j.ethGetTransactionCount(address,
                    DefaultBlockParameterName.PENDING).send();
            return response.getTransactionCount();
        } catch (IOException e) {
            throw new RuntimeException("io error", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
