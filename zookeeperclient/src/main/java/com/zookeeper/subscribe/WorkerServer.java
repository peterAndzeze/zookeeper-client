package com.zookeeper.subscribe;

import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.util.Optional;

/**
 * Created by wodezuiaishinageren on 2017/9/30.
 * 服务器工作类
 */
public class WorkerServer {
   private ZkClient zkClient;
   /**配置节点路径**/
   private String configPath;
   /***服务节点根路径****/
   private String serverPath;
   /**当前服务的基本信息**/
   private ServerData serverData;
   /***服务节点原始配置数据***/
   private DbConfig serConfig;
   private IZkDataListener iZkDataListener;
   /***当前节点路径***/
   private String currentPath;

    public WorkerServer(ZkClient zkClient, String configPath, String serverPath, ServerData serverData) {
        this.zkClient = zkClient;
        this.configPath = configPath;
        this.serverPath = serverPath;
        this.serverData = serverData;
        this.iZkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                //监听配置文件节点数据变化
                String retJson=new String((byte[])data);
                DbConfig dbConfig=JSON.parseObject(retJson,DbConfig.class);
                updateConfig(dbConfig);
                System.out.println("new workserver config is:"+retJson);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                //ignore
            }
        };
    }

    /**
     * 服务启动
     */
    public void start(){
        System.out.println(" server is starting。。。。。");
        initRunning();
    }

    /**
     * 停止服务
     */
    public void stop(){
        System.out.println("server is stoping 。。。。");
        zkClient.unsubscribeDataChanges(configPath,iZkDataListener);
    }

    /**
     * 初始化数据信息
     */
    private void initRunning(){
        registServerPath();
        //注册配置文件节点监听
        zkClient.subscribeDataChanges(configPath,iZkDataListener);
    }

    /**
     * 注册
     */
    private void  registServerPath(){
        String mepath=serverPath.concat("/").concat(serverData.getAddress());
        System.out.println("mePath-->"+mepath);
        try {
            zkClient.createEphemeral(mepath, JSON.toJSONString(serverData).getBytes());
            if(Optional.ofNullable(serConfig).isPresent()) {
                System.out.println("节点：" + mepath + "对应的配置信息：" + serConfig);
            }else{
                System.out.println("节点："+mepath+"还没有配置数据信息");
            }
        }catch(ZkNoNodeException e){
            zkClient.createPersistent(serverPath,true);
            registServerPath();
        }
    }

    /**
     * 更新配置数据
     */
    public void updateConfig(DbConfig changeConfig){
        this.serConfig=changeConfig;
    }

    public ServerData getServerData() {
        return serverData;
    }

    public void setServerData(ServerData serverData) {
        this.serverData = serverData;
    }

    public DbConfig getSerConfig() {
        return serConfig;
    }

    public void setSerConfig(DbConfig serConfig) {
        this.serConfig = serConfig;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }
}
