package com.zookeeper.subscribe;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wodezuiaishinageren on 2017/9/30.
 *
 * 发布订阅客户端
 *
 */
public class SubcribeZkclient {

    private static final int  CLIENT_QTY = 5;

    private static final String  ZOOKEEPER_SERVER = "192.168.109.130:2181";

    private static final String  CONFIG_PATH = "/config";
    private static final String  COMMAND_PATH = "/command";
    private static final String  SERVERS_PATH = "/servers";
    /**外部测试***/
    public static List<WorkerServer>  workServers = new ArrayList<>();

    public static void main(String[] args) throws Exception
    {

        List<ZkClient> clients = new ArrayList<ZkClient>();
        ManagerServer manageServer = null;

        try
        {
            DbConfig initConfig = new DbConfig();
            initConfig.setPwd("123456");
            initConfig.setUrl("jdbc:mysql://localhost:3306/mydb");
            initConfig.setUser("root");

            ZkClient clientManage = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new BytesPushThroughSerializer());
            manageServer = new ManagerServer(CONFIG_PATH,COMMAND_PATH,SERVERS_PATH,clientManage,initConfig);
            manageServer.start();

            for ( int i = 0; i < CLIENT_QTY; ++i )
            {
                ZkClient client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new BytesPushThroughSerializer());
                clients.add(client);
                ServerData serverData = new ServerData();
                serverData.setId(i);
                serverData.setName("WorkServer#"+i);
                serverData.setAddress("192.168.1."+i);

                WorkerServer  workServer = new WorkerServer(client,CONFIG_PATH, SERVERS_PATH, serverData);
                workServers.add(workServer);
                System.out.println(workServers.size());
                workServer.start();

            }
            System.out.println("敲回车键退出！\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

        }
        finally
        {
            System.out.println("Shutting down...");

            for ( WorkerServer workServer : workServers )
            {
                try {
                    workServer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for ( ZkClient client : clients )
            {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
