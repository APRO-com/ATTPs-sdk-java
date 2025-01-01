package org.apro.sdk.params;

import lombok.Getter;
import lombok.Setter;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.utils.Numeric;

import java.util.*;


public class MetaDataStruct extends DynamicStruct {

    @Getter
    @Setter
    private String contentType;

    @Getter
    @Setter
    private String encoding;

    @Getter
    @Setter
    private String compression;

    private static final List outputParameters = new ArrayList<TypeReference<Type>>();

    static {
        outputParameters.addAll(
                Arrays.asList(
                        new TypeReference<Utf8String>() {
                        },
                        new TypeReference<Utf8String>() {
                        },
                        new TypeReference<Utf8String>() {
                        }));
    }

    public MetaDataStruct(
            String contentType,
            String encoding,
            String compression) {
        super(
                new Utf8String(contentType),
                new Utf8String(encoding),
                new Utf8String(compression));
        this.contentType = contentType;
        this.encoding = encoding;
        this.compression = compression;
    }

    public MetaDataStruct(
            Utf8String contentType,
            Utf8String encoding,
            Utf8String signatureProof) {
        super(contentType, encoding, signatureProof);
        this.contentType = contentType.getValue();
        this.encoding = encoding.getValue();
        this.compression = signatureProof.getValue();
    }

    public static MetaDataStruct build(byte[] bytes) {
        List<Type> resultList =
                FunctionReturnDecoder.decode(Numeric.toHexString(bytes), outputParameters);

        return new MetaDataStruct(
                (Utf8String) resultList.get(0),
                (Utf8String) resultList.get(1),
                (Utf8String) resultList.get(2));
    }
}

