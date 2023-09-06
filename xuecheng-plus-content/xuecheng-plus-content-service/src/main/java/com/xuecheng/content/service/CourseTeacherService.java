package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-03
 */
public interface CourseTeacherService extends IService<CourseTeacher> {

    List<CourseTeacher> getCourseTeacher(String courseId);

    CourseTeacher addTeacher(Long companyId, CourseTeacher courseTeacher);

    CourseTeacher updateTeacher(Long companyId, CourseTeacher courseTeacher);

    void deleteTeacher(Long companyId, Long courseId, Long teacherId);
}
