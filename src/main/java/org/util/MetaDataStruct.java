package org.util;

import cn.hutool.core.util.HexUtil;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.util.*;

public class MetaDataStruct extends DynamicStruct {

    private byte[] contentType;

    private byte[] encoding;

    private byte[] compression;

    private static final List outputParameters = new ArrayList<TypeReference<Type>>();

    static {
        outputParameters.addAll(
                Arrays.asList(
                        new TypeReference<DynamicBytes>() {
                        },
                        new TypeReference<DynamicBytes>() {
                        },
                        new TypeReference<DynamicBytes>() {
                        }));
    }

    public MetaDataStruct(
            byte[] contentType,
            byte[] encoding,
            byte[] compression) {
        super(
                new DynamicBytes(contentType),
                new DynamicBytes(encoding),
                new DynamicBytes(compression));
        this.contentType = contentType;
        this.encoding = encoding;
        this.compression = compression;
    }

    public MetaDataStruct(
            DynamicBytes contentType,
            DynamicBytes encoding,
            DynamicBytes signatureProof) {
        super(contentType, encoding, signatureProof);
        this.contentType = contentType.getValue();
        this.encoding = encoding.getValue();
        this.compression = signatureProof.getValue();
    }

    public static MetaDataStruct build(byte[] bytes) {
        List<Type> resultList =
                FunctionReturnDecoder.decode(Numeric.toHexString(bytes), outputParameters);

        return new MetaDataStruct(
                (DynamicBytes) resultList.get(0),
                (DynamicBytes) resultList.get(1),
                (DynamicBytes) resultList.get(2));
    }


    public byte[] getContentType() {
        return contentType;
    }

    public void setContentType(byte[] contentType) {
        this.contentType = contentType;
    }

    public byte[] getEncoding() {
        return encoding;
    }

    public void setEncoding(byte[] encoding) {
        this.encoding = encoding;
    }

    public byte[] getCompression() {
        return compression;
    }

    public void setCompression(byte[] compression) {
        this.compression = compression;
    }

    public String toJsonString() {
        Map<String, String> map = new HashMap<>();
        map.put("contentType", HexUtil.encodeHexStr(contentType));
        map.put("encoding", HexUtil.encodeHexStr(encoding));
        map.put("compression", HexUtil.encodeHexStr(compression));
        return GsonUtil.toJson(map);
    }

    public static MetaDataStruct fromJsonString(String json) {
        Map<String, String> map = GsonUtil.fromJson(json, HashMap.class);
        byte[] contentType = HexUtil.decodeHex(map.get("contentType"));
        byte[] encoding = HexUtil.decodeHex(map.get("encoding"));
        byte[] compression = HexUtil.decodeHex(map.get("compression"));
        MetaDataStruct metaDataStruct = new MetaDataStruct(contentType, encoding, compression);
        return metaDataStruct;
    }
}
