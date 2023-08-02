package com.lc.lcclient.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.lc.lcclient.model.User;
import com.lc.lcclient.util.Sign;

import java.util.Date;
import java.util.HashMap;

/**
 * @Author Lc
 * @Date 2023/7/27
 * @PackageName: com.lc.interfaceinfo.client
 * @ClassName: LcClient
 * @Description:
 */

public class LcClient {

    private String accessKey;

    private String secretKey;

    public LcClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getNameByGet(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.get("http://localhost:8123/api/name/get/", paramMap);
        System.out.println(result);
        return result;
    }


    public String getNameByPost(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.post("http://localhost:8123/api/name/post/", paramMap);
        System.out.println(result);
        return result;
    }

    public HashMap<String, String> getHandlerMap(String body){
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("accessKey",accessKey);
//        stringStringHashMap.put("secretKey",secretKey);
        stringStringHashMap.put("once", RandomUtil.randomNumbers(4));
        stringStringHashMap.put("timeTemp",new Date().toString());
        stringStringHashMap.put("body",body);
        stringStringHashMap.put("sign", Sign.getSign(body,secretKey));
        return stringStringHashMap;
    }

    public String getUsernameByPost(User user) {
        String json = JSONUtil.toJsonStr(user);
        String result2 = HttpRequest.post("http://localhost:8123/api/name/user/")
                .body(json)
                .addHeaders(getHandlerMap(json))
                .execute().body();
        return result2;
    }
}
