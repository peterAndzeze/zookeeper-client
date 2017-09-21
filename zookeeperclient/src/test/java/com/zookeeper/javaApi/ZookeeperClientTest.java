package com.zookeeper.javaApi;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by wodezuiaishinageren on 2017/9/20.
 */
public class ZookeeperClientTest {
    @Test
    public void getChilds() throws Exception {
        List<String> childs=zookeeperClient.getChilds("/");
        childs.stream().forEach(c-> System.out.println("子节点："+c));
    }

    @Test
    public void getNodeInfo() throws Exception {
        zookeeperClient.createNode("/master","sunwei");
        String stat=zookeeperClient.getNodeInfo("/master");
        System.out.println(stat);
    }

    private ZookeeperClient zookeeperClient;

    @Before
    public void befor(){
        try {
            zookeeperClient=new ZookeeperClient();
            zookeeperClient.connectZookeeper();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void creteNode(){
        zookeeperClient.createNode("/master","sunwei");
    }

}
