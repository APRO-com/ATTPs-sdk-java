package org.apro.sdk.params;

import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.abi.datatypes.reflection.Parameterized;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.List;


public class AgentSettingsStruct extends DynamicStruct {

    private String[] signers;

    private long threshold;

    private String convertAddress;

    private AgentHeader agentHeader;


    private static final List outputParameters = new ArrayList<>();

    static {
        outputParameters.addAll(
                List.of(
                        new TypeReference<DynamicArray<Address>>() {
                        },
                        new TypeReference<Uint8>() {
                        },
                        new TypeReference<Address>() {
                        },
                        new TypeReference<AgentHeader>() {
                        }));
    }


    public AgentSettingsStruct(
            @Parameterized(type = Address.class)
            DynamicArray<Address> signers,
            Uint8 threshold,
            Address convertAddress,
            AgentHeader agentHeader) {
        super(signers, threshold, convertAddress, agentHeader);
        this.signers = new String[signers.getValue().size()];
        for (int i = 0; i < this.signers.length; i++) {
            this.signers[i] = signers.getValue().get(i).toString();
        }
        this.threshold = threshold.getValue().longValue();
        this.convertAddress = convertAddress.getValue();
        this.agentHeader = agentHeader;
    }

    public static AgentSettingsStruct build(byte[] bytes) {
        List<Type> resultList =
                FunctionReturnDecoder.decode(Numeric.toHexString(bytes), outputParameters);

        return new AgentSettingsStruct(
                (DynamicArray<Address>) resultList.get(0),
                (Uint8) resultList.get(1),
                (Address) resultList.get(2),
                (AgentHeader) resultList.get(3));
    }


    public String[] getSigners() {
        return signers;
    }

    public void setSigners(String[] signers) {
        this.signers = signers;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public String getConvertAddress() {
        return convertAddress;
    }

    public void setConvertAddress(String convertAddress) {
        this.convertAddress = convertAddress;
    }

    public AgentHeader getAgentConfig() {
        return agentHeader;
    }

    public void setAgentConfig(AgentHeader agentHeader) {
        this.agentHeader = agentHeader;
    }
}
