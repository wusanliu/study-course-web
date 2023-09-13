package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feginclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CourseMarketService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

    @Autowired
    CourseBaseService courseBaseInfoService;
    @Autowired
    TeachplanService teachplanService;
    @Autowired
    CourseMarketService courseMarketService;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.get(courseId);

        //课程计划信息
        List<TeachPlanDto> teachplanTree= teachplanService.get(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;

    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
//   1、查询课程基本信息、课程营销信息、课程计划信息等课程相关信息，整合为课程预发布信息。
        //约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if("202003".equals(auditStatus)){
            XueChengPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }

        //课程图片是否填写
        if(StringUtils.isEmpty(courseBase.getPic())){
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }

        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.get(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        //查询课程计划信息
        List<TeachPlanDto> teachplanTree = teachplanService.get(courseId);
        if(teachplanTree.size()<=0){
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }
        //转json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

//   2、向课程预发布表course_publish_pre插入一条记录，如果已经存在则更新，审核状态为：已提交。
        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

//   3、更新课程基本表course_base课程审核状态为：已提交。
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    public void publish(Long companyId, Long courseId) {
//        资格检查
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre==null){
            XueChengPlusException.cast("该课程未通过审核");
        }
        if (!companyId.equals(coursePublishPre.getCompanyId())){
            XueChengPlusException.cast("不能发布非本机构的课程");
        }
        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if(!"202004".equals(auditStatus)){
            XueChengPlusException.cast("操作失败，课程审核通过方可发布。");
        }
//        保存数据到课程发布表,更新课程表中的发布状态字段
        saveCoursePublish(courseId);
//        向消息表中插入任务
        saveCoursePublishMessage(courseId);
//        删除课程预发布表信息
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {

        //静态化文件
        File htmlFile  = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }

        return htmlFile;

    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/"+courseId+".html");
        if(course==null){
            XueChengPlusException.cast("上传静态文件异常");
        }
    }

    private void saveCoursePublish(Long courseId){
        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }

    private void saveCoursePublishMessage(Long courseId){
        MqMessage course_publish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (course_publish==null)
            XueChengPlusException.cast("向消息表插入任务失败");

    }

}
