package com.zookeeper.lock;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by wodezuiaishinageren on 2017/9/29.
 * 简化测试类
 */
public class SimpleDistributedLockMutex extends  BaseDistributedLock implements  DistributedLock{
    private static final String LOCK_NAME="lock-";
    private  final String basePath;
    /***当前持有节点**/
    private String ourLockPath;

    public SimpleDistributedLockMutex(ZkclientExt zkclient, String basePath) {
        super(zkclient,basePath,LOCK_NAME);

        this.basePath=basePath;
    }
    private boolean internalLock(long time, TimeUnit unit) throws Exception{
        checkBasePath();
        ourLockPath = attemptLock(time, unit);
        return ourLockPath != null;

    }

    public void acquire() throws Exception {
        if ( !internalLock(-1, null) ) {
            throw new IOException("连接丢失!在路径:'"+basePath+"'下不能获取锁!");
        }
    }

    @Override
    public boolean acquire(Long time, TimeUnit unit) throws Exception {
        return internalLock(time, unit);
    }

    public void release() throws Exception {

        releaseLock(ourLockPath);
    }
}
