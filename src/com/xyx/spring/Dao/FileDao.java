package com.xyx.spring.Dao;
import com.xyx.spring.Model.Files;
import com.xyx.spring.Utils.DataWrapper;
import java.util.List;
public interface FileDao {
	Files getById(Long id);
	DataWrapper<Files> getByName(String name);
	boolean deleteFiles(Long id);
	boolean addFiles(Files file);
	DataWrapper<List<Files>> getFilesList();
}
