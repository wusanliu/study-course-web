package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanService teachplanService;
    @Override
    public PageResult<CourseBase> pageSearch(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> basePage = courseBaseMapper.selectPage(page, queryWrapper);

        PageResult<CourseBase> pageResult = new PageResult<>();
        pageResult.setItems(basePage.getRecords());
        pageResult.setCounts(basePage.getTotal());
        return pageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto add(Long companyId,AddCourseDto addCourseDto) {
//        参数校验，判断指定字段为空，这里省略
//        向base表写入信息
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        courseBaseMapper.insert(courseBase); //待检验
//        向market表写入信息
        CourseMarket courseMarket = new CourseMarket();
//        确保主键相同，确保一一对应
        courseMarket.setId(courseBase.getId());
        BeanUtils.copyProperties(addCourseDto,courseMarket);
//        单独编写一个方法，实现存在则更新，不存在则添加
        savecourseMarket(courseMarket);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase1 = courseBaseMapper.selectById(courseBase.getId());
        BeanUtils.copyProperties(courseBase1,courseBaseInfoDto);
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseBase.getId());
        BeanUtils.copyProperties(courseMarket1,courseBaseInfoDto);
//        把课程分类的名称放到结果中
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBaseInfoDto.getMt());
        courseBaseInfoDto.setMtName(courseCategory.getName());
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBaseInfoDto.getSt());
        courseBaseInfoDto.setStName(courseCategory1.getName());
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto get(Long courseId) {
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase1 = courseBaseMapper.selectById(courseId);
        BeanUtils.copyProperties(courseBase1,courseBaseInfoDto);
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseId);
        BeanUtils.copyProperties(courseMarket1,courseBaseInfoDto);
//        把课程分类的名称放到结果中
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBaseInfoDto.getMt());
        courseBaseInfoDto.setMtName(courseCategory.getName());
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBaseInfoDto.getSt());
        courseBaseInfoDto.setStName(courseCategory1.getName());
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto update(Long companyId,UpdateCourseDto updateCourseDto) {

        Long courseId = updateCourseDto.getId();
        CourseBase courseBase1 = courseBaseMapper.selectById(courseId);
        if (courseBase1==null){
            XueChengPlusException.cast("课程不存在");
        }
        if(!companyId.equals(courseBase1.getCompanyId())){
            XueChengPlusException.cast("本机构只能修改本机构的课程ID");

        }

        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(updateCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBase);
        if (i<0){
            XueChengPlusException.cast("更新失败");
        }

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(updateCourseDto,courseMarket);
        int i1 = courseMarketMapper.updateById(courseMarket);
        if(i1<0){
            XueChengPlusException.cast("更新失败");
        }
//        更新营销信息
        CourseMarket courseMarket2 = new CourseMarket();
        BeanUtils.copyProperties(updateCourseDto,courseMarket2);
        savecourseMarket(courseMarket2);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase2 = courseBaseMapper.selectById(courseId);
        BeanUtils.copyProperties(courseBase2,courseBaseInfoDto);
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseId);
        BeanUtils.copyProperties(courseMarket1,courseBaseInfoDto);
        //        把课程分类的名称放到结果中
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBaseInfoDto.getMt());
        courseBaseInfoDto.setMtName(courseCategory.getName());
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBaseInfoDto.getSt());
        courseBaseInfoDto.setStName(courseCategory1.getName());
        return courseBaseInfoDto;
    }

    @Override
    public void delete(Long courseId) {
//        删除教师信息
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        courseTeacherMapper.delete(queryWrapper);
//        删除课程计划信息
        LambdaQueryWrapper<Teachplan> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Teachplan::getCourseId,courseId);
        List<Teachplan> teachplanList = teachplanMapper.selectList(queryWrapper1);
        for(Teachplan teachplan:teachplanList){
            teachplanService.delete(teachplan.getId());
        }
//        删除营销信息
        courseMarketMapper.deleteById(courseId);
//        删除课程信息
        courseBaseMapper.deleteById(courseId);
    }

    public void savecourseMarket(CourseMarket courseMarket){
//        这里需要判断参数合法性，省略
//        判断是要更新还是添加
        Long id = courseMarket.getId();
        if(courseMarketMapper.selectById(id)==null){
            courseMarketMapper.insert(courseMarket);
        }else {
            courseMarketMapper.updateById(courseMarket);
        }

    }
}
