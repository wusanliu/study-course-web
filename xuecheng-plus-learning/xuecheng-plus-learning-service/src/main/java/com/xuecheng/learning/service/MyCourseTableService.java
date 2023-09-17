package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @author Xue
 * @create 2023-09-17-9:54
 */
public interface MyCourseTableService {
    public XcChooseCourseDto addChooseCourse(String userId,String courseId);

    public XcCourseTablesDto getLearningStatus(String userId,Long courseId);

    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params);
}
