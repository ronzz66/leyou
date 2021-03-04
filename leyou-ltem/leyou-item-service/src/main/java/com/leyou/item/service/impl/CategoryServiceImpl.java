package com.leyou.item.service.impl;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements ICategoryService {



    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父节点id查询子节点
     * @param pid
     * @return
     */
    @Override
    public List<Category> queryCategoriesByPid(Long pid) {
        Category record = new Category();
        record.setParentId(pid);

        return categoryMapper.select(record);
    }

    //根据类别id集合，查询类别名称
    @Override
    public List<String> queryNameByids(List<Long> ids){
        List<Category> categories = categoryMapper.selectByIdList(ids);
        return categories.stream().map(category ->
                category.getName()).collect(Collectors.toList());


    }
}
