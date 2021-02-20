package com.fixbug.util;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname ZKClientUtils
 * @Date 2020/10/23 16:11
 * @Created by Fbl
 * @Description 和zookeeper 通信用的辅助类
 */

public class ZKClientUtils {
    // zookeeper 根节点
    private static String rootPath = "/nprpc";

    private ZkClient zkClient;

    private Map<String,String> ephemeralMap = new HashMap<>();

    /**
     * 传入zk_server 的ip  127.0.0.1:2181;
     * @param serverList
     */
    public ZKClientUtils(String serverList) {
        // 连接超时时间
        zkClient = new ZkClient(serverList,3000);

        // 创建根节点
        if(!this.zkClient.exists(rootPath)){
            this.zkClient.createPersistent(rootPath,null);
        }
    }

    public void close(){
        this.zkClient.close();
    }

    /**
     * 创建临时节点
     * @param path
     * @param data
     */
    public void createEphemeral(String path,String data){
        path = rootPath + path;

        if(this.zkClient.exists(path)) return;
        this.zkClient.createEphemeral(path,data);
        ephemeralMap.put(path,data);
    }

    /**
     * 创建永久节点
     * @param path
     */
    public void createPersistent(String path,String data){
        path = rootPath + path;
        if(this.zkClient.exists(path)) return;
        this.zkClient.createPersistent(path,data);
    }

    /**
     * 读取节点的数据
     * @param path
     * @return
     */
    public String read(String path){
        path = rootPath + path;
        if(this.zkClient.exists(path)){
            return this.zkClient.readData(path);
        }
        return null;
    }

    /**
     * 添加一个监听器 防止 provider端关闭30 秒内又重启
     * @param path
     */
    public void addWatcher(String path){
        zkClient.subscribeChildChanges(path, new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                System.out.println("watcher -- >" + path);

                String data = ephemeralMap.get(path);
                if(data != null){
                    createEphemeral(path,data);
                }
            }
        });
    }

    /**
     * 删除节点
     * @param path
     */
    public void delete(String path){
        zkClient.delete(path);
    }

    public static String getRootPath() {
        return rootPath;
    }

    public static void setRootPath(String rootPath) {
        ZKClientUtils.rootPath = rootPath;
    }

    /**
     * 测试 zookeeper 工具类
     * @param args
     */
    public static void main(String[] args) {
        ZKClientUtils zk = new ZKClientUtils("127.0.0.1:2181");
        //zk.createPersistent("/test","111");
        System.out.println(zk.read("/test"));
        zk.close();
    }

}
