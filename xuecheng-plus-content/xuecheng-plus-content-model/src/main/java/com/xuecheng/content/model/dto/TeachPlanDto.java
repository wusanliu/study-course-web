package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * @author Xue
 * @create 2023-09-06-10:26
 */
@Data
public class TeachPlanDto extends Teachplan{
//    树形结构
    private List<TeachPlanDto> teachPlanTreeNodes;
    private TeachplanMedia teachplanMedia;
}
