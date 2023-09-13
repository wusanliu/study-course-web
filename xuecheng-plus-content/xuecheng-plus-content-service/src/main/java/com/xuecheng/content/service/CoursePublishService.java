package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-03
 */
public interface CoursePublishService extends IService<CoursePublish> {

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    void commitAudit(Long companyId, Long courseId);

    void publish(Long companyId, Long courseId);

    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     */
    public File generateCourseHtml(Long courseId);

    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     * @return void
     */
    public void  uploadCourseHtml(Long courseId,File file);

}
