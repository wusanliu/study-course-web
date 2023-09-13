package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResult;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @description 媒资文件管理接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
 @Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
 @RestController
public class MediaFilesController {


  @Autowired
  MediaFileService mediaFileService;


 @ApiOperation("媒资列表查询接口")
 @PostMapping("/files")
 public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
  Long companyId = 1232141425L;
  return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);
 }

    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResult uploadPic(@RequestPart("filedata")MultipartFile filedata,
                                      @RequestParam(value = "objectName",required=false) String objectName) throws IOException {
//     准备查询条件
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileType("001001");//图片类型
        uploadFileParamsDto.setFileSize(filedata.getSize());
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
//        接收到文件,创建临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        filedata.transferTo(tempFile);
//     调用service上传图片
        Long companyId=1232141425L;
        String absolutePath = tempFile.getAbsolutePath();
        UploadFileResult fileResult = mediaFileService.upload(companyId, uploadFileParamsDto, absolutePath,objectName);
        return fileResult;
    }

}
