package com.shinho.framewok.job;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleJobListener implements ElasticJobListener {

    private Logger log = LoggerFactory.getLogger(SimpleJobListener.class);

    private long start = 0;

    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
        start = System.currentTimeMillis();
        log.info("simple-job:{} --start", shardingContexts.getJobName());
        log.info("分片总数:{} 分片项:{}", shardingContexts.getShardingTotalCount(), shardingContexts.getShardingItemParameters());
        log.info("自定义参数:{}", shardingContexts.getJobParameter());
    }

    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
        log.info("simple-job:{} --end", shardingContexts.getJobName());
        log.info("耗时:{}秒 \n\r", ((System.currentTimeMillis() - start) / 1000));
    }
}
