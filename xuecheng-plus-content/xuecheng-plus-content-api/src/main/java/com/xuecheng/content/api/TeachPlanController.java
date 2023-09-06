package com.xuecheng.content.api;

import com.xuecheng.base.error.ErrorReturn;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Xue
 * @create 2023-09-06-10:29
 */
@RestController
public class TeachPlanController {
    @Autowired
    private TeachplanService teachplanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> get(@PathVariable Long courseId){
        return teachplanService.get(courseId);
    }
    @PostMapping("/teachplan")
    public void saveTeachPlan(@RequestBody SaveTeachplanDto saveTeachplanDto){
       teachplanService.saveTeachPlan(saveTeachplanDto);
    }

    @DeleteMapping("/teachplan/{teachplanId}")
    public ErrorReturn delete(@PathVariable Long teachplanId){
        return teachplanService.delete(teachplanId);
    }

    @PostMapping("/teachplan/moveup/{teachplanId}")
    public void moveup(@PathVariable String teachplanId){
        teachplanService.moveup(teachplanId);
    }

    @PostMapping("/teachplan/movedown/{teachplanId}")
    public void movedown(@PathVariable String teachplanId){
        teachplanService.movedown(teachplanId);
    }
}
