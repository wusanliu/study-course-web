package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.error.ErrorReturn;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-03
 */
public interface TeachplanService extends IService<Teachplan> {

    List<TeachPlanDto> get(Long id);

    void saveTeachPlan(SaveTeachplanDto saveTeachplanDto);

    ErrorReturn delete(Long teachplanId);

    void moveup(String teachplanId);

    void movedown(String teachplanId);
}
