package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;

 @GetMapping("/coursepreview/{courseId}")
 public ModelAndView preview(@PathVariable("courseId") Long courseId){

     //获取课程预览信息
     CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

     ModelAndView modelAndView = new ModelAndView();
     modelAndView.addObject("model",coursePreviewInfo);
     modelAndView.setViewName("course_template");
     return modelAndView;

  }

    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId,courseId);

    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId,courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        return coursePublish;
    }

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @PostMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursepublishDto(@PathVariable Long courseId){
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish,courseBaseInfoDto);

        String teachplan = coursePublish.getTeachplan();
        List<TeachPlanDto> teachPlanDtos = JSON.parseArray(teachplan, TeachPlanDto.class);

        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachPlanDtos);
        return coursePreviewDto;
    }

}
