package zkclient.master;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wodezuiaishinageren on 2017/9/20.
 * master选举调度器模拟
 */
public class LeaderSelectorZkclent {
    /**10个客户端**/
    private static final int CLIENT_QTY=10;
    private static final String ZOOKEEPER_SERVER="192.168.109.131:2181";
    public static void main(String [] args){
        try {
            //radomMaster();
            firstMaster();
            System.out.println("敲回车键退出! \n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * 优先选举第一人master
     */
    public static void firstMaster(){
        try {
            /**所有客户端列表**/
            List<ZkClient> zkClients = new ArrayList<>();
            /***所有的服务列表**/
            List<WorkServer> workServers = new ArrayList<>();
            for (int i = 0; i < CLIENT_QTY; i++) {
                ZkClient zkClient = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new SerializableSerializer());
                zkClients.add(zkClient);
                RunningData runningData = new RunningData();
                runningData.setCid(Long.valueOf(i));
                runningData.setName("ClientName :" + i);
                //创建服务
                WorkServer workServer = new WorkServer(runningData);
                workServer.setZkClient(zkClient);
                workServers.add(workServer);
                workServer.start();
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("执行异常："+e.getMessage());
        }
    }

    /**
     * 随机选举节点作为master
     */
    public static void radomMaster(){
        try {
            /**所有客户端列表**/
            List<ZkClient> zkClients = new ArrayList<>();
            /***所有的服务列表**/
            List<WorkServerA> workServers = new ArrayList<>();
            for (int i = 0; i < CLIENT_QTY; i++) {
                ZkClient zkClient = new ZkClient(ZOOKEEPER_SERVER, 4000, 5000, new SerializableSerializer());
                zkClients.add(zkClient);
                RunningData runningData = new RunningData();
                runningData.setCid(Long.valueOf(i));
                runningData.setName("ClientName :" + i);
                //创建服务
                WorkServerA workServera = new WorkServerA(runningData);
                workServera.setZkClient(zkClient);
                workServers.add(workServera);
                workServera.start();
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("执行异常："+e.getMessage());
        }
    }
}
