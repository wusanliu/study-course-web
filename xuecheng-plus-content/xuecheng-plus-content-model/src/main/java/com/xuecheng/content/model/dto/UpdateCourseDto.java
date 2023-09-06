package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Xue
 * @create 2023-09-06-9:15
 */
@Data
public class UpdateCourseDto extends AddCourseDto{
    @ApiModelProperty(value = "课程id",required = true)
    private Long courseId;
}
