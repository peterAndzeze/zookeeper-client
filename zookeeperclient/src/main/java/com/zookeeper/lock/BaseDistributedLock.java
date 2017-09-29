package com.zookeeper.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by wodezuiaishinageren on 2017/9/21.
 */
public class BaseDistributedLock {
    private  final  ZkclientExt zkclient;
    private  final  String path;
    private  final String basePath;
    private String lockName;
    private static final Integer MAX_RETRY_COUNT=10;

    public BaseDistributedLock(ZkclientExt zkclient, String path, String lockName) {
        this.basePath = path;
        this.lockName = lockName;
        this.zkclient=zkclient;
        this.path=path.concat("/").concat(lockName);
        System.out.println("path------>"+path);
        this.lockName=lockName;
    }

    /**
     * 删除当前路径
     * @param currentPath
     * @throws Exception
     */
    public void deleteCurrent(String currentPath) throws  Exception{
        zkclient.delete(currentPath);
    }

    /**
     * 创建节点
     * @param zkclient
     * @param path
     * @return
     * @throws Exception
     */
    private String  createLockNode(ZkclientExt zkclient,String path) throws  Exception{
        return zkclient.createEphemeralSequential(path,null);
    }

    /**
     * 等待锁
     * @param startMillis 开始时间
     * @param millisToWait 结束时间
     * @param currentpath 当前节点
     * @return
     * @throws Exception
     */
    private boolean waitToLock(Long startMillis,Long millisToWait,String currentpath) throws  Exception{
        boolean haveTheLock=false;//是否持有锁
        boolean doDelete=false;//是否删除
        try{
            while (!haveTheLock){//如果没有持有锁 进入等待
                List<String> sortChildes=getSortChilds();
                String squenceName=currentpath.substring(basePath.length()+1);
                int ourIndex=sortChildes.indexOf(squenceName);
                if(ourIndex<0){//没有这个节点
                    throw new ZkNoNodeException("节点没有找到："+squenceName);
                }
                boolean isGetTheLock=ourIndex==0;//是第一个
                //如果是第一个pathToWatch 为空否则取出当前最小的节点名称
                String pathToWatch=isGetTheLock?null:sortChildes.get(ourIndex-1);
                if(isGetTheLock){//取得锁
                    haveTheLock=true;
                }else{//如果没有
                    String haveLockPath=basePath.concat("/").concat(pathToWatch);//获取持有锁的path
                    //等待持有锁线程完成锁释放后立即获取锁
                    final CountDownLatch countDownLatch=new CountDownLatch(1);
                    final IZkDataListener haveLockPathListeners=new IZkDataListener() {
                        @Override
                        public void handleDataChange(String s, Object o) throws Exception {
                            //ignore
                        }

                        @Override
                        public void handleDataDeleted(String s) throws Exception {
                            countDownLatch.countDown();//持有锁的节点被删除之后线程释放
                        }
                    };
                    try{
                        //把持有锁的路径加入监听／／如果路径没有则报异常
                        zkclient.subscribeDataChanges(haveLockPath,haveLockPathListeners);
                        if(null!=millisToWait){//等待时间如果不为空
                            millisToWait-=System.currentTimeMillis()-startMillis;
                            startMillis=System.currentTimeMillis();
                            if(millisToWait<=0){//等待删除节点如果为负数 则超出等待时间
                                doDelete=true;//删除当前节点
                                break;

                            }
                            countDownLatch.await(millisToWait, TimeUnit.MICROSECONDS);//否则等待到该时间点
                        }else{//等待时间为空则一直等待
                            countDownLatch.await();
                        }
                    }catch(ZkNoNodeException e){//节点不存在了
                        //ignore

                    }finally {
                        //取消监听
                        zkclient.unsubscribeDataChanges(haveLockPath,haveLockPathListeners);
                    }

                }


            }
        }catch(Exception e){
            doDelete=true;//程序出现异常删除节点
            throw  e;
        }finally {
            if(doDelete){//删除节点
                deleteCurrent(currentpath);
            }
        }
        return haveTheLock;
    }

    /**
     * 得到排序后的子节点信息 (正叙)
     * @return
     */
    private List<String> getSortChilds(){
        try {
            List<String> childes = zkclient.getChildren(basePath);
            childes.sort((String a1, String b1) -> {
                String a = a1.substring(a1.lastIndexOf("0"), a1.length());
                String b = b1.substring(b1.lastIndexOf("0"), b1.length());
                int i = Integer.valueOf(a).compareTo(Integer.valueOf(b));
                return i;
            });
            return childes;
        }catch (ZkNoNodeException e){
            zkclient.createPersistent(basePath,true);
            return getSortChilds();
        }

    }

    /**
     * 尝试获取锁
     * @param time
     * @param timeUnit
     */
    protected  String  attemptLock(Long time,TimeUnit timeUnit){
        final long      startMillis = System.currentTimeMillis();
        final Long      millisToWait = (timeUnit != null) ? timeUnit.toMillis(time) : null;
        String ourPath=null;
        boolean haveTheLock=false;
        boolean isDone=false;
        int  retryCount = 0;
        //网络闪断重试
        while (!isDone){
            isDone=true;

            ourPath=createLockNodePath(zkclient,path);
            try {
                haveTheLock = waitToLock(startMillis, millisToWait, ourPath);
            } catch (ZkNoNodeException e) {
                if(retryCount++<MAX_RETRY_COUNT){//如果小于最大重试次数
                    isDone=false;
                }else{
                    throw  e;
                }
            }catch (Exception e){

            }

        }
        if (haveTheLock){
            return ourPath;
        }
        return null;

    }
    /**
     * 创建节点
     * @param zkclient
     * @param path
     * @return
     */
    private  String createLockNodePath(ZkclientExt zkclient,String path){
        /**创建有序的临时节点*/
        return zkclient.createEphemeralSequential(path,new Random().toString().getBytes());
    }

    /**
     * 释放锁
     * @param lockPath
     */
    protected  void releaseLock(String lockPath) throws Exception {
        deleteCurrent(lockPath);
    }

    /***
     * 检查根节点
     */
    protected void checkBasePath(){
        boolean exists= zkclient.exists(basePath);
        if (!exists){
            zkclient.createPersistent(basePath,true);
        }
    }



}
