package org.wzm.service.impl;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.wzm.domain.BizException;
import org.wzm.service.FastDFSService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class FastDFSServiceImpl implements FastDFSService {
    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    private static final String DEFAULT_GROUP       = "group1";
    public static final  String PATH_KEY            = "path-key:";
    public static final  String UPLOADED_SIZE_KEY   = "uploaded-size-key:";
    public static final  String UPLOADED_NUMBER_KEY = "uploaded-number-key:";
    public static final  int    SLICE_SIZE          = 1024 * 1024 * 2;

    @Override
    public void deleteFile(String filePath) {
        fastFileStorageClient.deleteFile(DEFAULT_GROUP+"/"+filePath);
    }

    @Override
    public String uploadFileBySlices(MultipartFile file, String fileMD5, Integer sliceNumber, Integer totalSliceNumber)
            throws IOException {
        if (Objects.isNull(file) || Objects.isNull(sliceNumber) || Objects.isNull(totalSliceNumber)) {
            throw new BizException("Illegal parameters!");
        }
        String pathKey = PATH_KEY + fileMD5;
        String upSizeKey = UPLOADED_SIZE_KEY + fileMD5;
        String upNumberKey = UPLOADED_NUMBER_KEY + fileMD5;

        String upSizeVal = stringRedisTemplate.opsForValue().get(upSizeKey);
        Long upSize = 0L;
        if (StringUtils.isNotBlank(upSizeVal)) {
            upSize = Long.valueOf(upSizeVal);
        }
        String fileType = getFileType(file);
        if (Integer.valueOf(1).equals(sliceNumber)) {
            String path = uploadAppenderFile(file);
            if (StringUtils.isBlank(path)) {
                throw new BizException("Upload failed!");
            }
            stringRedisTemplate.opsForValue().set(pathKey, path);
            stringRedisTemplate.opsForValue().increment(upNumberKey, 1);
        } else {
            String filePath = stringRedisTemplate.opsForValue().get(pathKey);
            if (StringUtils.isBlank(filePath)) {
                throw new BizException("Upload failed!");
            }
            modifyAppenderFile(file, filePath, upSize);
            stringRedisTemplate.opsForValue().increment(upNumberKey, 1);
        }
        upSize += file.getSize();
        stringRedisTemplate.opsForValue().set(upSizeKey, String.valueOf(upSize));

        Integer upNumber = Integer.valueOf(stringRedisTemplate.opsForValue().get(upNumberKey));
        String url = null;
        if (upNumber.equals(totalSliceNumber)) {
            url = stringRedisTemplate.opsForValue().get(pathKey);
            List<String> keyList = Arrays.asList(upNumberKey, pathKey, upSizeKey);
            stringRedisTemplate.delete(keyList);
        }
        return url;
    }

    private void modifyAppenderFile(MultipartFile file, String filePath, Long upSize) throws IOException {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), upSize);
    }

    private String uploadAppenderFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String fileType = getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(),
                                                                         file.getSize(), fileType);
        return storePath.getPath();
    }

    @Override
    public String getFileType(MultipartFile file) {
        if (Objects.isNull(file)) {
            throw new BizException("illegal file!");
        }
        String filename = file.getOriginalFilename();
        int idx = filename.lastIndexOf(".");
        return filename.substring(idx + 1);
    }

    @Override
    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) {
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, url);
        long fileSize = fileInfo.getFileSize();
        String path = httpFdfsStorageAddr + url;
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        while(headerNames.hasMoreElements()) {
            String hdr = headerNames.nextElement();
            headers.put(hdr, request.getHeader(hdr));
        }
    }
}
