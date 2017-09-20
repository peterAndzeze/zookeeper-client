package com.zookeeper.javaApi;

import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Created by wodezuiaishinageren on 2017/9/19.
 */
public class ZookeeperClient implements Watcher{
    private ZooKeeper zooKeeper;
    /***ip*/
    private static final String host="192.168.109.131";
    /***端口***/
    private static final String port="2181";
    /***超时时间***/
    private static final int SESSION_TIME=5000;

    /**
     * 创建zookeeper客户端链接
     * @param watcher
     */
    public void connectZookeeper(Watcher watcher ){
        String connection=host+":"+port;
        try {
            zooKeeper=new ZooKeeper(connection,SESSION_TIME,watcher);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建节点
     * @param parentPath 父节点路径
     * @param data 数据信息
     */
    public void createNode(String parentPath,String data){
        try {
            zooKeeper.create(parentPath,data.getBytes(),null,CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            System.out.println("创建子节点应昌");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("当前事件："+watchedEvent.toString());
    }
}
