package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResult;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Slf4j
 @Service
public class MediaFileServiceImpl implements MediaFileService {

  @Autowired
 MediaFilesMapper mediaFilesMapper;

 @Autowired
 MinioClient minioClient;

 @Autowired
 MediaFileService currentProxy;

 @Autowired
 MediaProcessMapper mediaProcessMapper;

 //存储普通文件
 @Value("${minio.bucket.files}")
 private String bucket_mediafiles;

 //存储视频
 @Value("${minio.bucket.videofiles}")
 private String bucket_video;
 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  
  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }

 //根据扩展名获取mimeType
 private String getMimeType(String extension){
  if(extension == null){
   extension = "";
  }
  //根据扩展名取出mimeType
  ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
  String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
  if(extensionMatch!=null){
   mimeType = extensionMatch.getMimeType();
  }
  return mimeType;

 }

 /**
  * 将文件上传到minio
  * @param localFilePath 文件本地路径
  * @param mimeType 媒体类型
  * @param bucket 桶
  * @param objectName 对象名
  * @return
  */
 @Override
 @Transactional
 public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName){
  try {
   UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
           .bucket(bucket)//桶
           .filename(localFilePath) //指定本地文件路径
           .object(objectName)//对象名 放在子目录下
           .contentType(mimeType)//设置媒体文件类型
           .build();
   //上传文件
   minioClient.uploadObject(uploadObjectArgs);
   log.debug("上传文件到minio成功,bucket:{},objectName:{},错误信息:{}",bucket,objectName);
   return true;
  } catch (Exception e) {
   e.printStackTrace();
   log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}",bucket,objectName,e.getMessage());
  }
  return false;
 }

 //获取文件默认存储目录路径 年/月/日
 private String getDefaultFolderPath() {
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  String folder = sdf.format(new Date()).replace("-", "/")+"/";
  return folder;
 }
 //获取文件的md5
 private String getFileMd5(File file) {
  try (FileInputStream fileInputStream = new FileInputStream(file)) {
   String fileMd5 = DigestUtils.md5Hex(fileInputStream);
   return fileMd5;
  } catch (Exception e) {
   e.printStackTrace();
   return null;
  }
 }
 @Override
 public UploadFileResult upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
//   将文件上传到minio
  String filename = uploadFileParamsDto.getFilename();
  String s = filename.substring(filename.lastIndexOf("."));
  String mimeType = getMimeType(s);
  //  这里有bug,根据文件路径获取文件不合适,凑合写了
  String fileMd5 = getFileMd5(new File(localFilePath));
  String objectName=getDefaultFolderPath()+fileMd5+s;
  boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
  if (!b){
   System.out.println("文件上传失败");
  }
//  更新数据库
  MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
  if (mediaFiles==null){
   System.out.println("文件信息保存失败");
  }
//  返回
  UploadFileResult uploadFileResult = new UploadFileResult();
  BeanUtils.copyProperties(mediaFiles,uploadFileResult);
  return uploadFileResult;
 }

 @Override
 public RestResponse<Boolean> checkFile(String md5) {
//  检验数据库中是否存在
  MediaFiles mediaFiles = mediaFilesMapper.selectById(md5);
  if (mediaFiles==null){
   return RestResponse.validfail(false,"数据库中不存在该文件");
  }
//  检验minio中是否存在

  String bucket = mediaFiles.getBucket();
  String filePath = mediaFiles.getFilePath();
  GetObjectArgs builder = GetObjectArgs.builder()
          .bucket(bucket)
          .object(filePath)
          .build();

  try {
   GetObjectResponse response = minioClient.getObject(builder);
   if (response!=null){
    return RestResponse.success(true);
   }
  } catch (Exception e) {
   e.printStackTrace();
  }
  return RestResponse.success(false);
 }

 @Override
 public RestResponse<Boolean> checkChunk(String md5, int chunk) {

  //  检验minio中是否存在
  String filePath = getMd5Path(md5);
  GetObjectArgs builder = GetObjectArgs.builder()
          .bucket(bucket_video)
          .object(filePath+chunk)
          .build();

  try {
   GetObjectResponse response = minioClient.getObject(builder);
   if (response!=null){
    return RestResponse.success(true);
   }
  } catch (Exception e) {
   e.printStackTrace();
  }
  return RestResponse.success(false);
 }

 @Override
 public RestResponse<Boolean> uploadChunk(String md5, int chunk, String localChunkFilePath) {
//  将文件分块上传到minio
  String md5Path = getMd5Path(md5);
  String objectName = md5Path + chunk;
  String mimeType = getMimeType(null);
  boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, objectName);
  return b?RestResponse.success(true):RestResponse.validfail("上传分块文件失败");
 }

 @Override
 public RestResponse<Boolean> mergeChunks(Long companyId, String md5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
  String md5Path = getMd5Path(md5);
//  找到分块文件，进行合并
  List<ComposeSource> composeSources = new LinkedList<>();
  for (int i = 0; i < chunkTotal; i++) {
   ComposeSource composeSource = ComposeSource.builder()
           .bucket(bucket_video)
           .object(md5Path + i)
           .build();
   composeSources.add(composeSource);
  }
  String filename = uploadFileParamsDto.getFilename();
  String s = filename.substring(filename.lastIndexOf("."));
  String objectName = getFilePathByMd5(md5, s);
  ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
          .bucket(bucket_video)
          .object(objectName)
          .sources(composeSources)
          .build();
  try {
   minioClient.composeObject(composeObjectArgs);
  } catch (Exception e) {
   e.printStackTrace();
   return RestResponse.validfail("合并文件异常");
  }
//  检验上传的文件和源文件是否相同
//      先下载文件
  File file = downloadFileFromMinIO(bucket_video, objectName);
  try {
   String merge_md5 = DigestUtils.md5Hex(new FileInputStream(file));
   if(merge_md5!=md5){
    return RestResponse.validfail(false,"文件校验失败");
   }
  } catch (IOException e) {
   e.printStackTrace();
  }
  uploadFileParamsDto.setFileSize(file.length());
//  将文件信息入库
  MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, md5, uploadFileParamsDto, bucket_video, objectName);
  if (mediaFiles==null){
   return RestResponse.validfail("文件信息入库失败");
  }
