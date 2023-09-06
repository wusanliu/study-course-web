package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.error.ErrorReturn;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanMediaService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Override
    public List<TeachPlanDto> get(Long id) {
        return teachplanMapper.selectTreeNodes(id);
    }

    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan!=null){
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else {
            Teachplan teachplan1 = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan1);
            teachplan1.setCreateDate(LocalDateTime.now());
//            补充排序字段
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId,teachplan1.getCourseId());
            queryWrapper.eq(Teachplan::getParentid,teachplan1.getParentid());
            Integer count = teachplanMapper.selectCount(queryWrapper);
            teachplan1.setOrderby(count+1);
            teachplanMapper.insert(teachplan1);
        }
    }

    @Override
    public ErrorReturn delete(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        if(teachplan.getParentid()==0L){
//            大章节
            Long id = teachplan.getId();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,id);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count<=0){
//                没有子章节
                teachplanMapper.deleteById(teachplanId);
            }else {
//                有子章节
                return new ErrorReturn("120409","课程计划还有子信息，无法删除");
            }
        }else {
//            小章节
//            删除关联信息
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId,teachplan.getId());
            queryWrapper.eq(TeachplanMedia::getCourseId,teachplan.getCourseId());
            teachplanMediaMapper.delete(queryWrapper);
//            删除小章节
//            更新章节排序节点
            LambdaQueryWrapper<Teachplan> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(Teachplan::getParentid,teachplan.getParentid());
            queryWrapper1.gt(Teachplan::getOrderby,teachplan.getOrderby());
            List<Teachplan> teachplanList = teachplanMapper.selectList(queryWrapper1);
            for(Teachplan teachplan1:teachplanList){
                teachplan1.setOrderby(teachplan1.getOrderby()-1);
                teachplanMapper.updateById(teachplan1);
            }
            teachplanMapper.deleteById(teachplanId);
        }
        return null;
    }

    @Override
    public void moveup(String teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan.getOrderby()==1) return;
        Long parentid = teachplan.getParentid();
//        上面的节点向下移动
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,parentid);
        queryWrapper.eq(Teachplan::getOrderby,teachplan.getOrderby()-1);
        Teachplan teachplan1 = teachplanMapper.selectOne(queryWrapper);
        teachplan1.setOrderby(teachplan1.getOrderby()+1);
        teachplanMapper.updateById(teachplan1);
//        节点更新
        teachplan.setOrderby(teachplan.getOrderby()-1);
        teachplanMapper.updateById(teachplan);
    }

    @Override
    public void movedown(String teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        LambdaQueryWrapper<Teachplan> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Teachplan::getParentid,teachplan.getParentid());
        queryWrapper1.gt(Teachplan::getOrderby,teachplan.getOrderby());
        Integer count = teachplanMapper.selectCount(queryWrapper1);
        if(count>0){
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,teachplan.getParentid());
            queryWrapper.eq(Teachplan::getOrderby,teachplan.getOrderby()+1);
            Teachplan teachplan1 = teachplanMapper.selectOne(queryWrapper);
            teachplan1.setOrderby(teachplan1.getOrderby()-1);
            teachplanMapper.updateById(teachplan1);
            teachplan.setOrderby(teachplan.getOrderby()+1);
            teachplanMapper.updateById(teachplan);
        }
    }

}
