package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface ISpecificationService {
    //查询规格组
    List<SpecGroup> queryGroupsByCid(Long cid);

    //根据gid查询规格参数
    List<SpecParam> queryParams(Long gid,Long cid,Boolean generic,Boolean searching);
    //查询规格组和规格
    List<SpecGroup> queryParamsWithParam(Long cid);
}
