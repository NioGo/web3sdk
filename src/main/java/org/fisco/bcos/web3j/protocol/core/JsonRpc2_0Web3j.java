package org.fisco.bcos.web3j.protocol.core;

import io.reactivex.Flowable;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.Web3jService;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.request.ProofMerkle;
import org.fisco.bcos.web3j.protocol.core.methods.request.ShhFilter;
import org.fisco.bcos.web3j.protocol.core.methods.response.*;
import org.fisco.bcos.web3j.protocol.rx.JsonRpc2_0Rx;
import org.fisco.bcos.web3j.protocol.websocket.events.LogNotification;
import org.fisco.bcos.web3j.protocol.websocket.events.NewHeadsNotification;
import org.fisco.bcos.web3j.utils.Async;
import org.fisco.bcos.web3j.utils.BlockLimit;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//增加eth_pbftView接口

/**
 * JSON-RPC 2.0 factory implementation.
 */
public class JsonRpc2_0Web3j implements Web3j {
    static Logger logger = LoggerFactory.getLogger(JsonRpc2_0Web3j.class);
    protected static final long ID = 1;
    public static final int BLOCK_TIME = 15 * 100;
    public static final int DEFAULT_BLOCK_TIME = 15 * 1000;
    protected final Web3jService web3jService;
    private final JsonRpc2_0Rx web3jRx;
    private final long blockTime;
    private final ScheduledExecutorService scheduledExecutorService;
    private  int groupId = 1;
    
    public Web3jService web3jService() {
    	return web3jService;
    }

     public BigInteger getBlockNumber() {
        return ((ChannelEthereumService)web3jService).getChannelService().getNumber();
    }

    synchronized public void setBlockNumber(BigInteger blockNumber) {
        if(blockNumber.compareTo(((ChannelEthereumService)web3jService).getChannelService().getNumber()) > 0) {
        	((ChannelEthereumService)web3jService).getChannelService().setNumber(blockNumber);
        }
    }

    public JsonRpc2_0Web3j(Web3jService web3jService) {
        this(web3jService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService(),1);
    }

    public JsonRpc2_0Web3j(Web3jService web3jService, int groupId) {
        this(web3jService, DEFAULT_BLOCK_TIME, Async.defaultExecutorService(), groupId);
        this.groupId = groupId;
    }

    public JsonRpc2_0Web3j(
            Web3jService web3jService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService, int groupId) {
        this.web3jService = web3jService;
        this.web3jRx = new JsonRpc2_0Rx(this, scheduledExecutorService);
        this.blockTime = pollingInterval;
        this.scheduledExecutorService = scheduledExecutorService;
        this.groupId= groupId;

    }

    @Override
    public BigInteger getBlockNumberCache() {
        if (getBlockNumber().intValue() == 1)
        {
            try {
                EthBlockNumber ethBlockNumber = ethBlockNumber().sendAsync().get();
                setBlockNumber(ethBlockNumber.getBlockNumber());
            } catch (Exception e) {
                logger.error("Exception: " + e);
            }
        }
        return getBlockNumber().add(new BigInteger(BlockLimit.blockLimit.toString()));
    }

    @Override
    public Request<?, Web3ClientVersion> web3ClientVersion() {
        return new Request<>(
                "getClientVersion",
                Arrays.asList(groupId),
                web3jService,
                Web3ClientVersion.class);
    }

    @Override
    public Request<?, GroupList> ethGroupList() {
    	return new Request<>(
    			"getGroupList",
    			Arrays.asList(groupId),
    			web3jService,
    			GroupList.class);
    }
    
    @Override
    public Request<?, MinerList> getMinerList() {
    	return new Request<>(
    			"getMinerList",
    			Arrays.asList(groupId),
    			web3jService,
    			MinerList.class);
    }
    
    @Override
    public Request<?, ObserverList> getObserverList() {
        return new Request<>(
                "getObserverList",
               Arrays.asList(groupId),
                web3jService,
                ObserverList.class);
    }

    @Override
    public Request<?, EthPeers> ethPeersInfo() {
        return new Request<>(
                "getPeers",
               Arrays.asList(groupId),
                web3jService,
                EthPeers.class);
    }
    
    @Override
    public Request<?, NodeIDList> getNodeIDList() {
        return new Request<>(
                "getNodeIDList",
               Arrays.asList(groupId),
                web3jService,
                NodeIDList.class);
    }
    
    @Override
    public Request<?, SystemConfig> getSystemConfigByKey(String key) {
    	return new Request<>(
    			"getSystemConfigByKey",
    			Arrays.asList(groupId, key),
    			web3jService,
    			SystemConfig.class);
    }

    @Override
    public Request<?, EthSyncing> ethSyncing() {
        return new Request<>(
                "getSyncStatus",
               Arrays.asList(groupId),
                web3jService,
                EthSyncing.class);
    }
    
    @Override
    public Request<?, EthBlockNumber> ethBlockNumber() {
        return new Request<>(
                "getBlockNumber",
               Arrays.asList(groupId),
                web3jService,
                EthBlockNumber.class);
    }

