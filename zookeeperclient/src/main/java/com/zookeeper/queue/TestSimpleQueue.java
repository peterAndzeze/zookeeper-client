package com.zookeeper.queue;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wodezuiaishinageren on 2017/10/10.
 * 简单对了测试
 */
public class TestSimpleQueue {

    public static void main(String [] args){
        ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
        ZkClient zkClient=new ZkClient("192.168.109.130:2181",5000,5000,new SerializableSerializer());
        SubscribeSimpleQueue<User> simpleQueue=new SubscribeSimpleQueue<>(zkClient,"/Queue");
        User u=new User("张三",21);
        User u1=new User("里斯",30);

        //for (int i=0;i<5;i++){
           //new ReceiveDataThread(simpleQueue).start();

        //}
        int delyTime=5;
        try {
            scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    simpleQueue.offer(u);
                    simpleQueue.offer(u1);
                }
            },delyTime, TimeUnit.SECONDS);
            System.out.println("ready poll!");
            User user = (User) simpleQueue.poll();
            User user1 = (User) simpleQueue.poll();
            System.out.println(user.getName()+"--->"+user1.getName());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            scheduledExecutorService.shutdown();
            try {
                scheduledExecutorService.awaitTermination(100,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
