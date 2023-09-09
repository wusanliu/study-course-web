package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.error.ErrorReturn;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;

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

    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    Boolean deleteMediaBind(Long teachPlanId, String mediaId);
}
