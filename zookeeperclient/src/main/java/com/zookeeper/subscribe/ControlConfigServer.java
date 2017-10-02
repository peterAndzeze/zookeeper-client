package com.zookeeper.subscribe;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

/**
 * Created by wodezuiaishinageren on 2017/10/1.
 * 监听配置文件
 */
public class ControlConfigServer {
    private static final String  ZOOKEEPER_SERVER = "192.168.109.130:2181";
    private static final String  COMMAND_PATH = "/command";
    private static final String  CONFIG_PATH = "/config";


    public static  void main(String [] args){

        ZkClient clientManage = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new BytesPushThroughSerializer());

        changeCommonPathCmd(clientManage);
    }
    public static void changeCommonPathCmd(ZkClient clientManage){
       /*try {
            clientManage.writeData(COMMAND_PATH, "list".getBytes());
        }catch (ZkNoNodeException e) {
           clientManage.createPersistent(COMMAND_PATH, true);
       }*/
//        System.out.println("--------------华丽的分割线---------------");
//        try {
            //clientManage.writeData(COMMAND_PATH, "create".getBytes());
//        }catch (ZkNoNodeException e){
//            clientManage.createPersistent(COMMAND_PATH,true);
//        }



        clientManage.writeData(COMMAND_PATH, "modify".getBytes());

    }



}
