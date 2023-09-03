package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
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
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParam, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        System.out.println("11111111111111");
        return null;
    }
}
