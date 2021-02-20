package com.fixbug;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * @Classname UserServiceImpl
 * @Date 2020/10/21 14:10
 * @Created by Fbl
 * @Description 本地服务 这里模拟登录和注册
 */

public class UserServiceImpl extends UserServiceProto.UserServiceRpc{
    /**
     * login 的本地服务方法
     * @param name
     * @param pwd
     * @return
     */
    private boolean login(String name, String pwd) {
        System.out.println("姓名："+name);
        System.out.println("密码："+pwd);
        return true;
    }


    /**
     * Login的rpc代理方法
     * @param controller 可以接收方法执行状态
     * @param request
     * @param done
     */
    @Override
    public void login(RpcController controller, UserServiceProto.LoginRequest request, RpcCallback<UserServiceProto.Response> done) {
        // 1. 从request得到远程rpc调用的请求参数
        String name = request.getName();
        String pwd = request.getPwd();

        // 2. 做本地业务
        boolean result = login(name, pwd);

        // 3. 填写方法的响应值
        UserServiceProto.Response.Builder response_builder = UserServiceProto.Response.newBuilder() ;
        response_builder.setErrno(0);
        response_builder.setErrinfo("");
        response_builder.setResult(result);

        // 4. 把response 对象给nprpc框架，第三个就是回调
        done.run(response_builder.build());
    }



    /**
     * reg的rpc代理方法
     * @param controller
     * @param request
     * @param done
     */
    @Override
    public void reg(RpcController controller, UserServiceProto.RegRequest request, RpcCallback<UserServiceProto.Response> done) {

    }
}
