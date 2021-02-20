package com.fixbug.consumer;

import com.fixbug.proto.RpcMetaProto;
import com.fixbug.util.ZKClientUtils;
import com.google.protobuf.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * @Classname RpcConsumer
 * @Date 2020/10/22 21:44
 * @Created by Fbl
 * @Description TODO
 */

public class RpcConsumer  implements RpcChannel {
    private static final String ZK_SERVER = "zookeeper";
    private String zookeeper;

    public RpcConsumer(String zookeeper) {
        Properties pro = new Properties();

        try {
            pro.load(RpcConsumer.class.getClassLoader().getResourceAsStream(zookeeper));
            this.zookeeper = pro.getProperty(ZK_SERVER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * stub 代理对象 需要接收一个实现了 RpcChannel 的对象
     * ，当用stub调用任意rpc方法的时候，全补都调用了这个方法
     * @param methodDescriptor
     * @param rpcController
     * @param message
     * @param message1
     * @param rpcCallback
     */
    @Override
    public void callMethod(Descriptors.MethodDescriptor methodDescriptor,
                           RpcController rpcController,
                           Message message,
                           Message message1,
                           RpcCallback<Message> rpcCallback) {
        /**
         * 打包参数，通过玩咯发送 这里的网络模块是采用bio
         */
        // 获得服务对象
        Descriptors.ServiceDescriptor sd = methodDescriptor.getService();
        String serviceName = sd.getName();
        String methodName = methodDescriptor.getName();

        // 序列化头部信息
        RpcMetaProto.rpc_meta.Builder meta_builder = RpcMetaProto.rpc_meta.newBuilder();
        meta_builder.setServiceName(serviceName);
        meta_builder.setMethodName(methodName);
        byte[] metaBuf = meta_builder.build().toByteArray();

        // 参数
        byte[] agrBuf = message.toByteArray();

        // 组织rpc参数信息
        ByteBuf buf = Unpooled.buffer(4 + metaBuf.length + agrBuf.length);
        buf.writeInt(metaBuf.length);
        buf.writeBytes(metaBuf);
        buf.writeBytes(agrBuf);

        // 待发送的数据
        byte[] sendBuf = buf.array();

        // todo 通过zookeeper 获取服务的端口
        String ip = "";
        String port = "";
        ZKClientUtils zk = new ZKClientUtils(zookeeper);
        String path = "/" + serviceName + "/" + methodName;
        String hostStr = zk.read(path);
        if(hostStr == null){
            rpcController.setFailed("路径 " + path + " 不合法");
            rpcCallback.run(message1);
            return;
        }else {
            String[] host_ip = hostStr.split(":");
            ip = host_ip[0];
            port = host_ip[1];
        }


        // 通过网络发送rpc请求调用信息
        Socket client = null;
        OutputStream out = null;
        InputStream in = null;

        try {
            client = new Socket();
            client.connect(new InetSocketAddress(ip,Integer.parseInt(port)));
            out = client.getOutputStream();
            in = client.getInputStream();

            // 发送数据
            out.write(sendBuf);
            out.flush();


            // wait 等待响应
            ByteArrayOutputStream recvbuf = new ByteArrayOutputStream();
            byte[] rbuf = new byte[1024];
            int size = in.read(rbuf);
            /**
             * 注意这里的size 又可能为0 因为rpcProvider 响应参数的时候
             * 如果响应参数的成员变量 的值都是默认值，那么实际上rpcProvider 递给 rpcServer就是一个空数据
             */
            if(size > 0){
                recvbuf.write(rbuf, 0, size);
                //System.out.println(recvbuf.size());
                rpcCallback.run(message1.getParserForType().parseFrom(recvbuf.toByteArray()));
            }else {
                rpcCallback.run(message1);
            }
        } catch (IOException e) {
            rpcController.setFailed("网络连接失败！！");
            rpcCallback.run(message1);
        } finally {
            try {
                if(out != null){
                    out.close();
                }
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if(client != null){
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
