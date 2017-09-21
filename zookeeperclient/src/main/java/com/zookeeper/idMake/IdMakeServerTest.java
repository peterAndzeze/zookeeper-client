package com.zookeeper.idMake;

/**
 * Created by wodezuiaishinageren on 2017/9/21.
 */
public class IdMakeServerTest {

    public static void main(String args [] ) throws Exception {
        IdMakeServer idMakeServer=new IdMakeServer("192.168.109.131:2181","/namespace/idGen","ID");
        idMakeServer.start();
        try{
            System.out.println(DeleteMethod.DELAY.name());
            for (int i=0;i<10;i++) {
                String id = idMakeServer.generateId(DeleteMethod.DELAY.name());
                System.out.println("得到的id："+id);
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            idMakeServer.stop();
        }
    }
}
