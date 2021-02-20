package com.fixbug;

import com.fixbug.provider.RpcProvider;

/**
 * @Classname App
 * @Date 2020/10/21 14:51
 * @Created by Fbl
 * @Description hello world
 */

public class App {
    public static void main(String[] args) {
        /**
         * 启动一个可以提供rpc远程方法调用的server
         * 1. 需要一个RpcProvider （npRpc提供的）对象
         * 2. 向RpcProvider 上注册rpc方法 UserServiceImpl.login   UserService.reg
         * 3. 启动RpcProvider 这个Server站点了，阻塞等待远程rpc方法调用请求
         */
        RpcProvider.Builder builder = RpcProvider.newBuilder("config.properties");
        RpcProvider provider = builder.builder();

        /**
         * 发布服务
         */
        provider.registerRpcService(new UserServiceImpl());

        provider.start();
    }
}
