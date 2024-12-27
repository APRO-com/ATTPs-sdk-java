package org.util;

import cn.hutool.core.util.HexUtil;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.util.*;

public class ProofsStruct extends DynamicStruct {

    private byte[] zkProof;

    private byte[] merkleProof;

    private byte[] signatureProof;

    private static final List outputParameters = new ArrayList<TypeReference<Type>>();

    static {
        outputParameters.addAll(
                Arrays.asList(
                        new TypeReference<DynamicBytes>() {},
                        new TypeReference<DynamicBytes>() {},
                        new TypeReference<DynamicBytes>() {}));
    }

    public ProofsStruct(
            byte[] zkProof,
            byte[] merkleProof,
            byte[] signatureProof) {
        super(
                new DynamicBytes(zkProof),
                new DynamicBytes(merkleProof),
                new DynamicBytes(signatureProof));
        this.zkProof = zkProof;
        this.merkleProof = merkleProof;
        this.signatureProof = signatureProof;
    }

    public ProofsStruct(
            DynamicBytes zkProof,
            DynamicBytes merkleProof,
            DynamicBytes signatureProof) {
        super(zkProof, merkleProof, signatureProof);
        this.zkProof = zkProof.getValue();
        this.merkleProof = merkleProof.getValue();
        this.signatureProof = signatureProof.getValue();
    }

    public static ProofsStruct build(byte[] bytes) {
        List<Type> resultList =
                FunctionReturnDecoder.decode(Numeric.toHexString(bytes), outputParameters);

        return new ProofsStruct(
                (DynamicBytes) resultList.get(0),
                (DynamicBytes) resultList.get(1),
                (DynamicBytes) resultList.get(2));
    }


    public byte[] getZkProof() {
        return zkProof;
    }

    public void setZkProof(byte[] zkProof) {
        this.zkProof = zkProof;
    }

    public byte[] getMerkleProof() {
        return merkleProof;
    }

    public void setMerkleProof(byte[] merkleProof) {
        this.merkleProof = merkleProof;
    }

    public byte[] getSignatureProof() {
        return signatureProof;
    }

    public void setSignatureProof(byte[] signatureProof) {
        this.signatureProof = signatureProof;
    }

    public String toJsonString(){
        Map<String, String> map = new HashMap<>();
        map.put("zkProof", HexUtil.encodeHexStr(zkProof));
        map.put("merkleProof", HexUtil.encodeHexStr(merkleProof));
        map.put("signatureProof", HexUtil.encodeHexStr(signatureProof));
        return GsonUtil.toJson(map);
    }

    public static ProofsStruct fromJsonString(String json) {
        Map<String, String> map = GsonUtil.fromJson(json, HashMap.class);
        byte[] zkProof = HexUtil.decodeHex(map.get("zkProof"));
        byte[] merkleProof = HexUtil.decodeHex(map.get("merkleProof"));
        byte[] signatureProof = HexUtil.decodeHex(map.get("signatureProof"));
        ProofsStruct proofsStruct = new ProofsStruct(zkProof, merkleProof, signatureProof);
        return proofsStruct;
    }

}
