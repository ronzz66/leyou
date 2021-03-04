package com.leyou.elasticsearch.test;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.Repository.GoodsRepository;
import com.leyou.search.client.GoodClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearcgTest {

    @Autowired//用来创建索引和映射
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodClient goodClient;
    @Test
    public void test(){//创建索引和映射
        elasticsearchTemplate.createIndex(Goods.class);//创建索引
        elasticsearchTemplate.putMapping(Goods.class);//创建映射

        Integer page = 1;
        Integer size = 100;
        do{
            //按页查询添加到elasticsearch
            PageResult<SpuBo> result = this.goodClient.querySpoByPage(null, null, page, size);
            List<SpuBo> items = result.getItems();//获取所有的spu数据
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buidGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());//返回一个新的数组


            //执行新增数据
            goodsRepository.saveAll(goodsList);
            page++;
            size=items.size();
        }while (size == 100);

    }

}
