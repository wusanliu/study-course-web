package com.xuecheng.content.feginclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Xue
 * @create 2023-09-12-20:32
 */
public class MediaServiceClientFallBack implements MediaServiceClient{
    @Override
    public String uploadFile(MultipartFile upload, String objectName) {
        return null;
    }
}
