package zkclient.master;

import java.io.Serializable;

/**
 * Created by wodezuiaishinageren on 2017/9/20.
 */
public class RunningData implements Serializable {
    /*服务器编号*/
    private Long cid;
    /***服务器名称**/
    private String name;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
