package com.technology.utils;


import com.alibaba.fastjson.util.IOUtils;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件工具类
 */
public class FileUtil {

    /**
     * 读取文本文件内容
     * @param filePath 文件路径
     * @return 文本文件内容
     */
    public static String readFileContent(String filePath) {
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        List<String> lineList = Lists.newArrayList();
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8.name());
            reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                lineList.add(line);
            }
            return lineList.stream().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException("Read file content failure, fileName=" + filePath, e);
        } finally {
            IOUtils.close(reader);
            IOUtils.close(inputStreamReader);
        }
    }

    /**
     * 下载文件
     * @param response 响应
     * @param inputStream 输入流
     * @param fileName 文件名称
     */
    public static void downLoadFile(HttpServletResponse response, InputStream inputStream, String fileName) {
        OutputStream outputStream = null;
        try {
            // 设置文件类型
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            response.setContentLength(inputStream.available());
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, i);
                outputStream.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("Download file failure, fileName=" + fileName, e);
        } finally {
            IOUtils.close(outputStream);
            IOUtils.close(inputStream);
        }
    }

}
