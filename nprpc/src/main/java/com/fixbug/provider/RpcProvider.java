package com.fixbug.provider;

import com.fixbug.callback.INotifyProvider;
import com.fixbug.util.ZKClientUtils;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Classname RpcProvider
 * @Date 2020/10/21 15:21
 * @Created by Fbl
 * @Description rpc 方法发布的站点 只需要一个战点就可
 *              以发布当前主机上所有的rpc方法了，
 *              所有我们用单例模式来设计他
 */

public class RpcProvider implements INotifyProvider {
    private static final String IP = "ip";
    private static final String PORT = "port";
    private static final String Zk_SERVER = "zookeeper";

    private String zk_server;
    private String server_ip;
    private String server_port;

    private Map<String,ServiceInfo> serviceMap;


    /**
     * 因为RpcProvider 只有一个实咧，而 notify 方法可能会有多个
     * 线程访问 ，为了让每个线程都知道其response 所以这里用threadLocal
     */
    private ThreadLocal<byte[]> responsebuflocal;

    private RpcProvider() {
        this.responsebuflocal = new ThreadLocal<>();
        serviceMap = new HashMap<>();
    }

    /**
     * 启动rpc站点提供服务
     */
    public void start() {
       /* serviceMap.forEach((k,v) -> {
            System.out.println(k);
            v.methodMap.forEach((a,b) -> System.out.println(a));
        });*/
        ZKClientUtils zk = new ZKClientUtils(zk_server);
        serviceMap.forEach((k,v) -> {
            String path = "/" + k;
            zk.createPersistent(path,null);
            v.methodMap.forEach((a,b)->{
                String ephPath = path + "/" + a;
                zk.createEphemeral(ephPath,server_ip + ":" + server_port);
                zk.addWatcher(ephPath);
                System.out.println("reg zk -> " + ephPath);
            });
        });

        System.out.println("rpc start at:" + server_ip + " " + server_port);

        // 启动网络服务
        RpcServer server = new RpcServer(this);
        server.start(server_ip,Integer.parseInt(server_port));
    }

    /**
     * 回调函数 接收网络端报上来的数据 进行处理并返回
     * @param serviceName
     * @param methodName
     * @param args
     * @return 把rpc方法调用完成以后的响应值返回
     */
    @Override
    public byte[] notify(String serviceName, String methodName, byte[] args) {
        ServiceInfo si = serviceMap.get(serviceName);
        Service service = si.service;
        Descriptors.MethodDescriptor method = si.methodMap.get(methodName);

        // 从arg反序列化出method 方法的参数，(自定义的proto message) loginRequest, RegRequest
        Message request = service.getRequestPrototype(method);
        try {
            //反序列化操作
            request = request.getParserForType().parseFrom(args);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        service.callMethod(method,null,request,message -> {
            responsebuflocal.set(message.toByteArray());
        });
        return responsebuflocal.get();
    }

    /**
     * 服务方法的类型信息
     */
    private class ServiceInfo{
        public ServiceInfo(){
            this.service = null;
            this.methodMap = new HashMap<>();
        }
        Service service;
        Map<String,Descriptors.MethodDescriptor> methodMap;
    }

    /**s
     * 注册rpc方法
     * 注意这里的传参  因为所有服务都要实现protoBuf提供的Service 接口
     * 体现了面向接口编程的思想 向下解耦s
     * @param service
     */
    public void registerRpcService(Service service) {
        Descriptors.ServiceDescriptor sd = service.getDescriptorForType();
        String serviceName = sd.getName();
        List<Descriptors.MethodDescriptor> methods = sd.getMethods();
        ServiceInfo si = new ServiceInfo();
        si.service = service;
        for (Descriptors.MethodDescriptor method : methods) {
            si.methodMap.put(method.getName(),method);
        }
        serviceMap.put(serviceName,si);
    }


    /**
     * 静态内部类 并封装RpcProvider对象的创建细节 类似protoBuf的建造者模式
     */
    public static class Builder{
        private static RpcProvider INSTANCE = new RpcProvider();

        /**
         * 从配置文件中读取server的ip和端口 初始化rpc
         * @param file
         */
        public Builder(String file){
            Properties pro = new Properties();
            try {
                pro.load(Builder.class.getClassLoader().getResourceAsStream(file));
                INSTANCE.setServer_ip(pro.getProperty(IP));
                INSTANCE.setServer_port(pro.getProperty(PORT));
                INSTANCE.setZk_server(pro.getProperty(Zk_SERVER));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public RpcProvider builder(){
            return INSTANCE;
        }
    }

    public static Builder newBuilder(String file){
        return new Builder(file);
    }

    public String getServer_ip() {
        return server_ip;
    }

    public void setServer_ip(String server_ip) {
        this.server_ip = server_ip;
    }

    public String getServer_port() {
        return server_port;
    }

    public void setServer_port(String server_port) {
        this.server_port = server_port;
    }

    public String getZk_server() {
        return zk_server;
    }

    public void setZk_server(String zk_server) {
        this.zk_server = zk_server;
    }
}
