package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;
//SelectByIdListMapper<实体类，主键>
public interface CategoryMapper extends Mapper<Category>,SelectByIdListMapper<Category,Long>{
}
