package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.UUID;

/**
 * 文件的上传和下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件的上传
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        log.info("文件上传，文件名为: {}", file.toString());
        // 原始文件名
        String originFileName = file.getOriginalFilename();
        // 使用UUID重新生成文件名，防止文件名重复，造成后面上传的文件覆盖前面上传的文件
        String fileName = UUID.randomUUID().toString();
        // 截取文件后缀名
        String suffix = originFileName.substring(originFileName.lastIndexOf("."));
        // 拼接文件名
        fileName = fileName + suffix;

        // 创建一个目录对象 dir
        File dir = new File(basePath);
        // 如果目录不存在，创建目录
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            // 将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName));
        } catch(Exception e) {
            e.printStackTrace();
        }
        // 返回文件名
        return R.success(fileName);
    }


    /**
     * 文件的下载
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        log.info("文件下载，文件名为: {}", name);

        try {
            // 输入流，读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            // 输出流，将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len); // 输出流写到浏览器中
                outputStream.flush(); // 刷新缓冲区
            }
            // 关闭流
            fileInputStream.close();
            outputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
