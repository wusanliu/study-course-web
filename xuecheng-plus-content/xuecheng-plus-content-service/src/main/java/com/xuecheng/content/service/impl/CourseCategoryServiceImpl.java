package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryDto> queryTreeNodes(String id) {
        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        //调用mapper递归查询出分类信息
        List<CourseCategory> courseCategories = courseCategoryMapper.selectList(queryWrapper);
        ArrayList<CourseCategoryDto> courseCategoryTreeDtos = new ArrayList<>();
        for(CourseCategory courseCategory:courseCategories){
            CourseCategoryDto courseCategoryDto = new CourseCategoryDto();
            courseCategoryDto.setId(courseCategory.getId());
            courseCategoryDto.setName(courseCategory.getName());
            courseCategoryDto.setLabel(courseCategory.getLabel());
            courseCategoryDto.setOrderby(courseCategory.getOrderby());
            courseCategoryDto.setIsLeaf(courseCategory.getIsLeaf());
            courseCategoryDto.setIsShow(courseCategory.getIsShow());
            courseCategoryDto.setParentid(courseCategory.getParentid());
            courseCategoryTreeDtos.add(courseCategoryDto);
        }

        //找到每个节点的子节点，最终封装成List<CourseCategoryTreeDto>
        //先将list转成map，key就是结点的id，value就是CourseCategoryTreeDto对象，目的就是为了方便从map获取结点,filter(item->!id.equals(item.getId()))把根结点排除
        Map<String, CourseCategoryDto> mapTemp = courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //定义一个list作为最终返回的list
        List<CourseCategoryDto> courseCategoryList = new ArrayList<>();
        //从头遍历 List<CourseCategoryTreeDto> ，一边遍历一边找子节点放在父节点的childrenTreeNodes
        courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).forEach(item -> {
            if (item.getParentid().equals(id)) {
                courseCategoryList.add(item);
            }
            //找到节点的父节点
            CourseCategoryDto courseCategoryParent = mapTemp.get(item.getParentid());
            if(courseCategoryParent!=null){
                if(courseCategoryParent.getCourseCategoryList()==null){
                    //如果该父节点的ChildrenTreeNodes属性为空要new一个集合，因为要向该集合中放它的子节点
                    courseCategoryParent.setCourseCategoryList(new ArrayList<CourseCategoryDto>());
                }
                //到每个节点的子节点放在父节点的childrenTreeNodes属性中
                courseCategoryParent.getCourseCategoryList().add(item);
            }



        });

        return courseCategoryList;
    }
}
