package com.leyou.goods.controller;


import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class GoodsController {//nginx配置了如果本地没有就经过这里

    @Autowired
    private GoodsService goodsService;

    @Autowired
    GoodsHtmlService  goodsHtmlService;
    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long id, Model model){


        Map<String, Object> map = goodsService.loadData(id);
        model.addAllAttributes(map);



        goodsHtmlService.creareEngine(id);//生成静态页面
        return "item";
    }
}
