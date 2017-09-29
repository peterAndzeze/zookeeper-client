package com.zookeeper.lock;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wodezuiaishinageren on 2017/9/29.
 * 锁数据
 */
public class LockData {
    /***锁路径***/
    final  String lockPath;
    /***锁数量**/
    final  AtomicInteger lockCount=new AtomicInteger(1);

    /**
     * 当前路径对应的线程
     * @param owningThred
     * @param lockPath
     */
    public LockData(Thread owningThred ,String lockPath) {
        this.lockPath=lockPath;
    }
    public static void main(String [] args){
        AtomicInteger atomicInteger=new AtomicInteger(1);
        System.out.println(atomicInteger.incrementAndGet());
    }
}
