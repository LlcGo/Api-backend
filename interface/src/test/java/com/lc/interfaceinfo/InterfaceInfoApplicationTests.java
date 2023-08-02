package com.lc.interfaceinfo;

import com.lc.lcclient.client.LcClient;
import com.lc.lcclient.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class InterfaceInfoApplicationTests {

    @Resource
    private LcClient lcClient;

    @Test
    void contextLoads() {
        lcClient.getNameByGet("hello");
        lcClient.getNameByPost("hello2");
        User user = new User();
        user.setUserName("hello3");
        System.out.println(lcClient.getUsernameByPost(user));
    }

}
