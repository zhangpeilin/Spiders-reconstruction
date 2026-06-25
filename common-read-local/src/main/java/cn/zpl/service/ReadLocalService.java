package cn.zpl.service;

import cn.zpl.common.bean.Ehentai;
import cn.zpl.common.bean.QueryDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReadLocalService {
    
    String convertToTraditionalChinese(String simplifiedChinese);
    
    Ehentai getEh(String id);
    
    void invalidCache(String comicId);
    
    List<Ehentai> queryBySql(String sql);
    
    List<Ehentai> searchComics(QueryDTO queryDTO);
    
    List<Ehentai> searchComicsWithCustomPath(QueryDTO queryDTO, String customPath);

    /**
     * 获取所有已扫描漫画的ID列表
     */
    List<String> getAllComicIds();
}
