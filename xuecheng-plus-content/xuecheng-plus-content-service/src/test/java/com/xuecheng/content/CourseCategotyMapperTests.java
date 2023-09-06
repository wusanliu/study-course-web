package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author Xue
 * @create 2023-09-04-14:44
 */
@SpringBootTest
public class CourseCategotyMapperTests {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Test
    public void test1(){
        List<CourseCategoryDto> courseCategoryDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryDtos);
    }
}
