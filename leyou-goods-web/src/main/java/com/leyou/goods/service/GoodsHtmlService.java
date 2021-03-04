package com.leyou.goods.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;

@Service
public class GoodsHtmlService {

    //模板引擎 生成静态页面
    @Autowired
    private TemplateEngine engine;

    @Autowired
    private  GoodsService goodsService;
    public void  creareEngine(Long spuId){
        //初始化运行上下文
        Context context = new Context();
        //设置数据模板
        context.setVariables(this.goodsService.loadData(spuId));


       //生成静态页面 到本地
        PrintWriter printWriter = null;
        try {
            //创建一个file
            File file = new File("C:\\SpringBoot\\tool\\nginx-1.14.0\\nginx-1.14.0\\html\\item\\"+spuId+".html");
            //输出流
            printWriter = new PrintWriter(file);
            this.engine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null){
                printWriter.close();
            }
        }
    }

    //删除静态页面
    public void deleteEngine(Long id) {
        File file = new File("C:\\SpringBoot\\tool\\nginx-1.14.0\\nginx-1.14.0\\html\\item\\"+id+".html");
        file.deleteOnExit();
    }
}
