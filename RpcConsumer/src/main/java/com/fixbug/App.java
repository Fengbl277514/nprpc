package com.fixbug;

import com.fixbug.consumer.RpcConsumer;
import com.fixbug.controller.NRpcController;
import com.google.protobuf.RpcController;

/**
 * @Classname App
 * @Date 2020/10/22 19:29
 * @Created by Fbl
 * @Description 模拟consume r远程调用rpc服务
 */

public class App {

    public static void main(String[] args) {
        /**
         * 模拟rpc方法的调用
         */

        // 客户端的代理对象
        UserServiceProto.UserServiceRpc.Stub stub
                = UserServiceProto.UserServiceRpc.newStub(new RpcConsumer("config.properties"));
        UserServiceProto.LoginRequest.Builder login_builder
                = UserServiceProto.LoginRequest.newBuilder();
        login_builder.setName("张三");
        login_builder.setPwd("12345");
        RpcController controller = new NRpcController();
        stub.login(controller,login_builder.build(),response -> {
            /**
             * 这里是rpc方法调用完成后的返回值
             */

            // rpc 方法没有调用成功
            if(controller.failed()){
                System.out.println(controller.errorText());
            }else {
                System.out.println("接收到的server的响应");
                if(response.getErrno() == 0){
                    System.out.println(response.getResult());
                }else {
                    System.out.println(response.getErrinfo());
                }
            }

        });
    }
}
