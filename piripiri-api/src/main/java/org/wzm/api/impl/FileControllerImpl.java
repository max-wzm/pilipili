package org.wzm.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.wzm.api.FileController;
import org.wzm.model.response.JsonResponse;
import org.wzm.service.FileService;

import java.io.IOException;

@RestController
public class FileControllerImpl implements FileController {
    @Autowired
    private FileService fileService;

    //上传文件+秒传
    @PutMapping("/file-slices")
    @Override
    public JsonResponse<String> uploadFileBySlices(MultipartFile slice, String fileMD5, Integer sliceNumber,
                                                   Integer totalSliceNumber) throws IOException {
        String filePath = fileService.uploadFileBySlices(slice, fileMD5, sliceNumber, totalSliceNumber);
        return new JsonResponse<>(filePath);
    }

    //删除文件
    @DeleteMapping("/file-delete")
    @Override
    public JsonResponse<String> deleteFile(String filePath) {
        fileService.deleteFile(filePath);
        return new JsonResponse<>("删除成功！");
    }

    @PostMapping("/md5files")
    @Override
    public JsonResponse<String> getFileMD5(MultipartFile file) throws IOException {
        String fileMD5 = fileService.getFileMD5(file);
        return new JsonResponse<>(fileMD5);
    }
}