    //增加pbftView接口
    @Override
    public Request<?, EthPbftView> ethPbftView() {
        return new Request<>(
                "getPbftView",
               Arrays.asList(groupId),
                web3jService,
                EthPbftView.class);
    }

    @Override
    public Request<?, EthConsensusStatus> consensusStatus() {
        return new Request<>(
                "getConsensusStatus",
               Arrays.asList(groupId),
                (ChannelEthereumService)web3jService,
                EthConsensusStatus.class);
    }

    @Override
    public Request<?, EthGetCode> ethGetCode(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "getCode",
                Arrays.asList(groupId, address),
                web3jService,
                EthGetCode.class);
    }
    
    @Override
    public Request<?, TotalTransactionCount> getTotalTransactionCount() {
    	return new Request<>(
    			"getTotalTransactionCount",
    			Arrays.asList(groupId),
    			web3jService,
    			TotalTransactionCount.class);
    }

    @Override
    public Request<?, EthCall> ethCall(
            org.fisco.bcos.web3j.protocol.core.methods.request.Transaction transaction, DefaultBlockParameter defaultBlockParameter) {
        return new Request<>(
                "call",
                Arrays.asList(groupId,transaction),
                web3jService,
                EthCall.class);
    }
    
    @Override
    public Request<?, EthGasPrice> ethGasPrice() {
        return new Request<>(
                "gasPrice",
               Arrays.asList(groupId),
                web3jService,
                EthGasPrice.class);
    }

    @Override
    public Request<?, EthBlock> ethGetBlockByHash(
            String blockHash, boolean returnFullTransactionObjects) {
        return new Request<>(
                "getBlockByHash",
                Arrays.asList(groupId,
                        blockHash,
                        returnFullTransactionObjects),
                web3jService,
                EthBlock.class);
    }

    @Override
    public Request<?, EthBlock> ethGetBlockByNumber(
            DefaultBlockParameter defaultBlockParameter,
            boolean returnFullTransactionObjects) {
        return new Request<>(
                "getBlockByNumber",
                Arrays.asList(groupId,
                        defaultBlockParameter.getValue(),
                        returnFullTransactionObjects),
                web3jService,
                EthBlock.class);
    }
    
    @Override
    public Request<?, BlockHash> getBlockHashByNumber(
    		DefaultBlockParameter defaultBlockParameter) {
    	return new Request<>(
    			"getBlockHashByNumber",
    			Arrays.asList(groupId,
    					defaultBlockParameter.getValue()),
    			web3jService,
    			BlockHash.class);
    }

    @Override
    public Request<?, EthTransaction> ethGetTransactionByHash(String transactionHash) {
        return new Request<>(
                "getTransactionByHash",
                Arrays.asList(groupId,transactionHash),
                web3jService,
                EthTransaction.class);
    }

    @Override
    public Request<?, EthTransaction> ethGetTransactionByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex) {
        return new Request<>(
                "getTransactionByBlockHashAndIndex",
                Arrays.asList(groupId,
                        blockHash,
                        Numeric.encodeQuantity(transactionIndex)),
                web3jService,
                EthTransaction.class);
    }

    @Override
    public Request<?, EthTransaction> ethGetTransactionByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex) {
        return new Request<>(
                "getTransactionByBlockNumberAndIndex",
                Arrays.asList(groupId,
                        defaultBlockParameter.getValue(),
                        Numeric.encodeQuantity(transactionIndex)),
                web3jService,
                EthTransaction.class);
    }

    @Override
    public Request<?, EthGetTransactionReceipt> ethGetTransactionReceipt(String transactionHash) {
        return new Request<>(
                "getTransactionReceipt",
                Arrays.asList(groupId,transactionHash),
                web3jService,
                EthGetTransactionReceipt.class);
    }

    @Override
    public Request<?, EthPendingTransactions> ethPendingTransaction() {
        return new Request<>(
                "getPendingTransactions",
               Arrays.asList(groupId),
                web3jService,
                EthPendingTransactions.class);
    }
    
    @Override
    public Request<?, PendingTxSize> getPendingTxSize() {
    	return new Request<>(
    			"getPendingTxSize",
    			Arrays.asList(groupId),
    			web3jService,
    			PendingTxSize.class);
    }
    
    @Override
    public Request<?, EthSendTransaction>
    ethSendRawTransaction(
            String signedTransactionData) {
        return new Request<>(
                "sendRawTransaction",
                Arrays.asList(groupId,signedTransactionData),
                web3jService,
                EthSendTransaction.class);
    }

    @Override
    public Request<?, EthPeerList> ethGroupPeers() {
        return new Request<>(
                "getGroupPeers",
               Arrays.asList(groupId),
                web3jService,
                EthPeerList.class);
    }
    

    @Override
    public Request<?, EthFilter> ethNewPendingTransactionFilter() {
        return new Request<>(
                "newPendingTransactionFilter",
               Arrays.asList(groupId),
                web3jService,
                EthFilter.class);
    }
    
    @Override
    public Request<?, EthFilter> ethNewBlockFilter() {
        return new Request<>(
                "newBlockFilter",
               Arrays.asList(groupId),
                web3jService,
                EthFilter.class);
    }
    
    @Override
    public Request<?, EthLog> ethGetFilterChanges(BigInteger filterId) {
        return new Request<>(
                "getFilterChanges",
                Arrays.asList(groupId,Numeric.toHexStringWithPrefixSafe(filterId)),
                web3jService,
                EthLog.class);
    }
    
    @Override
    public Request<?, EthUninstallFilter> ethUninstallFilter(BigInteger filterId) {
        return new Request<>(
                "uninstallFilter",
                Arrays.asList(groupId,Numeric.toHexStringWithPrefixSafe(filterId)),
                web3jService,
                EthUninstallFilter.class);
    }
    
    @Override
    public Request<?, EthFilter> ethNewFilter(
            org.fisco.bcos.web3j.protocol.core.methods.request.EthFilter ethFilter) {
        return new Request<>(
                "newFilter",
                Arrays.asList(groupId,ethFilter),
                web3jService,
                EthFilter.class);
    }
    
    @Override
    public Flowable<NewHeadsNotification> newHeadsNotifications() {
        return web3jService.subscribe(
                new Request<>(
                        "subscribe",
                        Collections.singletonList("newHeads"),
                        web3jService,
                        EthSubscribe.class),
                "unsubscribe",
                NewHeadsNotification.class
        );
    }

    @Override
    public Flowable<LogNotification> logsNotifications(
            List<String> addresses, List<String> topics) {

        Map<String, Object> params = createLogsParams(addresses, topics);

        return web3jService.subscribe(
                new Request<>(
                        "subscribe",
                        Arrays.asList(groupId,"logs", params),
                        web3jService,
                        EthSubscribe.class),
                "unsubscribe",
                LogNotification.class
        );
    }

    private Map<String, Object> createLogsParams(List<String> addresses, List<String> topics) {
        Map<String, Object> params = new HashMap<>();
        if (!addresses.isEmpty()) {
            params.put("address", addresses);
        }
        if (!topics.isEmpty()) {
            params.put("topics", topics);
        }
        return params;
    }

    @Override
    public Flowable<String> ethBlockHashFlowable() {
        return web3jRx.ethBlockHashFlowable(blockTime);
    }

    @Override
    public Flowable<String> ethPendingTransactionHashFlowable() {
        return web3jRx.ethPendingTransactionHashFlowable(blockTime);
    }

    @Override
    public Flowable<Log> ethLogFlowable(
            org.fisco.bcos.web3j.protocol.core.methods.request.EthFilter ethFilter) {
        return web3jRx.ethLogFlowable(ethFilter, blockTime);
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
    transactionFlowable() {
        return web3jRx.transactionFlowable(blockTime);
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
    pendingTransactionFlowable() {
        return web3jRx.pendingTransactionFlowable(blockTime);
    }

    @Override
    public Flowable<EthBlock> blockFlowable(boolean fullTransactionObjects) {
        return web3jRx.blockFlowable(fullTransactionObjects, blockTime);
    }

    @Override
    public Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return web3jRx.replayBlocksFlowable(startBlock, endBlock, fullTransactionObjects);
    }

    @Override
    public Flowable<EthBlock> replayPastBlocksFlowable(DefaultBlockParameter startBlock,
                                                       DefaultBlockParameter endBlock,
                                                       boolean fullTransactionObjects,
                                                       boolean ascending) {
        return web3jRx.replayBlocksFlowable(startBlock, endBlock,
                fullTransactionObjects, ascending);
    }

    @Override
    public Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects,
            Flowable<EthBlock> onCompleteFlowable) {
        return web3jRx.replayPastBlocksFlowable(
                startBlock, fullTransactionObjects, onCompleteFlowable);
    }

    @Override
    public Flowable<EthBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return web3jRx.replayPastBlocksFlowable(startBlock, fullTransactionObjects);
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
    replayPastTransactionsFlowable(DefaultBlockParameter startBlock,
                                   DefaultBlockParameter endBlock) {
        return web3jRx.replayTransactionsFlowable(startBlock, endBlock);
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
    replayPastTransactionsFlowable(DefaultBlockParameter startBlock) {
        return web3jRx.replayPastTransactionsFlowable(startBlock);
    }

    @Override
    public Flowable<EthBlock> replayPastAndFutureBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return web3jRx.replayPastAndFutureBlocksFlowable(
                startBlock, fullTransactionObjects, blockTime);
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
    replayPastAndFutureTransactionsFlowable(DefaultBlockParameter startBlock) {
        return web3jRx.replayPastAndFutureTransactionsFlowable(
                startBlock, blockTime);
    }


    public void shutdown() {
        scheduledExecutorService.shutdown();
        try {
            web3jService.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close web3j service", e);
        }
    }
}
