package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Override
    public List<CourseTeacher> getCourseTeacher(String courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    public CourseTeacher addTeacher(Long companyId, CourseTeacher courseTeacher) {
        Long courseId = courseTeacher.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能添加本机构课程的老师！");
        }
        courseTeacher.setCreateDate(LocalDateTime.now());
        courseTeacherMapper.insert(courseTeacher);
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    @Override
    public CourseTeacher updateTeacher(Long companyId, CourseTeacher courseTeacher) {
        Long courseId = courseTeacher.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能更新本机构课程的老师！");
        }
        courseTeacherMapper.updateById(courseTeacher);
        return courseTeacher;
    }

    @Override
    public void deleteTeacher(Long companyId, Long courseId, Long teacherId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能删除本机构课程的老师！");
        }
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        queryWrapper.eq(CourseTeacher::getId,teacherId);
        courseTeacherMapper.delete(queryWrapper);
    }

}
