package com.zookeeper.queue;

import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Created by wodezuiaishinageren on 2017/10/2.
 * 分布式队列
 */
public class SubscribeSimpleQueue<T> {
    protected ZkClient zkClient;
    protected String root;
    private static final String NODE_NAME="N_";

    public SubscribeSimpleQueue(ZkClient zkClient, String root) {
        this.zkClient = zkClient;
        this.root = root;
    }

    /**
     * 获取队列数据个数
     * @return
     */
    public int size(){
        return zkClient.getChildren(root).size();
    }

    /**
     * 队列是否为空
     * @return
     */
    public boolean isEmpty(){
        return zkClient.getChildren(root).size()==0;
    }

    /**
     * 放入队列数据
     * @param data
     */
    public void offer(T data){
        String nodeFullPath=root.concat("/").concat(NODE_NAME);
        try {
            zkClient.createPersistentSequential(nodeFullPath, data);
        }catch (ZkNoNodeException e){//如果root节点不存在
            zkClient.createPersistent(root);
            offer(data);
        }catch (Exception e){
            System.out.println("创建队列数据异常："+e.getMessage());
            ExceptionUtil.convertToRuntimeException(e);
        }
    }

    /**
     * 从队列中取数据
     * @return
     */
   public T poll(){
       try {

           List<String> children = zkClient.getChildren(root);
           if (children.size() == 0) {
               return null;
           }
           Optional<String> optional = children.stream().min(new Comparator<String>() {
               @Override
               public int compare(String o1, String o2) {
                   String a = o1.substring(o1.lastIndexOf("0") + 1, o1.length());
                   String b = o2.substring(o1.lastIndexOf("0") + 1, o1.length());
                   //System.out.println("a-->" + a + ",b---->" + b);
                   return a.compareTo(b);
               }
           });
           System.out.println(optional.get()+"--->"+optional.isPresent());
           if (optional.isPresent()) {
               String nodeFullPath = root.concat("/").concat(optional.get());
              // byte [] dataStr=zkClient.readData(nodeFullPath);
               //System.out.println(dataStr);
               T dataModel=(T) zkClient.readData(nodeFullPath);
               zkClient.delete(nodeFullPath);
               return dataModel;
           }
           return null;
       }catch (Exception e){
           System.out.println("获取数据异常："+e.getMessage());
           throw  ExceptionUtil.convertToRuntimeException(e);
       }
   }

   public static void main(String [] args){

       List<String> list= new ArrayList<>();
       /*for ( String nodeName : list ){

           System.out.println("排序前："+nodeName);
       }
       Collections.sort(list, new Comparator<String>() {
           public int compare(String lhs, String rhs) {
               return getNodeNumber(lhs, NODE_NAME).compareTo(getNodeNumber(rhs, NODE_NAME));
           }
       });
       for ( String nodeName : list ){

           System.out.println("排序后："+nodeName);
       }*/
      // Optional<String> optional=list.stream().reduce(String::concat);//把字符串拼接在一起
       Optional<String> optional=list.stream().min(new Comparator<String>() {
           @Override
           public int compare(String o1, String o2) {
               String a=o1.substring(o1.lastIndexOf("0"),o1.length());
               String b=o2.substring(o1.lastIndexOf("0"),o1.length());
               System.out.println("a-->"+a+",b---->"+b);
               return a.compareTo(b);
           }
       });
       System.out.println(optional.isPresent());


   }
    private static String getNodeNumber(String str, String nodeName) {
        int index = str.lastIndexOf(nodeName);
        if (index >= 0) {
            index += NODE_NAME.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        System.out.println(str+"*******");
        return str;

    }
}
