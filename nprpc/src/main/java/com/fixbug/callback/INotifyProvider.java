package com.fixbug.callback;

/**
 * @Classname INotifyProvider
 * @Date 2020/10/22 16:12
 * @Created by Fbl
 * @Description RpcProvider notify方法的回调
 */

public interface INotifyProvider {
    /**
     * 回调操作 rpc server 向rpcProvider 上报接收到的rpc服务调用相关参数信息
     * @param serviceName
     * @param methodName
     * @param args
     * @return 把rpc 调用完成后的数据响应返回
     */
   byte[] notify(String serviceName,String methodName,byte[] args);
}
