package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feginclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CourseIndex;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.mapper.MqMessageMapper;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author Xue
 * @create 2023-09-12-15:47
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    SearchServiceClient searchServiceClient;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }


    @Override
    public boolean execute(MqMessage mqMessage) {
//        执行课程发布处理任务
        String courseId = mqMessage.getBusinessKey1();
//        向es写索引数据
        saveCourseIndex(mqMessage, Long.valueOf(courseId));
//        向redis写缓存
        saveCourseCache(mqMessage, Long.valueOf(courseId));
//        课程静态化上传到minio
        generateCourseHtml(mqMessage, Long.valueOf(courseId));
        return true;
    }

    private void generateCourseHtml(MqMessage mqMessage,Long courseId){
//        任务幂等化处理
        Long id = mqMessage.getId();
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne==1){
            log.info("课程静态化任务开始处理");
        }
        //生成静态化页面
        File file = coursePublishService.generateCourseHtml(courseId);
        //上传静态化页面
        if(file!=null){
            coursePublishService.uploadCourseHtml(courseId,file);
        }
        mqMessageService.completedStageOne(id);
    }

    private void saveCourseIndex(MqMessage mqMessage,Long courseId){
//        任务幂等化处理
        Long id = mqMessage.getId();
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo==1){
            log.info("课程静态化任务开始处理");
        }
        Boolean result = saveCourseIndex(courseId);
        if(result){
            //保存第一阶段状态
            mqMessageService.completedStageTwo(id);
        }
    }

    private Boolean saveCourseIndex(Long courseId) {

        //取出课程发布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //拷贝至课程索引对象
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        //远程调用搜索服务api添加课程信息到索引
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengPlusException.cast("添加索引失败");
        }
        return add;
    }

    private void saveCourseCache(MqMessage mqMessage,Long courseId){

    }

}
