package com.xuecheng.ucenter.client;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Xue
 * @create 2023-09-15-17:19
 */
@Slf4j
@Component
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {

            @Override
            public Boolean verify(String key, String code) {
                log.debug("调用验证码服务熔断异常:{}", throwable.getMessage());
                return null;
            }
        };
    }
}

