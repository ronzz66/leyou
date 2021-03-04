package com.leyou.item.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@RequestMapping("category")
public interface CategoryApi {



    //根据分组ids集合查询分组
    @GetMapping
    public List<String> queryNameByIds(@RequestParam("ids")List<Long> ids);
}
