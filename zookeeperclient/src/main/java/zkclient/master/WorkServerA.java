package zkclient.master;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wodezuiaishinageren on 2017/9/20.
 * master 节点被zookeeper 删除的情况
 * 1.master节点主动释放
 * 2.master所在服务器宕机
 * 3.网络问题 造成zookeeper检测不到master节点信息  那么服务器会再次发起master节点的争抢
 * 造成不必要的问题出现（比如网络抖动） 上一次master节点优先争抢
 * 服务类
 */
public class WorkServerA {
    /**master运行状态**/
    private volatile  boolean running=false;
    /***当前服务数据信息**/
    private RunningData serverData;
    /**master节点数据信息**/
    private RunningData masterData;
    /***节点数据信息***/
    private IZkDataListener dataListener;
    /**master节点路径****/
    private final static String MASTER_PATH="/master";
    /***zkclient客户端***/
    private ZkClient zkClient;
    /**定时*/
    private ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
    /**延迟时间**/
    private  int delayTime=5;

    /**
     * 工作服务的构造函数
     * @param runningData
     */
    public WorkServerA(RunningData runningData) {

        this.serverData=runningData;
        //实例化dataListener
        this.dataListener=new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                //当节点数据被删除后 进行争抢master操作
                takeMaster();//随机节点
                //判断当前节点是否为master节点
               /* if(masterData.getName().equals(serverData.getName())){//如果是 优先争抢master
                    takeMaster();
                }else{//延迟5s争抢master
                    scheduledExecutorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                takeMaster();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },delayTime, TimeUnit.SECONDS);
                }*/
            }
        };
    }

    /**
     * 服务启动
     * @throws Exception
     */
    public  void start() throws  Exception{
        //当前服务器是否运行
        if(running){
            throw new Exception("server has started...");
        }
        running=true;
        /**订阅master 删除事件**/
        zkClient.subscribeDataChanges(MASTER_PATH,dataListener);
        //争抢master权力
        takeMaster();
    }

    /**
     * 服务停止
     * @throws Exception
     */
    public void stop() throws  Exception{
        if(!running){
            throw new Exception("server has stoped....");
        }
        running=false;
         //取消master节点事件订阅
        zkClient.unsubscribeDataChanges(MASTER_PATH,dataListener);
        releaseMaster();
    }

    /**
     * 争抢master
     * @throws Exception
     */
    public void takeMaster() throws Exception{
        if(!running){
            return;
        }
            //创建master节点 临时节点
        try {
            zkClient.create(MASTER_PATH, serverData, CreateMode.EPHEMERAL);
            masterData=serverData;
            System.out.println(masterData.getName()+"---》选举为master");
            scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {//每隔5s中释放测试用
                    try {
                        if(checkMaster()) {
                            System.out.println(masterData.getName()+"释放 master");
                            releaseMaster();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },delayTime, TimeUnit.SECONDS);
        }catch (ZkNodeExistsException e){//节点已经存在
            //获取master节点中的数据
            RunningData runningMasterData=zkClient.readData(MASTER_PATH,true);
            Optional optional=Optional.ofNullable(runningMasterData);
            if(optional.isPresent()){//存在数据
                masterData=runningMasterData;
            }else{//不存在 再次争抢master
                takeMaster();
            }
        }

    }

    /**
     * 释放master
     * @throws Exception
     */
    public void releaseMaster() throws Exception{
        if(checkMaster()){//如果是master 删除自己创建的临时节点
            System.out.println("删除master："+serverData.getName());
            zkClient.delete(MASTER_PATH);
        }
    }

    /**
     * 检查服务是否为master
     * @return
     */
    public boolean checkMaster(){
        try {
            RunningData cuccrentMasterData=zkClient.readData(MASTER_PATH,true);
            masterData=cuccrentMasterData;
            if(masterData.getName().equals(serverData.getName())){//如果当前服务名称和master服务名称相同认为是master
                return true;
            }
            return false;
        }catch (ZkNoNodeException e){//master 节点不存在
            return false;
        }catch (ZkInterruptedException e){//链接中断
            return checkMaster();//尝试
        }catch (ZkException e){
            return false;
        }
    }

    public  static void main(String [] args ){
        String str=null;
        Optional optional=Optional.ofNullable(str);
        System.out.println(optional.isPresent());
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }
}
