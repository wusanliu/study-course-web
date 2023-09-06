package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Xue
 * @create 2023-09-04-11:50
 */
@RestController
public class CourseCategoryController {

    @Autowired
    public CourseCategoryService courseCategoryService;
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryDto> tree(){
        return courseCategoryService.queryTreeNodes("1");
    }
}
