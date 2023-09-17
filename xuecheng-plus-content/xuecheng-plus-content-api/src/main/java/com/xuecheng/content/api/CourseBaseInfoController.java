package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author Xue
 * @create 2023-09-03-11:09
 */
@RestController
public class CourseBaseInfoController {
    @Autowired
    public CourseBaseService courseBaseService;
//    @PreAuthorize("hasAuthority('xc_teachmanger_course_list')")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParam, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        return courseBaseService.pageSearch(pageParam,queryCourseParamsDto);
    }

    @PostMapping("/course")
    public CourseBaseInfoDto add(@RequestBody AddCourseDto addCourseDto){
        return  courseBaseService.add(1232114145L,addCourseDto);
    }

    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto get(@PathVariable Long courseId){
        return courseBaseService.get(courseId);
    }

    @PutMapping("/course")
    public CourseBaseInfoDto update(@RequestBody UpdateCourseDto updateCourseDto){
        return courseBaseService.update(1232141425L,updateCourseDto);
    }

    @DeleteMapping("/course/{courseId}")
    public void delete(@PathVariable Long courseId){
        courseBaseService.delete(courseId);
    }
}
