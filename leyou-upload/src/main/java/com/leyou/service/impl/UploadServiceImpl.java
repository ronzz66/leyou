package com.leyou.service.impl;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.service.IUploadService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UploadServiceImpl implements IUploadService {
    //可以上传的图片文件类型
    private static  final List<String> CONTENT_TYPE = Arrays.asList("image/gif","image/jpeg");
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Autowired
    private FastFileStorageClient fastFileStorageClientl;
    @Override
    public String uploadImage(MultipartFile file) {
            //校验文件类型
            String originalFilename = file.getOriginalFilename();//获取原始文件名
           /* String[] split = originalFilename.split(".");//1.
            String xx = split[split.length-1];*/
            String contentType = file.getContentType();//获取文件类型
            if (!CONTENT_TYPE.contains(contentType)){//如果不包含在白名单
                LOGGER.info("文件类型不合法: {}",originalFilename);
                return null;
            }
        try {
            //校验文件内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());//判断是不是图片
            if (bufferedImage==null ){
                LOGGER.info("文件内容不合法:{}",originalFilename);
                return null;
            }
            //保存到本地文件服务器
            //file.transferTo(new File("C:\\SpringBoot\\image\\"+originalFilename));
            //返回url
            //return "http://image.leyou.com/"+originalFilename;

            //获取文件后缀名
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            // 上传到fastDFS并且生成缩略图
            StorePath storePath = fastFileStorageClientl.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            return "http://image.leyou.com/"+storePath.getFullPath();//带分组的fastdfs位置

        } catch (IOException e) {
            LOGGER.info("服务器内部错误:{}",originalFilename);
            e.printStackTrace();
        }

        return null;
    }
}
