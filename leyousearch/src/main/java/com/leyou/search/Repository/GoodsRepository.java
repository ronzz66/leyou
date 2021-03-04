package com.leyou.search.Repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

//GoodsRepository  Long为主键类型
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
