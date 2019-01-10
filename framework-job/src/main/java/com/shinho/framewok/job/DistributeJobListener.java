package com.shinho.framewok.job;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributeJobListener extends AbstractDistributeOnceElasticJobListener {

    private Logger log = LoggerFactory.getLogger(DistributeJobListener.class);
    private final long startedTimeoutMilliseconds;

    private final long completedTimeoutMilliseconds;

    public DistributeJobListener(final long startedTimeoutMilliseconds, final long completedTimeoutMilliseconds) {
        super(startedTimeoutMilliseconds, completedTimeoutMilliseconds);
        this.startedTimeoutMilliseconds = startedTimeoutMilliseconds;
        this.completedTimeoutMilliseconds = completedTimeoutMilliseconds;
    }

    @Override
    public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
        log.info("doBeforeJobExecutedAtLastStarted:" + shardingContexts);
    }

    @Override
    public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
        log.info("doAfterJobExecutedAtLastCompleted:" + startedTimeoutMilliseconds + "," + completedTimeoutMilliseconds);
    }
}
