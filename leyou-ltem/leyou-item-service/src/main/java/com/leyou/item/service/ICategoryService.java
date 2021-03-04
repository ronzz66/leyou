package com.leyou.item.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface ICategoryService {

    List<Category> queryCategoriesByPid(Long pid);
     List<String> queryNameByids(List<Long> ids);
}
