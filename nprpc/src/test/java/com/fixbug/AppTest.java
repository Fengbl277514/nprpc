package com.fixbug;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @Classname UserServiceProto
 * @Date 2020/10/21 11:07
 * @Created by Fbl
 * @Description TODO
 */

public class AppTest {
    @Test
    public void test() throws InvalidProtocolBufferException {
        TestProto.LoginRequest.Builder login_builder = TestProto.LoginRequest.newBuilder();

        login_builder.setName("张三");
        login_builder.setPwd("111");

        TestProto.LoginRequest request = login_builder.build();
        System.out.println(request.getName());
        System.out.println(request.getPwd());

        // 序列化
        /**
         * 此处的sendBuf 就可以 通过网络发送出去了
         */
        byte[] sendBuf = request.toByteArray();

        // 反序列化
        TestProto.LoginRequest request1 = TestProto.LoginRequest.parseFrom(sendBuf);

        System.out.println(request1.getPwd());
        System.out.println(request1.getName());

    }

    @Test
    public void test1() throws IOException {
        Properties properties = new Properties();
        properties.load(AppTest.class.getClassLoader().getResourceAsStream("config.properties"));
        System.out.println(properties.getProperty("ip"));
    }
}
