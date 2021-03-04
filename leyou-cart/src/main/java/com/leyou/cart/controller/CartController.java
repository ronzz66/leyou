package com.leyou.cart.controller;


import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping//添加商品到购物车 redis
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        //添加商品到redis
        cartService.addCart(cart);


        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    //查询购物车
    @GetMapping
    public ResponseEntity<List<Cart>> quertCart(){
        List<Cart> carts = cartService.queryCarts();

        if (CollectionUtils.isEmpty(carts)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


        return ResponseEntity.ok(carts);
    }

    //修改商品
    @PutMapping  //putmapping 修改数据
    public ResponseEntity<Void> update(@RequestBody Cart cart){
        cartService.updateNum(cart);

        return ResponseEntity.noContent().build();
    }

}
