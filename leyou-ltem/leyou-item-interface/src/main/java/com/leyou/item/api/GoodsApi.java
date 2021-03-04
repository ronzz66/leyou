package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {

    //根据spuid查询spuDetail
    @GetMapping("spu/detail/{spuId}")
    public SpuDetail qeurySpuDetaillBySpuId(@PathVariable("spuId")Long spuId);

    /**
     * 根据gid查询规格参数
     * @param
     * @return
     */
    @GetMapping("spu/page")
    public PageResult<SpuBo> querySpoByPage
            (@RequestParam(value = "key",required = false)String key,
             @RequestParam(value = "saleable",required = false)Boolean saleable,//上架，下架，全部
             @RequestParam(value = "page",defaultValue = "1")Integer page,
             @RequestParam(value = "rows",defaultValue = "5")Integer rows
            );

    //根据spuid查询skus集合
    @GetMapping("sku/list")
    public List<Sku> querySkusBySpuId(@RequestParam("id")Long spuId );

    //根据id查询spu
    @GetMapping("{id}")
    public Spu querySpuById(@PathVariable("id")Long id);


    @GetMapping("sku/{skuId}")
    public Sku queryBySkuId(@PathVariable("skuId")Long skuId);
}
