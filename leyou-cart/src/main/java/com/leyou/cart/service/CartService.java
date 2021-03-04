package com.leyou.cart.service;

import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static  final  String KEY_PREFIX ="user:cart:";
    @Autowired
    private GoodsClient goodsClient;
    //添加商品到redis
    public void addCart(Cart cart) {
        //获取当前登录的用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //1.查询redis购物车记录  r-edis中的格式为 (用户id:{商品id:商品信息})
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        //获取redis中购物车的key
        String key = cart.getSkuId().toString();
        Integer oldNum =  cart.getNum();
        //2.判断当前商品是否存在
        if (hashOps.hasKey(key)){
            //3.存在 就加数量
            String cartJson = hashOps.get(key).toString();//redis中的cart为json类型
            cart = JsonUtils.parse(cartJson, Cart.class);//序列化为cart对象
            cart.setNum(cart.getNum()+oldNum);//更新数量

        }else {
            //4.不在就新增购物车
            //添加到redis
            Sku sku = goodsClient.queryBySkuId(cart.getSkuId());
            //传递过来的cart只有skuid 和 数量
            cart.setUserId(userInfo.getId());//设置用户id
            cart.setTitle(sku.getTitle());//设置标题
            cart.setPrice(sku.getPrice());//设置价钱
            cart.setOwnSpec(sku.getOwnSpec());//设置独有规格
            cart.setImage(StringUtils.isBlank(sku.getImages())?"":StringUtils.split(sku.getImages(),",")[0]);//设置图片
        }

        hashOps.put(key,JsonUtils.serialize(cart));//添加到redis


    }

    //查询购物车
    public List<Cart> queryCarts() {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (!redisTemplate.hasKey(KEY_PREFIX + userInfo.getId())){
            //判断用户是否有购物车记录
            return  null;
        }
        //获从redis取当前登录用户的购物车所有商品
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        List<Object> cartsJson = hashOps.values();//获取所有的Cart 商品信息和数量
        if (CollectionUtils.isEmpty(cartsJson)){
            //如果购物车集合为空也返回null
            return null;
        }
        return  cartsJson.stream().map(cartJson->JsonUtils.parse(cartJson.toString(),Cart.class)//反序列化为cart对象
        ).collect(Collectors.toList());//返回集合

    }

    //更新购物车商品数量方法
    public void updateNum(Cart cart) {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (!redisTemplate.hasKey(KEY_PREFIX + userInfo.getId())){
            //判断用户是否有购物车记录
            return ;
        }
        Integer num = cart.getNum();//获取修改成多少数量
        //获从redis取当前登录用户的购物车所有商品
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();//获取要修改的商品 key为skuid
        cart = JsonUtils.parse(cartJson, Cart.class);//反序列化为对象
        cart.setNum(num);//修改数量
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));//存redis时 序列化为json

    }
}
