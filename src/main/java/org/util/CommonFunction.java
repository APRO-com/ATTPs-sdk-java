package org.util;

import cn.hutool.core.util.HexUtil;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface CommonFunction {

    Function decimals = new Function("decimals", Collections.emptyList(),
            List.of(new TypeReference<Uint8>() {
            }));

    Event MessageVerified = new Event("MessageVerified",
            Arrays.asList(
                    new TypeReference<Bytes32>(true) {
                    },
                    new TypeReference<Bytes32>(true) {
                    },
                    new TypeReference<DynamicBytes>(false) {
                    },
                    new TypeReference<ProofsStruct>(false) {
                    },
                    new TypeReference<MetaDataStruct>(false) {
                    }
            ));
    Event AddedAccess = new Event("AddedAccess",
            Arrays.asList(
                    new TypeReference<Address>(false) {
                    }));


    static Function getAgentConfig(String agent, String configDigest) {
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Address(agent));
        inputParameters.add(new Bytes32(HexUtil.decodeHex(Numeric.cleanHexPrefix(configDigest))));

        List<TypeReference<?>> outputParameters = Arrays.<TypeReference<?>>asList(
                new TypeReference<Uint256>() {
                },
                new TypeReference<DynamicArray<Address>>() {
                },
                new TypeReference<Uint8>() {
                },
                new TypeReference<Address>() {
                },
                new TypeReference<AgentHeader>() {
                }
        );

        return new Function("getAgentConfig", inputParameters, outputParameters);
    }

}
