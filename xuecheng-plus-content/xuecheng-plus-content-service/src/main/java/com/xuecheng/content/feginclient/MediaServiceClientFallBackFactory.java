package com.xuecheng.content.feginclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Xue
 * @create 2023-09-12-20:35
 */

@Slf4j
@Component
public class MediaServiceClientFallBackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String uploadFile(MultipartFile upload, String objectName) {
                log.debug("调用媒资管理服务上传文件时发生熔断，异常信息:{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
