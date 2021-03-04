package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.leyou.item.service.IGoodsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodServiceImpl implements IGoodsService {

    @Autowired
    private SpuMapper spuMapperl;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired//查询品牌
    private BrandMapper brandMapper;
    @Autowired//查询分类名称
    private CategoryServiceImpl categoryService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        //条件查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isBlank(key)){//如果查询条件不为空，就模糊查询
            criteria.andLike("title","%"+key+"%");
        }

        //添加上，下架的过滤条件
        if(saleable!=null){//如果上，下架的过滤条件为空
            criteria.andEqualTo("saleable",saleable);
        }
        //分页
        PageHelper.startPage(page,rows);
        //执行查询
        List<Spu> spus = spuMapperl.selectByExample(example);
        PageInfo info = new PageInfo(spus);
        //转换成spubo集合
        List<SpuBo> spuBos = spus.stream().map(spu -> {//集合每个元素处理，返回一个新的集合
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu, spuBo); //复制spu属性到spubo
            //查询品牌名称
            Brand brand = brandMapper.selectByPrimaryKey(spuBo.getBrandId());
            spuBo.setBname(brand.getName());
            //查询分类名称
            List<String> names = categoryService.queryNameByids(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "-"));
            return spuBo;//返回每个处理后元素
        }).collect(Collectors.toList());//指定返回新的集合类型

        //返回PageResult

        return new PageResult<>(info.getTotal(),spuBos);
    }

    //添加goods
    @Override
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //1.spu     实体类：继承spu，包装了：SpuDetail，sku集合，
        spuBo.setId(null);
        spuBo.setSaleable(true);//上架
        spuBo.setCreateTime(new Date());//创建时间
        spuBo.setLastUpdateTime(spuBo.getCreateTime());//修改时间
        spuMapperl.insertSelective(spuBo);//spuBo本质上就是spu
        //2.spudetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId()); //设置spuDetail表对应spuid
        spuDetailMapper.insertSelective(spuDetail);
        //3.sku和库存
        saveSkuAndStock(spuBo);


        sendMsg("insert",spuBo.getId());
    }


    //amqpTemplate发送消息到交换器  同步修改
    private void sendMsg(String type,Long id) {
        try {
            amqpTemplate.convertAndSend("item"+type,id);//配置文件设置了默认交换机发送通配符,和消息msg
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    //根据spuid查询spuDetail  //更新商品规格参数页面
    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return spuDetailMapper.selectByPrimaryKey(spuId);
    }

    //根据spuid查询skus集合 //更新商品sku属性页面
    @Override
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(record);
       return skus.stream().map(sku -> {
            Integer stock = stockMapper.selectByPrimaryKey(sku.getId()).getStock();
            sku.setStock(stock);
            return sku;
        }).collect(Collectors.toList());



    }

    //更新商品:执行
    @Override
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //先删除,再添加
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = skuMapper.select(record);

        //1.先删除stock
        skus.forEach(sku -> {
            stockMapper.deleteByPrimaryKey(sku.getId());
        });
        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        skuMapper.delete(sku);
        //添加sku和库存
        saveSkuAndStock(spuBo);

        //更新spudetaill,和spu
        spuBo.setCreateTime(null);//创建时间不能更新
        spuBo.setLastUpdateTime(new Date());//修改时间也不能更新
        spuBo.setValid(null);//是否有效
        spuBo.setSaleable(null);//上下架也不能随意更新
        spuMapperl.updateByPrimaryKeySelective(spuBo);
        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMsg("update",spuBo.getId());
    }

    //根据spuid 查询spu
    @Override
    public Spu querySpuById(Long id) {
        return spuMapperl.selectByPrimaryKey(id);
    }

    @Override
    public Sku querySkuById(Long skuId) {
        return  skuMapper.selectByPrimaryKey(skuId);

    }

    //添加sku和库存方法
    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku -> {
            sku.setId(null);
            sku.setSpuId(spuBo.getId()); //设置sku表 对应spuid
            sku.setCreateTime(new Date());//创建时间
            sku.setLastUpdateTime(sku.getCreateTime());//最后修改时间
            skuMapper.insertSelective(sku);
            //4.添加stock库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());//设置库存表的 skuid
            stock.setStock(sku.getStock());//设置库存， 传在sku里面
            stockMapper.insert(stock);
        });
    }
}
