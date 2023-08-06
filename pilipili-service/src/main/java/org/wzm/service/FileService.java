package org.wzm.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    String uploadFileBySlices(MultipartFile slice, String fileMD5, Integer sliceNumber, Integer totalSliceNumber) throws
                                                                                                                  IOException;

    void deleteFile(String filePath);

    String getFileMD5(MultipartFile file) throws IOException;
}
