package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.IBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("brand")    //key=&page=1&rows=5&sortBy=id&desc=false
public class BrandController {

    @Autowired
    private IBrandService brandService;

    /**
     * 根据查询条件分页,并排序查询
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    //品牌查询
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>>  queryBrandByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "desc",required = false)Boolean desc
    ){
        PageResult<Brand> pageResult= brandService.queryBrandByPage(key,page,rows,sortBy,desc);
        if (CollectionUtils.isEmpty(pageResult.getItems())){
            return ResponseEntity.notFound().build();
        }

        return  ResponseEntity.ok(pageResult);

    }


    //新增品牌操作, 一个品牌对应多个类别 cids为类别id, 一对多的关系
    @PostMapping
    public ResponseEntity<Void> saveBrand
    (Brand brand, @RequestParam("cids")List<Long> cids){
        brandService.saveBrand(brand,cids);
        return  ResponseEntity.status(HttpStatus.CREATED).build();

    }


    //根据分类id查询品牌列表
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandsByCid(@PathVariable("cid")Long cid){
        List<Brand> brands=brandService.queryBrandsByCid(cid);
        if (CollectionUtils.isEmpty(brands)){
            return ResponseEntity.notFound().build();
        }
        return  ResponseEntity.ok(brands);
    }

    //根据id查询品牌名称
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id")Long id){
        Brand brand= brandService.queryBrandById(id);
        if (brand == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(brand);

    }














}
