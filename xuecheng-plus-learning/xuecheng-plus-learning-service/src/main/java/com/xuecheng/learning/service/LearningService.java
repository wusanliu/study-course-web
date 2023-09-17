package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

/**
 * @author Xue
 * @create 2023-09-17-16:43
 */
public interface LearningService {
    public RestResponse<String> getVideo(String userId,Long courseId,String mediaId);
}
