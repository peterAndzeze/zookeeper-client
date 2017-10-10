package com.zookeeper.queue;

import java.util.Optional;

/**
 * Created by wodezuiaishinageren on 2017/10/10.
 */
public class ReceiveDataThread extends  Thread {
    private SubscribeSimpleQueue<User> subscribeSimpleQueue;

    public ReceiveDataThread(SubscribeSimpleQueue<User> simpleQueue) {
        this.subscribeSimpleQueue=simpleQueue;
    }

    @Override
    public void run(){
        User user=(User) subscribeSimpleQueue.poll();
        if (Optional.ofNullable(user).isPresent()){
            System.out.println("当前线程："+Thread.currentThread().getName()+"获取数据："+user.getName());
        }else{
            System.out.println("当前线程："+Thread.currentThread().getName()+"没有获取到数据");
        }
    }

}
