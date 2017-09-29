package com.zookeeper.lock;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by wodezuiaishinageren on 2017/9/29.
 * 分布式事务锁
 */

public class DistributedLockMutex extends  BaseDistributedLock implements  DistributedLock {
    //锁名称前缀
    private static final String LOCK_PREFIX = "lock-";
    //进程中的线程信息
    private final ConcurrentMap<Thread, LockData> threadData = new ConcurrentHashMap<Thread, LockData>();
    private ZkclientExt zkclien;
    /***根目录**/
    private String basePath;
    /****持有锁名称***/
    private String lockName;
    /****最大重试次数**/
    private  final  int MAX_RETRY_COUNT=10;
    public DistributedLockMutex(ZkclientExt zkclient,  String basePath) {
        super(zkclient, basePath,LOCK_PREFIX);
    }

    /**
     * 内部锁
     * @param time
     * @param timeUnit
     * @return
     */
    public boolean internalLock(Long time,TimeUnit timeUnit){
        Thread thread=Thread.currentThread();//得到当前线程
        LockData lockData=threadData.get(thread);//如果得到锁信息
        if(null!=lockData) {
            lockData.lockCount.incrementAndGet();//线程数加1
            return true;
        }
        String lockPath=attemptLock(time,timeUnit);
        Optional optional=Optional.ofNullable(lockPath);
        if(optional.isPresent()){
            LockData lockData1=new LockData(thread,lockPath);
            threadData.put(thread,lockData1);
        }
        return false;
    }

    /**
     * 测试链接
     * @throws Exception
     */
    @Override
    public void acquire() throws Exception {
        if(!internalLock(-1L,null)){
            throw new IOException("连接丢失!在路径:'"+basePath+"'下不能获取锁!");
        }

    }

    /**
     * 持有锁
     * @param time
     * @param timeUnit
     * @return
     * @throws Exception
     */
    @Override
    public boolean acquire(Long time, TimeUnit timeUnit) throws Exception {
        return internalLock(time,timeUnit);
    }

    /**
     * 释放锁
     * @throws Exception
     */
    @Override
    public void release() throws Exception {
        Thread thread=Thread.currentThread();
        LockData lockData=threadData.get(thread);
        if(null==lockData){
            throw new IllegalMonitorStateException("您不是："+basePath+"的拥有者，无法获取锁");
        }
        int newLockCount=lockData.lockCount.decrementAndGet();
        if(newLockCount>0){
            return;
        }
        if (newLockCount<0){
            throw new IllegalMonitorStateException("锁计数器："+basePath+"已经为负数");
        }
        try {
            releaseLock(lockData.lockPath);
        }finally {
            threadData.remove(thread);
        }


    }



}
