# APRO ATTPs JAVA SDK

This is the Java version of the APRO ATTPs SDK.

## Requirements

- **Java 17 or above**: Ensure Java 17 or a later version installed.

## Integration

### Gradle

```gradle
implementation group: 'com.apro.attps', name: 'attps-sdk', version: '0.0.9'
```

### Maven

```xml
<dependency>
    <groupId>com.apro.attps</groupId>
    <artifactId>attps-sdk</artifactId>
    <version>0.0.9</version>
</dependency>
```

## Usage

### Register an Agent
```java
String address = "${EOA}"; // the user address
String addressPrikey = "";
String proxyAddress = ""; // the proxy address of the Apro ATTPs agent
AiAgentCli aiAgentCli = new AiAgentCli(BSC_TEST); // specify the RPC server
BigInteger nonce = ChainUtil.getNonce(address, aiAgentCli.getWeb3j());

List<String> signerList = new ArrayList<>();
// add the signers
signerList.add("${SIGNER1_ADDR}");
signerList.add("${SIGNER2_ADDR}");
// .....

AgentSettingsParams agentSettingsParams = AgentSettingsParams.builder()
    .signers(new DynamicArray<>(Address.class, signerList.stream().map(Address::new).toList()))
    .threshold(new Uint8(2))
    // If the payload to be verified is obtained from a third service that the data format needs to transform, 
    // you should set the converterAddress to the specified address
    .converterAddress(Address.DEFAULT)
    .version(new Utf8String("1.0"))
    .messageId(new Utf8String("333833c0-0b15-449c-815e-8040eff67c8d"))
    .sourceAgentId(new Utf8String("2c167873-a6fc-4cee-b505-6c1ae2cd4763"))
    .sourceAgentName(new Utf8String("sdk test"))
    .targetAgentId(new Utf8String("c1dd33c9-8196-4c7d-b035-1baab7966c73"))
    .timestamp(new Uint256(System.currentTimeMillis()/1000))
    .messageType(new Uint8(2))
    .priority(new Uint8(1))
    .ttl(new Uint256(3600))
    .build();

RawTransaction rawTransaction = aiAgentCli.buildRegisterAgentTx(
    nonce,
    BigInteger.valueOf(5000000000L), // gas price
    BigInteger.valueOf(10000000),  // gas limit
    proxyAddress,
    agentSettingsParams
);

byte[] signedTx = ChainUtil.signTx(rawTransaction, aiAgentCli.getConfig().getChainId(), addressPrikey);

EthSendTransaction ethSendTransaction = ChainUtil.broadcast(signedTx, aiAgentCli.getWeb3j());
```

### Verify the Agent Data
```java
String address = "${EOA}"; // the user address
String addressPrikey = "";
String proxyAddress = ""; // the proxy address of the Apro ATTPs agent
String agentAddress = "";  // The agent address that has been successfully accepted
String digest = "";  // The agent setting digest, obtained through the agent accept transaction log
String data = Hex.toHexString("hello world".getBytes());
// The dataHash is calculated by Keccak256 if the converterAddress is 0x0000000000000000000000000000000000000000.
// But if the data is obtained from a third service that the data format needs to transform, 
// you should set the converterAddress when registering the agent,
// The dataHash should be calculated by the converterAddress.converter(data).
byte[] dataHashBytes = Utils.toKeccak256(data);
String dataHash = Hex.toHexString(dataHashBytes);
List<Sign.SignatureData> signatures = new ArrayList<>();
Sign.SignatureData data1 = Sign.signMessage(Utils.toBytes(data), Credentials.create("${SIGNER1_PRIKEY}").getEcKeyPair());
Sign.SignatureData data2 = Sign.signMessage(Utils.toBytes(data), Credentials.create("${SIGNER2_PRIKEY}").getEcKeyPair());
signatures.add(data1);
signatures.add(data2);

AiAgentCli aiAgentCli = new AiAgentCli(BSC_TEST);
BigInteger nonce = ChainUtil.getNonce(address, aiAgentCli.getWeb3j());

VerifyParams verifyParams = VerifyParams.builder()
    .agent(agentAddress)
    .settingsDigest(digest)
    .data(data)
    .dataHash(dataHash)
    .signatures(signatures)
    .build();

RawTransaction rawTransaction = aiAgentCli.buildVerifyTx(
    nonce,
    BigInteger.valueOf(5000000000L),
    BigInteger.valueOf(10000000),
    proxyAddress,
    verifyParams
);

byte[] signedTx = ChainUtil.signTx(rawTransaction, aiAgentCli.getConfig().getChainId(), addressPrikey);

EthSendTransaction ethSendTransaction = ChainUtil.broadcast(signedTx, aiAgentCli.getWeb3j());
```

## Contributing
Thank you for considering helping out with the source code! We welcome contributions from anyone on the internet and are grateful for even the smallest of fixes!

If you'd like to contribute to this project, please fork, fix, commit, and send a pull request for the maintainers to review and merge into the main code base.

## License
This project is licensed under the [GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html).

