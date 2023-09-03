package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author Xue
 * @create 2023-09-03-11:02
 */
@Data
@ToString
public class QueryCourseParamsDto {
    private String auditStatus;
    private String courseName;
    private String publishStatus;
}
