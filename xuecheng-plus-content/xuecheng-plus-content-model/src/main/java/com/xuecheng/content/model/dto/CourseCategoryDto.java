package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Xue
 * @create 2023-09-04-11:48
 */
@Data
public class CourseCategoryDto extends CourseCategory implements Serializable {
    public List<CourseCategoryDto> childrenTreeNodes;
}
