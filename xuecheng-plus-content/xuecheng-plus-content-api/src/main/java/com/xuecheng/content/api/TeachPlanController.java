package com.xuecheng.content.api;

import com.xuecheng.base.error.ErrorReturn;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation(value = "课程计划和媒资信息解除绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void deleteMediaBind(@PathVariable Long teachPlanId,@PathVariable String mediaId){
        teachplanService.deleteMediaBind(teachPlanId,mediaId);
    }

}
