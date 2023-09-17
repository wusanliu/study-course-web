package com.xuecheng.learning.service.Impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTableService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Xue
 * @create 2023-09-17-16:49
 */
@Slf4j
@Service
public class LearningServiceImpl implements LearningService {
    @Autowired
    MyCourseTableService myCourseTableService;
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, String mediaId) {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
//        判断学习资格
//            判断是否试学（略）
//            判断是否登录
        if (StringUtils.isNotEmpty(userId)){
//            获取学习资格
            XcCourseTablesDto learningStatus = myCourseTableService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if ("702002".equals(learnStatus)){
                return RestResponse.validfail("无法学习，因为没有资格");
            }else if ("702003".equals(learnStatus)){
                return RestResponse.validfail("已过期需要重新续期");
            }else{

            }
        }else {
            String charge = coursepublish.getCharge();
            if("201000".equals(charge)){
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMediaId;
            }
        }

//        远程调用媒资服务查询视频的播放地址
        return RestResponse.validfail("该课程需要购买");
    }
}
