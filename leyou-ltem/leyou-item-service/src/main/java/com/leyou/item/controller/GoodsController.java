package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsController {


    @Autowired
    private IGoodsService goodsService;

    /**
     * 根据条件分页查询spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>>querySpoByPage
            (@RequestParam(value = "key",required = false)String key,
             @RequestParam(value = "saleable",required = false)Boolean saleable,//上架，下架，全部
             @RequestParam(value = "page",defaultValue = "1")Integer page,
             @RequestParam(value = "rows",defaultValue = "5")Integer rows
             ){

        PageResult<SpuBo> pageResult= goodsService.querySpuByPage(key,saleable,page,rows);


        if (CollectionUtils.isEmpty(pageResult.getItems())){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(pageResult);
    }



    //添加商品
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){

        goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    //根据spuid查询spuDetail
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> qeurySpuDetaillBySpuId(
            @PathVariable("spuId")Long spuId){

        SpuDetail spuDetail=goodsService.querySpuDetailBySpuId(spuId);
        if (spuDetail==null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(spuDetail);
    }


    //根据spuid查询skus集合
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id")Long spuId ){

        List<Sku> skus=goodsService.querySkusBySpuId(spuId);
        if (CollectionUtils.isEmpty(skus)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(skus);
    }

    //更新商品的信息
    @PutMapping("goods")
    public ResponseEntity<Void> upDateGoods(@RequestBody SpuBo spuBo){

        goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();//204

    }

    //根据id查询spu
    @GetMapping("{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){

        Spu spu = goodsService.querySpuById(id);

        if (spu==null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(spu);
    }


    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> queryBySkuId(@PathVariable("skuId")Long skuId){
        Sku sku = goodsService.querySkuById(skuId);

        if (sku==null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(sku);
    }

}
