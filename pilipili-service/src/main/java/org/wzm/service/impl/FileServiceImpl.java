package org.wzm.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.wzm.domain.File;
import org.wzm.mapper.FileMapper;
import org.wzm.service.FileService;
import org.wzm.service.FastDFSService;
import org.wzm.utils.MD5Util;

import java.io.IOException;
import java.util.Objects;

public class FileServiceImpl implements FileService {

    @Autowired
    private FastDFSService fastDFSService;

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String uploadFileBySlices(MultipartFile slice, String fileMD5, Integer sliceNumber, Integer totalSliceNumber)
            throws IOException {
        File dbFile = fileMapper.getByMD5(fileMD5);
        if (Objects.nonNull(dbFile)) {
            return dbFile.getUrl();
        }

        String filePath = fastDFSService.uploadFileBySlices(slice, fileMD5, sliceNumber, totalSliceNumber);
        if (StringUtils.isNotBlank(filePath)) {
            File file = new File();
            file.setUrl(filePath);
            file.setMd5(fileMD5);
            file.setType(fastDFSService.getFileType(slice));
            fileMapper.save(file);
        }
        return filePath;
    }

    @Override
    public void deleteFile(String filePath) {
        fileMapper.removeByUrl(filePath);
        fastDFSService.deleteFile(filePath);
    }

    @Override
    public String getFileMD5(MultipartFile file) throws IOException {
        return MD5Util.getFileMD5(file);
    }
}
