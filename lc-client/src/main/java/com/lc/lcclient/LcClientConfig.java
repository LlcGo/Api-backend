package com.lc.lcclient;

import com.lc.lcclient.client.LcClient;
import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Lc
 * @Date 2023/7/27
 * @PackageName: com.lc.lcclient
 * @ClassName: LcClientConfig
 * @Description:
 */

@Configuration
@ComponentScan
@ConfigurationProperties("client.lc")
@Data
public class LcClientConfig {
    private String accessKey;
    private String secretKey;

    @Bean
    public LcClient lcClient(){
        return new LcClient(accessKey,secretKey);
    }

}
