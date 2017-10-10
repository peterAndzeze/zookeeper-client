package com.zookeeper.queue;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Created by wodezuiaishinageren on 2017/10/10.
 */
public class SubscribeQueue<T> extends SubscribeSimpleQueue<T> {

    public SubscribeQueue(ZkClient zkClient, String root) {
        super(zkClient, root);
    }

    /**
     *
     * @return
     */
    public T poll(){
        while (true){
            CountDownLatch countDownLatch=new CountDownLatch(1);
            IZkChildListener zkChildListener=new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> children) throws Exception {
                      countDownLatch.countDown();
                }
            };
            zkClient.subscribeChildChanges(root,zkChildListener);
            try {
                T dataModel=super.poll();
                if(Optional.ofNullable(dataModel).isPresent()){
                    return  dataModel;
                }
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                zkClient.unsubscribeChildChanges(root,zkChildListener);
            }
        }
    }

}