//  清理文件分块
  clearChunkFiles(md5Path,chunkTotal);
  return RestResponse.success(true);
 }

 /**
  * 清除分块文件
  * @param chunkFileFolderPath 分块文件路径
  * @param chunkTotal 分块文件总数
  */
 private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){
  Iterable<DeleteObject> objects =  Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> new DeleteObject(chunkFileFolderPath+ i)).collect(Collectors.toList());;
  RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
  Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
  //要想真正删除
  results.forEach(f->{
   try {
    DeleteError deleteError = f.get();
   } catch (Exception e) {
    e.printStackTrace();
   }
  });

 }
 /**
  * @description 将文件信息添加到文件表
  * @param companyId  机构id
  * @param fileMd5  文件md5值
  * @param uploadFileParamsDto  上传文件的信息
  * @param bucket  桶
  * @param objectName 对象名称
  * @return com.xuecheng.media.model.po.MediaFiles
  * @author Mr.M
  * @date 2022/10/12 21:22
  */
 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
  //将文件信息保存到数据库
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if(mediaFiles == null){
   mediaFiles = new MediaFiles();
   BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
   //文件id
   mediaFiles.setId(fileMd5);
   //机构id
   mediaFiles.setCompanyId(companyId);
   //桶
   mediaFiles.setBucket(bucket);
   //file_path
   mediaFiles.setFilePath(objectName);
   //file_id
   mediaFiles.setFileId(fileMd5);
   //url
   mediaFiles.setUrl("/"+bucket+"/"+objectName);
   //上传时间
   mediaFiles.setCreateDate(LocalDateTime.now());
   //状态
   mediaFiles.setStatus("1");
   //审核状态
   mediaFiles.setAuditStatus("002003");
   //插入数据库
   int insert = mediaFilesMapper.insert(mediaFiles);
   if(insert<=0){
    log.debug("向数据库保存文件失败,bucket:{},objectName:{}",bucket,objectName);
    return null;
   }
   addWaitingTask(mediaFiles);
   return mediaFiles;
  }
  return mediaFiles;
 }
 /**
  * 添加待处理任务
  * @param mediaFiles 媒资文件信息
  */
 private void addWaitingTask(MediaFiles mediaFiles){
  //文件名称
  String filename = mediaFiles.getFilename();
  //文件扩展名
  String exension = filename.substring(filename.lastIndexOf("."));
  //文件mimeType
  String mimeType = getMimeType(exension);
  //如果是avi视频添加到视频待处理表
  if(mimeType.equals("video/x-msvideo")){
   MediaProcess mediaProcess = new MediaProcess();
   BeanUtils.copyProperties(mediaFiles,mediaProcess);
   mediaProcess.setStatus("1");//未处理
   mediaProcess.setFailCount(0);//失败次数默认为0
   mediaProcessMapper.insert(mediaProcess);
  }
 }

 public String getMd5Path(String md5){
  return md5.substring(0,1)+"/"+md5.substring(1,2)+"/"+md5+"/"+"chunk"+"/";
 }

 /**
  * 从minio下载文件
  * @param bucket 桶
  * @param objectName 对象名称
  * @return 下载后的文件
  */
 @Override
 public File downloadFileFromMinIO(String bucket,String objectName){
  //临时文件
  File minioFile = null;
  FileOutputStream outputStream = null;
  try{
   InputStream stream = minioClient.getObject(GetObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .build());
   //创建临时文件
   minioFile=File.createTempFile("minio", ".merge");
   outputStream = new FileOutputStream(minioFile);
   IOUtils.copy(stream,outputStream);
   return minioFile;
  } catch (Exception e) {
   e.printStackTrace();
  }finally {
   if(outputStream!=null){
    try {
     outputStream.close();
    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }
  return null;
 }

 /**
  * 得到合并后的文件的地址
  * @param fileMd5 文件id即md5值
  * @param fileExt 文件扩展名
  * @return
  */
 private String getFilePathByMd5(String fileMd5,String fileExt){
  return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
 }
}
