package com.zookeeper.javaApi;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by wodezuiaishinageren on 2017/9/20.
 */
public class ZookeeperClientTest {
    private ZookeeperClient zookeeperClient;

    @Before
    public void befor(){
        zookeeperClient=new ZookeeperClient();
        zookeeperClient.connectZookeeper(zookeeperClient);
    }

    @Test
    public void getConnectionInfo(){
       // zookeeperClient.createNode("/zookeeper","sunwei");
    }

}
