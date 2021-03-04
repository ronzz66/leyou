package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.*;



@RequestMapping("brand")    //key=&page=1&rows=5&sortBy=id&desc=false
public interface BranApi {

    //根据id查询品牌
    @GetMapping("{id}")
    public Brand queryBrandById(@PathVariable("id")Long id);














}
