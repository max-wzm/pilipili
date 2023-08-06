package org.wzm.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wangzhiming
 */
public interface FastDFSService {
    void deleteFile(String filePath) ;

    String uploadFileBySlices(MultipartFile file, String fileMD5, Integer sliceNumber, Integer totalSliceNumber)
            throws IOException;
    String getFileType(MultipartFile file);

    void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url);
}
