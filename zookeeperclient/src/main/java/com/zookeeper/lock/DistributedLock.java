package com.zookeeper.lock;

import java.util.concurrent.TimeUnit;

/**
 * Created by wodezuiaishinageren on 2017/9/21.
 * 分布式锁接口
 */
public interface DistributedLock {
    /**
     * 获取锁 如果没有就等待
     * @throws Exception
     */
    public void acquire() throws  Exception;

    /**
     * 获取锁直到超时
     * @param time
     * @param timeUnit
     * @return
     * @throws Exception
     */
    public boolean acquire(Long time, TimeUnit timeUnit) throws  Exception;

    /**
     * 释放锁
     * @throws Exception
     */
    public void release() throws Exception;
}
