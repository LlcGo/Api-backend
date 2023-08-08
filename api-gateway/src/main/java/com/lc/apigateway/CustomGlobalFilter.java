package com.lc.apigateway;

import com.lc.common.model.entity.InterfaceInfo;
import com.lc.common.model.entity.User;
import com.lc.common.service.InnerInterfaceInfoService;
import com.lc.common.service.InnerUserInterfaceInfoService;
import com.lc.common.service.InnerUserService;
import com.lc.lcclient.util.Sign;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @DubboReference
    private InnerUserService innerUserService;

    public static final List<String> WHITE_LIST = Arrays.asList("127.0.0.1");

    public static final String preUrl = "http://localhost:8123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.请求日志
        ServerHttpRequest request = exchange.getRequest();
        log.info("请求request id:" + request.getId());
        String path = preUrl +  request.getPath().toString();
        log.info("请求request 路径:" + path);
        log.info("请求request 远程调用ip:" + request.getRemoteAddress().getAddress());
        String hostAddress = request.getLocalAddress().getAddress().getHostAddress();
        log.info("请求request 本地ip:" + hostAddress);
        log.info("请求request 参数:" + request.getQueryParams());
        log.info("请求request 路径:" + request.getURI());
        String method = request.getMethod().toString();
        log.info("请求request 方法:" + method);
        //2.黑白名单
        ServerHttpResponse response = exchange.getResponse();
        if (!WHITE_LIST.contains(hostAddress)) {
            return noAuth(response);
        }
        //用户鉴权
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String once = headers.getFirst("once");
        String timeTemp = headers.getFirst("timeTemp");
        String body = headers.getFirst("body");
        //todo 数据库中查询是否有这个权限
        User invokeUser = null;
        try {
            invokeUser  = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.info("getInvokeUser err",e);
        }
        if (invokeUser == null){
            return noAuth(response);
        }
        //如果时间超过5分钟不允许访问
        final Long FIVE_TIME = 60 * 5L;
        Long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - Long.parseLong(timeTemp) >= FIVE_TIME) {
            return noAuth(response);
        }
        //发送的
        String sign1 = headers.getFirst("sign");

        String secretKey = invokeUser.getSecretKey();
        //加密后的
        String sign = Sign.getSign(body, secretKey);
        if (!sign.equals(sign1)) {
            throw new RuntimeException("无权限");
        }

        InterfaceInfo interfaceInfoByPath = null;
        try {
            interfaceInfoByPath = innerInterfaceInfoService.getInterfaceInfoByPath(path, method);
        } catch (Exception e) {
            log.info("getInterfaceInfoByPath err",e);
        }
        if(interfaceInfoByPath == null){
            return noAuth(response);
        }
        Long interfaceInfoId = interfaceInfoByPath.getId();
        Long userId = invokeUser.getId();
        return log(exchange,chain,userId,interfaceInfoId);

//        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> noAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> log(ServerWebExchange exchange, GatewayFilterChain chain,Long userId,Long interfaceInfoId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            HttpStatus statusCode = originalResponse.getStatusCode();

            if(statusCode == HttpStatus.OK){
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                        //log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                //todo 修改次数
                                try {
                                    innerUserInterfaceInfoService.invokeCount(userId,interfaceInfoId);
                                } catch (Exception e) {
                                   log.info("invokeCount error",e);
                                }
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                sb2.append("<--- {} {} \n");
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                //rspArgs.add(requestUrl);
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);
                                log.info("响应结果:" + data);//log.info("<-- {} {}\n", originalResponse.getStatusCode(), data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        }catch (Exception e){
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }

    }
}