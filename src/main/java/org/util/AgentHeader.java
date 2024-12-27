package org.util;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgentHeader extends DynamicStruct {

    private String version;
    private String messageId;
    private String sourceAgentId;
    private String sourceAgentName;
    private String targetAgentId;

    private BigInteger timestamp;

    private int messageType;
    private int priority;

    private BigInteger ttl;


    private static final List outputParameters = new ArrayList<TypeReference<Type>>();

    static {
        outputParameters.addAll(
                Arrays.asList(
                        new TypeReference<Utf8String>() {
                        }, new TypeReference<Utf8String>() {
                        },
                        new TypeReference<Utf8String>() {
                        },
                        new TypeReference<Utf8String>() {
                        }, new TypeReference<Utf8String>() {
                        }, new TypeReference<Uint256>() {
                        }, new TypeReference<Uint8>() {
                        }, new TypeReference<Uint8>() {
                        }, new TypeReference<Uint256>() {
                        }));
    }

    public AgentHeader(
            Utf8String version,
            Utf8String messageId,
            Utf8String sourceAgentId,
            Utf8String sourceAgentName,
            Utf8String targetAgentId,
            Uint256 timestamp,
            Uint8 messageType,
            Uint8 priority,
            Uint256 ttl) {
        super(version, messageId, sourceAgentId, sourceAgentName, targetAgentId, timestamp, messageType, priority, ttl);
        this.version = version.getValue();
        this.messageId = messageId.getValue();
        this.sourceAgentId = sourceAgentId.getValue();
        this.sourceAgentName = sourceAgentName.getValue();
        this.targetAgentId = targetAgentId.getValue();
        this.timestamp = timestamp.getValue();
        this.messageType = messageType.getValue().intValue();
        this.priority = priority.getValue().intValue();
        this.ttl = ttl.getValue();
    }

    public static AgentHeader build(byte[] bytes) {
        List<Type> resultList =
                FunctionReturnDecoder.decode(Numeric.toHexString(bytes), outputParameters);

        return new AgentHeader(
                (Utf8String) resultList.get(0),
                (Utf8String) resultList.get(1),
                (Utf8String) resultList.get(2),
                (Utf8String) resultList.get(3),
                (Utf8String) resultList.get(4),
                (Uint256) resultList.get(5),
                (Uint8) resultList.get(6),
                (Uint8) resultList.get(7),
                (Uint256) resultList.get(8));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSourceAgentId() {
        return sourceAgentId;
    }

    public void setSourceAgentId(String sourceAgentId) {
        this.sourceAgentId = sourceAgentId;
    }

    public String getTargetAgentId() {
        return targetAgentId;
    }

    public void setTargetAgentId(String targetAgentId) {
        this.targetAgentId = targetAgentId;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public BigInteger getTtl() {
        return ttl;
    }

    public void setTtl(BigInteger ttl) {
        this.ttl = ttl;
    }

    public String getSourceAgentName() {
        return sourceAgentName;
    }

    public void setSourceAgentName(String sourceAgentName) {
        this.sourceAgentName = sourceAgentName;
    }
}
