package org.wzm.mapper;

import org.wzm.domain.File;

public interface FileMapper {
    File getByMD5(String md5);

    int save(File file);

    int removeByUrl(String url);
}
