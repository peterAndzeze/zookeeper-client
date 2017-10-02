package com.zookeeper.subscribe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.util.List;

/**
 * Created by wodezuiaishinageren on 2017/9/30.
 * 管理服务
 */
public class ManagerServer {
    /**配置节点路径***/
    private String configPath;
    /*****控制器路径**/
    private String commendPath;
    /***服务器路径****/
    private String serverPath;
    private ZkClient zkClient;
    /**监听服务节点***/
    private IZkChildListener iZkChildListener;
    /***配置文件***/
    private DbConfig dbConfig;
    /**服务节点集合***/
    private List<String> workerServers;
    /***服务器子节点监听****/
    private IZkChildListener serverPathChildsListeners;
    /****监听控制器指令？****/
    private IZkDataListener iZkDataListener;

    /***默认执行命令***/
    private volatile String cmd="list";

    public ManagerServer(String configPath,String commendPath,String serverPath,ZkClient zkClient,DbConfig initConfig){
        this.configPath=configPath;
        this.commendPath=commendPath;
        this.serverPath=serverPath;
        this.zkClient=zkClient;
        this.dbConfig=initConfig;
        serverPathChildsListeners=new IZkChildListener() {
            @Override
            public void handleChildChange(String path, List<String> currentChilds) throws Exception {
                workerServers=currentChilds;
                System.out.println("服务器["+path+"]发生变化");
                execList();
            }
        };
        iZkDataListener=new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                String cmd=new String((byte[])data);
                System.out.println("操作指令："+cmd);
                execCmd(cmd);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {

            }
        };

    }

    /**
     * 列出服务器子节点集合
     */
    private void execList(){
        String serversStr=JSONArray.toJSONString(workerServers);
        System.out.println("服务器子节点："+serversStr);
    }

    /***
     * 控制器执行命令
     * @param cmd
     */
    private void execCmd(String cmd){
        if("list".equalsIgnoreCase(cmd)){
            execList();
        }else if("create".equalsIgnoreCase(cmd)){
            execCreate();
        }else if("modify".equalsIgnoreCase(cmd)){
            String newDbconfig=new String((byte[]) zkClient.readData(configPath));
            DbConfig dbConfig=JSON.parseObject(newDbconfig,DbConfig.class);
            dbConfig.setUser("sunwei");
            newDbconfig=JSON.toJSONString(dbConfig);
            //在实际的开发中 重写配置信息就好
            execModify(newDbconfig);
        }else {
            System.out.println("未知操作");
        }
    }

    /**
     * 执行新增
     */
    private void execCreate(){
        if(!zkClient.exists(configPath)){
            try {
                zkClient.createPersistent(configPath, JSON.toJSONString(dbConfig).getBytes());
            }catch (ZkNodeExistsException e){
                zkClient.writeData(configPath,JSON.toJSONString(dbConfig).getBytes());
            }catch (ZkNoNodeException e){
                String parentDir=configPath.substring(0,configPath.lastIndexOf("/"));
                zkClient.createPersistent(parentDir,true);
                execCreate();
            }
        }else{//为了测试
            zkClient.delete(configPath);
            execCreate();
        }
    }


    private  void execModify(String newDbconfig){
        dbConfig=JSON.parseObject(newDbconfig,DbConfig.class);
        try {
            zkClient.writeData(configPath,JSON.toJSONString(dbConfig).getBytes());
        }catch (ZkNoNodeException e){
            execCreate();
        }
    }
    public void start(){
        initRunning();

    }

    private void initRunning(){
        zkClient.subscribeChildChanges(serverPath,serverPathChildsListeners);
        zkClient.subscribeDataChanges(commendPath,iZkDataListener);
    }
    public void stop(){
        zkClient.unsubscribeDataChanges(commendPath,iZkDataListener);
        zkClient.unsubscribeChildChanges(serverPath,serverPathChildsListeners);
    }
}
