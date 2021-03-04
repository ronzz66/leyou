package com.leyou.item.service.impl;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParmMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.ISpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpectifictionServiceImpl implements ISpecificationService {


    //规格组
    @Autowired
    SpecGroupMapper specGroupMapper ;
    //参数
    @Autowired
    SpecParmMapper specParmMapper;

    /**
     * 查询参数组
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return specGroupMapper.select(record);
    }

    //根据条件查询规格参数组
    @Override
    public List<SpecParam> queryParams(Long gid,Long cid,Boolean generic,Boolean searching) {

        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        return specParmMapper.select(specParam);
    }


    //根据cid查询规格参数组(主体)和 规格参数组对应的规格参数(品牌,上市月份)
    @Override
    public List<SpecGroup> queryParamsWithParam(Long cid) {
        List<SpecGroup> specGroups = this.queryGroupsByCid(cid);

        specGroups.forEach(specGroup -> {//根据组id查询规格参数集合
            List<SpecParam> params = this.queryParams(specGroup.getId(), null, null, null);
            specGroup.setParams(params);
        });
        return specGroups;
    }
}
