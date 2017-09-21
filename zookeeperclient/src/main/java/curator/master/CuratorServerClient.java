package curator.master;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.Optional;

/**
 * Created by wodezuiaishinageren on 2017/9/20.
 */
public class CuratorServerClient  {
    private static final String host="192.168.109.131:2181";

    private CuratorFramework client;

    public CuratorServerClient() {
        try {
            this.client=  CuratorFrameworkFactory.builder()
                    .connectString(host)
                    .connectionTimeoutMs(50000)
                    .sessionTimeoutMs(50000)
                    .retryPolicy(new ExponentialBackoffRetry(1000,3))
                    .build();
            this.client.start();
            System.out.println(this.client.getState());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建节点
     * @param path
     */
    public  void createNode(String path){
        try {
            if(!checkNode(path)) {
                String createBuilder = client.create().withMode(CreateMode.EPHEMERAL).forPath("/master", "lisi".getBytes());
                System.out.println("create node info is :" + createBuilder);
            }else{
                System.out.println(path+"---->已经被创建");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查节点是否存在
     * @param path
     * @return
     */
    public boolean checkNode(String path){
        try {
            Stat stat=client.checkExists().forPath(path);
            Optional optional=Optional.ofNullable(stat);
            return optional.isPresent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String [] args){
        CuratorServerClient  curatorServer=new CuratorServerClient();
        curatorServer.createNode("/master");
    }
}
