package com.xyx.spring.Service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.xyx.spring.Model.Files;

public interface FileService {
	boolean deleteFileByPath(String filePathAndName,HttpServletRequest request);
	Files getById(Long id);
	boolean deleteFileById(Long id);
	boolean addFile(Files file);
	Files uploadFile(String filePath, MultipartFile file, Integer fileType,HttpServletRequest request);
}
