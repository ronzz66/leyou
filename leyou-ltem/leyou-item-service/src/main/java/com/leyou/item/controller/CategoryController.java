package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 根据父节点id查询子节点
     * @param pid
     * @return
     */
    @GetMapping("list")//ResponseEntity Restful写法
    public ResponseEntity<List<Category>> queryCategorysByid
            (@RequestParam(value = "pid",defaultValue = "0")Long pid){
        try {
            if(pid == null || pid <0){//参数不合法:400
                //1.return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                //2.return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                //3.
                return ResponseEntity.badRequest().build();
            }

            List<Category> categories = this.categoryService.queryCategoriesByPid(pid);
            if (CollectionUtils.isEmpty(categories)){//404,未找到
                //return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                return ResponseEntity.notFound().build();
            }
            //200查询成功
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //500 服务器内部异常
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //根据分组ids集合查询分组
    @GetMapping
    public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids")List<Long> ids){

        List<String> categories = categoryService.queryNameByids(ids);

        if (CollectionUtils.isEmpty(categories)){//404,未找到
            return ResponseEntity.notFound().build();
        }
        //200查询成功
        return ResponseEntity.ok(categories);
    }
}
