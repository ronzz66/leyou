package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.IBrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements IBrandService {



    @Autowired
    private BrandMapper brandMapper;

    /**
     * 根据查询条件分页,并排序查询
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @Override
    public PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //初始化example对象  模糊查询
        Example example = new Example(Brand.class); //模板类
        Example.Criteria criteria = example.createCriteria();//查询条件
        //根据name 或者 首字母 模糊查询
        if (!StringUtils.isBlank(key)){
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }
        //添加分页条件
        PageHelper.startPage(page,rows);
        //添加排序条件
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+" "+(desc?"desc":"asc"));
        }
        List<Brand> brands = brandMapper.selectByExample(example);//通用mapper模糊查询

        //包装成pageInfo对象
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //包装成分页结果集
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());

    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Transactional
    @Override
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增brand
        Boolean flag = brandMapper.insertSelective(brand)==1;


        //然后中间表
        cids.forEach(cid ->{
            brandMapper.insertCategoryAndBrand(cid,brand.getId());
        });
    }

    /**
     * 根据分类 查询品牌list
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandsByCid(Long cid) {
        return brandMapper.selectBrandsBycid(cid);
    }

    @Override
    public Brand queryBrandById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }


}
