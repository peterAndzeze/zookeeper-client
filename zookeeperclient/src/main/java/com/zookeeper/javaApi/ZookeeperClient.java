package com.zookeeper.javaApi;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

import static org.apache.zookeeper.CreateMode.EPHEMERAL;

/**
 * Created by wodezuiaishinageren on 2017/9/19.
 */
public class ZookeeperClient{
    private ZooKeeper zooKeeper;
    /***ip*/
    private static final String host="192.168.109.131:2181";
    /***超时时间***/
    private static final int SESSION_TIME=5000;

    /**
     * 创建zookeeper客户端链接
     */
    public void connectZookeeper( ){
        try {
            zooKeeper=new ZooKeeper(host, SESSION_TIME, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    System.out.println("当前事件信息："+watchedEvent.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建节点
     * @param path 父节点路径
     * @param data 数据信息
     */
    public void createNode(String path,String data){
        try {
            boolean flag=checkNodeExists(path);
            if(flag) {//acl 不能为空 acl 是节点权限
                String nodePath = zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, EPHEMERAL);
                System.out.println("创建的节点" + nodePath);
            }else {
                System.out.println(path + "---》已经被创建");
            }
        }catch (KeeperException.NodeExistsException e){
            System.out.println("节点已经存在："+e.getMessage());
        }catch (KeeperException e) {
            System.out.println("创建临时节点异常："+e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查节点是否存在
     * @param path
     * @return
     */
    public boolean checkNodeExists(String path){
        try {
            Stat stat= zooKeeper.exists(path,true);
            if(null==stat){
                return true;
            }else{
                System.out.println("已存在节点信息："+stat.toString());
            }
            return false;
        } catch (KeeperException e) {
            System.out.println("验证节点是否存在异常："+e.getMessage());
            e.printStackTrace();
            return  false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取节点信息
     * @param path
     */
    public String getNodeInfo(String path){
        String str="";
        try {
            byte[] bytes=zooKeeper.getData(path,true,null);
            str=new String(bytes);
        } catch (KeeperException e) {
            System.out.println("获取节点信息异常");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 获取子节点
     * @param path
     */
    public List<String> getChilds(String path){
        try {
            return zooKeeper.getChildren(path,true);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
