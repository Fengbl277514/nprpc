package com.fixbug.provider;

import com.fixbug.callback.INotifyProvider;
import com.fixbug.proto.RpcMetaProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ObjectEncoder;
/**
 * @Classname RpcServer
 * @Date 2020/10/22 14:16
 * @Created by Fbl
 * @Description Server 网络模块
 */

public class RpcServer {
    private INotifyProvider notifyProvider;

    public RpcServer(INotifyProvider notifyProvider) {
        this.notifyProvider = notifyProvider;
    }

    public void start(String ip, int port){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bs = new ServerBootstrap();

        try {
            bs.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            /**
                             * 1. 设置数据的编码和解码器  网络的字节流 《==》 序列化 和 反序列化
                             */
                            //这里业务本身的数据 就是字节流 就不需要解码了
                            //ch.pipeline().addLast(new ObjectDecoder());
                            ch.pipeline().addLast(new ObjectEncoder());// 编码
                            ch.pipeline().addLast(new RpcServerChannel());
                        }
                    });

            // 阻塞开启网络服务
            ChannelFuture future = bs.bind(ip,port).sync();

            // 关闭网络服务
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    /**
     * 主要提供响应的回调操作
     */
    private class RpcServerChannel extends ChannelInboundHandlerAdapter{
        /**
         * 处理接收到的事件
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            /**
             * request 就是远端发送过来的rpc调用请求包含的所有信息参数
             *
             * 消息格式 header_size + UserServiceImpllogin+zhangsan123456
             * 如 20 + 类名 方法名 + 参数
             */
            ByteBuf request = (ByteBuf) msg;

            // 1. 先获取头
            int header_size = request.readInt();

            // 2. 获取类名 方法名
            byte[] metabuf = new byte[header_size];
            request.readBytes(metabuf);
            RpcMetaProto.rpc_meta rpc_meta = RpcMetaProto.rpc_meta.parseFrom(metabuf);
            String serviceName = rpc_meta.getServiceName();
            String methodName = rpc_meta.getMethodName();

            // 3. 获取参数
            byte[] argbuf = new byte[request.readableBytes()];
            request.readBytes(argbuf);

            // 4.
            byte[] response = notifyProvider.notify(serviceName, methodName, argbuf);

            // 5. 把rpc方法调用的响应response 通过网络发给rpc调用方
            ByteBuf buf = Unpooled.buffer(response.length);
            buf.writeBytes(response);
            ChannelFuture future = ctx.writeAndFlush(buf);

            // 7. 模拟http响应完成后，直接关闭连接
            if(future.sync().isSuccess()){
                ctx.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}


