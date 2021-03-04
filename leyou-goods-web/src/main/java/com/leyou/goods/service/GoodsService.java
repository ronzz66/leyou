package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {
    @Autowired
    private BrandClient brandClient;//品牌
    @Autowired
    private CategoryClient categoryClient;//分类
    @Autowired
    private GoodClient goodClient ;
    @Autowired
    private SpecificationClient specificationClient;//规格参数client


    public Map<String,Object> loadData(Long spuId){
        Map<String,Object> model= new HashMap();
        Spu spu = goodClient.querySpuById(spuId);//查询spu
        SpuDetail spuDetail = goodClient.qeurySpuDetaillBySpuId(spuId);//查询spuDetail

        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());//spu的cid集合
        List<String> names = categoryClient.queryNameByIds(cids);//根据spu的分类cid集合查询分类名
        //封装为List<Map<String,Object>> k为cid v为cid的name
        List<Map<String,Object>> categories = new ArrayList<>();
        for (int i = 0; i <cids.size() ; i++) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",cids.get(i));
            map.put("name",names.get(i));
            categories.add(map);
        }

        Brand brand = brandClient.queryBrandById(spu.getBrandId());//查询brand品牌

        List<Sku> skus = goodClient.querySkusBySpuId(spuId);//根据spuId查询spu集合
        //根据cid3查询规格参数组集合  (主体,基本信息,)
        List<SpecGroup> groups = specificationClient.queryGrupsWhithParam(spu.getCid3());

        //查询分类cid3查询对应规格参数集合(品牌,上市年份)
        List<SpecParam> params = specificationClient.queryParams(null, spu.getCid3(), false, null);
        //初始化特殊规格参数的map
        Map<Long,String> paramMap = new HashMap<>();
        params.forEach(param ->{
            paramMap.put(param.getId(),param.getName());
        });

        model.put("spu",spu);
        model.put("spuDetail",spuDetail);
        model.put("categories",categories);
        model.put("brand",brand);
        model.put("skus",skus);
        model.put("groups",groups);
        model.put("paramMap",paramMap);

        return model;
    }
}
