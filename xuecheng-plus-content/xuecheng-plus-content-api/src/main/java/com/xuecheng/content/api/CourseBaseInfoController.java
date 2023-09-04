package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Xue
 * @create 2023-09-03-11:09
 */
@RestController
public class CourseBaseInfoController {
    @Autowired
    public CourseBaseService courseBaseService;
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParam, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        return courseBaseService.pageSearch(pageParam,queryCourseParamsDto);
    }

    @PostMapping("/course")
    public CourseBaseInfoDto add(@RequestBody AddCourseDto addCourseDto){
        return  courseBaseService.add(1232114145L,addCourseDto);
    }
}
