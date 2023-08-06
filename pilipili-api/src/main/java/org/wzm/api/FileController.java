package org.wzm.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;
import org.wzm.model.response.JsonResponse;

import java.io.IOException;

public interface FileController {

    //上传文件+秒传
    @PutMapping("/file-slices")
    JsonResponse<String> uploadFileBySlices(MultipartFile slice, String fileMD5, Integer sliceNumber,
                                            Integer totalSliceNumber) throws IOException;

    //删除文件
    @DeleteMapping("/file-delete")
    JsonResponse<String> deleteFile(String filePath);

    @PostMapping("/md5files")
    JsonResponse<String> getFileMD5(MultipartFile file) throws IOException;
}
