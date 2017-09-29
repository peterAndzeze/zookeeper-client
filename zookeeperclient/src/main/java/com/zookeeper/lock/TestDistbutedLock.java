package com.zookeeper.lock;

import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

/**
 * Created by wodezuiaishinageren on 2017/9/29.
 */
public class TestDistbutedLock {



    public static void main(String  [] args) throws Exception {
        final ZkclientExt zkclientExt=new ZkclientExt("192.168.109.131:2181",5000,5000,new BytesPushThroughSerializer());
        final SimpleDistributedLockMutex mutex=new SimpleDistributedLockMutex(zkclientExt,"/mutext");
        final ZkclientExt zkclientExt1=new ZkclientExt("192.168.109.131:2181",5000,5000,new BytesPushThroughSerializer());
        final  SimpleDistributedLockMutex mutex1=new SimpleDistributedLockMutex(zkclientExt1,"/mutext");
        mutex.acquire();
        System.out.println("zkclientExt locked");
        Thread client2Thd = new Thread(new Runnable() {

            public void run() {
                try {
                    mutex1.acquire();
                    System.out.println("zkclientExt1 locked");
                    mutex1.release();
                    System.out.println("zkclientExt1 released lock");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        client2Thd.start();
        Thread.sleep(5000);
        mutex.release();
        System.out.println("Client released lock");

        client2Thd.join();
    }

}
