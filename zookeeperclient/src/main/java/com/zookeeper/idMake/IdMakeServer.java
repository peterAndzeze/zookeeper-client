package com.zookeeper.idMake;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by wodezuiaishinageren on 2017/9/21.
 * id 生成器服务
 * 实现思路：
 *
 * 按照zookeeper生成节点的顺序规则 进行分布式生成id
 */
public class IdMakeServer {
    private ZkClient zkClient;
    /***zookeeper地址***/
    private final String server;
    /***id 生成节点的跟路径***/
    private final String root;
    /***生成node节点的名称***/
    private final String nodeName;
    /***当前服务运行状态**/
    private volatile boolean  running=false;
    /***删除节点线程池****/
    private ExecutorService cleanExecutor=null;


    public IdMakeServer(String server,String root,String nodeName) {
        this.server = server;
        this.root=root;
        this.nodeName=nodeName;
    }

    /***
     * 启动方法
     * @throws Exception
     */
    public void start() throws  Exception{
            if(running){
                System.out.println("server has stared.....");
                return;
            }
            running=true;
            init();
    }

    /**
     * 停止方法
     * @throws Exception
     */
    public void stop () throws Exception{
            if (!running){
                System.out.println("server has stoped.....");
                return ;
            }
            running=false;
            releaseResource();//释放资源
    }

    /**
     * 初始化服务
     */
    public void init(){
        zkClient=new ZkClient(server,5000,5000,new BytesPushThroughSerializer());
        cleanExecutor= Executors.newFixedThreadPool(1);
        try {
            zkClient.createPersistent(root, true);
        }catch (ZkNodeExistsException e){//如果节点存在不做任何操作
            System.out.println("root 节点已经被创建");
        }
    }

    /**
     * 释放资源
     */
    public void releaseResource(){

        /*
         *shutdown方法：平滑的关闭ExecutorService，当此方法被调用时，
         * ExecutorService停止接收新的任务并且等待已经提交的任务（包含提交正在执行和提交未执行）执行完成。
         * 当所有提交任务执行完毕，线程池即被关闭。
         */
        cleanExecutor.shutdown();
        /**
         *awaitTermination方法：接收人timeout和TimeUnit两个参数，
         * 用于设定超时时间及单位。当等待超过设定时间时，会监测ExecutorService是否已经关闭，
         * 若关闭则返回true，否则返回false。一般情况下会和shutdown方法组合使用
         */
        try{
            cleanExecutor.awaitTermination(2, TimeUnit.SECONDS);
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            cleanExecutor=null;
        }
            if(null!=zkClient){
                zkClient.close();
                zkClient=null;
            }
    }

    /**
     * 检查服务是否运行
     * @return
     */
    public boolean checkRunning(){
        return running;
    }

    /**
     * 生成id
     * @return
     * @throws Exception
     * @param methodType
     */
    public String generateId(String methodType) throws Exception{
        if(checkRunning()){
            return createId(methodType);
        }else{
            start();
            return createId(methodType);
        }

    }

    /**
     * 创建 idnode
     * @param methodType
     * @return
     */
    private String createId(String methodType){
        final String fullNodeName=root.concat("/").concat(nodeName);
        final String curentNodeName= zkClient.createPersistentSequential(fullNodeName,null);
        if(methodType.equals(DeleteMethod.PERSISTENT)){

        }else if(methodType.equals(DeleteMethod.DELTE)){
            zkClient.delete(curentNodeName);
        }else if(methodType.equals(DeleteMethod.DELAY)){//延迟删除
            cleanExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    zkClient.delete(curentNodeName);
                }
            });
        }
        return computationIdValue(curentNodeName);
    }

    /**
     * 计算id
     * @return
     */
    private String computationIdValue(String currentPath){
        int index=currentPath.lastIndexOf(nodeName);
        if(index>0){
            index+=nodeName.length();
            return index<=currentPath.length()?currentPath.substring(index):"";
        }
        return currentPath;

    }
    public static void main(String [] args){
        IdMakeServer idMakeServer=new IdMakeServer("11","1","0001");
        String currentPath="0000001";
        String str=idMakeServer.computationIdValue(currentPath);
        System.out.println(str);

    }

}
