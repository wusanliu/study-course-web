package com.xuecheng.learning.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Xue
 * @create 2023-09-17-9:56
 */
@Slf4j
@Service
public class MyCourseTableServiceImpl implements MyCourseTableService {
    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;
    @Autowired
    ContentServiceClient contentServiceClient;

    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, String courseId) {
//        调用内容管理服务查询课程是否收费
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(Long.valueOf(courseId));
        if (coursepublish==null){
            XueChengPlusException.cast("课程不存在");
        }
        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse=new XcChooseCourse();
        if("201000".equals(charge)){
//        免费课程，则加入到选课表和课程表
            xcChooseCourse = addFreeCoruse(userId, coursepublish);
            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);
        }else{
//        收费课程，向选课表写入数据
            xcChooseCourse = addChargeCoruse(userId, coursepublish);
        }
//        判断学生的学习资格
        XcCourseTablesDto learningStatus = getLearningStatus(userId, Long.valueOf(courseId));

        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
//        查询课程表，判断是否有
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcCourseTables::getCourseId,courseId)
                .eq(XcCourseTables::getUserId,userId);
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(queryWrapper);
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if (xcCourseTables!=null){
//            表示没有该课
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
//        有的话，查询是否过期
        boolean isBefore = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (isBefore){
//            表示过期了
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
//        正常学习
        xcCourseTablesDto.setLearnStatus("702001");
        return xcCourseTablesDto;
    }

    @Override
    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params) {
        int page = params.getPage();
        int size = params.getSize();
        Page<XcCourseTables> xcChooseCoursePage = new Page<>(page,size);
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcCourseTables::getUserId,params.getUserId());
        Page<XcCourseTables> result = xcCourseTablesMapper.selectPage(xcChooseCoursePage, queryWrapper);
        long total = result.getTotal();
        List<XcCourseTables> records = result.getRecords();
        return new PageResult(records, records.size(), page,size);
    }

    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
//        判断是否已经选了，如果选了直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcChooseCourse::getCourseId,coursepublish.getId())
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getStatus,"701001")
                .eq(XcChooseCourse::getOrderType,"700001");
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses.size()>0){
            return xcChooseCourses.get(0);
        }
//        没有选过，写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }

    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){
//        判断是否已经选了，如果选了直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcChooseCourse::getCourseId,coursepublish.getId())
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getStatus,"701002")
                .eq(XcChooseCourse::getOrderType,"700002");
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses.size()>0){
            return xcChooseCourses.get(0);
        }
//        没有选过，写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }
    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){
//        选课成功了才可以添加到课程表
        String status = xcChooseCourse.getStatus();
        if(!"701001".equals(status)){
            XueChengPlusException.cast("选课还没有成功，无法加入课程表");
        }
//        查询有没有记录
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcCourseTables::getCourseId,xcChooseCourse.getCourseId())
                .eq(XcCourseTables::getUserId,xcChooseCourse.getUserId());
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(queryWrapper);
        if (xcCourseTables!=null){
            return xcCourseTables;
        }
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTablesMapper.insert(xcCourseTablesNew);

        return xcCourseTablesNew;
    }

}
