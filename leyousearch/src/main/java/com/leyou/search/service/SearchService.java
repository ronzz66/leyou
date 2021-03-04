package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.Repository.GoodsRepository;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.mockito.internal.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sun.security.jgss.GSSHeader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired //远程调用
    private CategoryClient categoryClient;

    @Autowired //远程调用
    private BrandClient brandClient;
    @Autowired //远程调用
    private GoodClient goodClient;
    @Autowired //远程调用
    private SpecificationClient specificationClient;
    //jackson工具
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private GoodsRepository goodsRepository;

    public SearchResult search(SearchRequest request) {
        if (StringUtils.isBlank(request.getKey())){
            return null;
        }
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件 在all字段里查询
        //QueryBuilder basicQuey = QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND);//and关系
        BoolQueryBuilder basicQuey = buidBoolQueryBuilder(request);//组合过滤查询

        queryBuilder.withQuery(basicQuey);//添加条件
        //分页条件 页码从0开始，每20条
        queryBuilder.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id","skus","subTitle"},null));


        //聚合 分类和品牌
         String categoryAggName = "categories";//分类聚合名称
        String brandAggName="brands";//品牌聚合名称
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));//根据cid3聚合分类
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));//根据品牌聚合



        //执行查询获取结果查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)goodsRepository.search(queryBuilder.build());

        //获取聚合结果集并解析  分类和品牌
        List<Map<String,Object>> categories = getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
        List<Brand> brands = getBrandAggResult(goodsPage.getAggregation(brandAggName));


        //判断是否只有一个分类 只有一个分类才做规格参数的聚合 //不这样的数据规格参数会很多
        List<Map<String,Object>>  specs = null;
        if (!CollectionUtils.isEmpty(categories) && categories.size()==1){
            //聚合规格参数
            specs = getParmAggResult((Long)categories.get(0).get("id"),basicQuey);//获取分类id+和查询条件
        }

        return new SearchResult//总记录数，总页数，记录集合 ,聚合分类，聚合品牌集合
                (goodsPage.getTotalElements(),goodsPage.getTotalPages(),goodsPage.getContent(),categories,brands,specs);


    }

    //构建组合查询 ,布尔查询
    private BoolQueryBuilder buidBoolQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();//获取bool查询对象
        //添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //添加过滤条件
        Map<String, Object> filter = request.getFilter();//获取用户选中的过滤信息   (key:分类)(value:具体的参数))
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            //获取entry key值
            String key = entry.getKey();
            if (StringUtils.equals("品牌",key)){
                key="brandId";
            }else if (StringUtils.equals("分类",key)){
                key="cid3";
            }else {//只有品牌和分类是独立的 其他的在specs里面
                key="specs." +key+".keyword";
            }
            //词条过滤
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));//添加所有的过滤条件:
                                                                            // 格式"specs.CPU核数.keyword": "四核"
        }


        return  boolQueryBuilder;

    }

    //根据分类id+查询条件  聚合规格参数
    private List<Map<String,Object>> getParmAggResult(Long cid, QueryBuilder basicQuey) {
        //自定义查询对象构建
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件
        queryBuilder.withQuery(basicQuey);

        //查询要聚合的规格参数  查询规格参数: 根据分类id 和 是搜索字段 结果就是聚合参数
        List<SpecParam> params = this.specificationClient.queryParams(null, cid, null, true);
        //添加规格参数聚合
        params.forEach(param ->{                                                       //specs字段下的规格参数  .keyword为不分词
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));
        });
        //结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //执行聚合查询 获取聚合结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        List<Map<String,Object>> specs = new ArrayList<>();//返回结果集
        //解析聚合结果集  key-聚合名称,value-聚合对象
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();//获取结果集转换为map 类型默认就是 key集合名value桶集合
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            Map<String,Object> map= new HashMap<>(); //需要{k:规格参数名 options：参数值}类型 k:规格参数名(品牌,电池,核数) options:聚合的规格参数值集合
            map.put("k",entry.getKey()); //entry.getKey()就是k:规格参数名(品牌,电池,核数)
            //初始化一个options集合搜集桶中的key
            List<String> options = new ArrayList<>();
            //获取聚合
            StringTerms terms = (StringTerms)entry.getValue();//获取一个聚合对象
            //获取聚合里面的桶集合
            terms.getBuckets().forEach(bucket -> {//遍历桶集合 获取规格参数加入 规格参数值集合
                //搜集桶中的key
                options.add(bucket.getKeyAsString());//桶的key就是类型四核,八核这样的
            });
            map.put("options",options);//把规格参数值集合集合加入map
            specs.add(map); //最后把map加入返回结果集 一个聚合解析添加list完成
        }
        return specs;

    }


    //解析分类的聚合结果集
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms longTerms = (LongTerms) aggregation;//*分类桶 分类的映射为是分类的id

        //获取桶集合 转换为list<map>
       return longTerms.getBuckets().stream().map(bucket -> {
            Map<String, Object> map = new HashMap<>();
            Long id = bucket.getKeyAsNumber().longValue();//*分类桶 分类的映射为分类的id
            List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(id));//根据分类id集合查询 分类name集合

            map.put("id",id);
            map.put("name",names.get(0));//集合里只有一个数据 就拿第一个
            return  map;

        }).collect(Collectors.toList());//转换为集合
    }


    //解析品牌的聚合结果集
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms longTerms = (LongTerms) aggregation;//强转 品牌桶为数字类型

        //获取品牌桶集合     *品牌桶 品牌的映射为是品牌的id
        return longTerms.getBuckets().stream().map(bucket -> {
            long l = bucket.getKeyAsNumber().longValue();//获取每个桶对应的id
            return this.brandClient.queryBrandById(l);//查询品牌
        }).collect(Collectors.toList());//返回一个新的集合

    }


    //构建buidgoods方法
    public Goods buidGoods(Spu spu) throws IOException {
        Goods goods= new Goods();

        //根据分类id查询分类名称
        List<String> names = categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //根据品牌id查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());

        //查询所有sku
        List<Sku> skus = goodClient.querySkusBySpuId(spu.getId());
        //初始化价格集合,添加所有sku的价格
        List<Long> prices = new ArrayList<Long>();
        //搜集sku需要的字段信息
        List<Map<String,Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());//添加所有的sku价格
            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            //获取sku图片,分割图片字符串获取第一个
            map.put("image",StringUtils.isBlank
                    (sku.getImages())?"":StringUtils.split(sku.getImages(),",")[0]);

            skuMapList.add(map);
        });

        //根据spu中的cid3 和成为搜索字段的 查询所有搜索规格参数
        List<SpecParam> params = specificationClient.queryParams(null, spu.getCid3(), null, true);
        //1.先根据spuid查询spudetail
        SpuDetail spuDetail = goodClient.qeurySpuDetaillBySpuId(spu.getId());
        //2.反序列化spudetail的generic通用规格参数
        Map<String,Object> genericSpecMap =  MAPPER.readValue(spuDetail.getGenericSpec(),new TypeReference<Map<String,Object>>(){});
        //3.反序列化特殊spudetail的SpecialSpec特殊规格参数
        Map<String,List<Object>> specialSpecMap =  MAPPER.readValue(spuDetail.getSpecialSpec(),new TypeReference<Map<String,List<Object>>>(){});
        //4.保存到goods里面
        Map<String,Object> specs= new HashMap<>();

        params.forEach(param -> {
            if (param.getGeneric()){//判断是否特殊规格参数
                //yes
                String value = genericSpecMap.get(param.getId().toString()).toString();
                if (param.getNumeric()){//判断是否 数字类型:是数字类型又是搜索字段则为返回区间
                    value = chooseSegment(value, param);//
                }
                specs.put(param.getName(),value);
            }else {
                //no
                List<Object> value = specialSpecMap.get(param.getId().toString());
                specs.put(param.getName(),value);//特殊字段,里面都是sku具体的规格,不能为区间
            }
        });


        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());

        goods.setBrandId(spu.getBrandId());

        goods.setCreateTime(spu.getCreateTime());

        goods.setSubTitle(spu.getSubTitle());
        //拼接All字段 需要spu标题，分类以及品牌名称,
        goods.setAll(spu.getTitle()+" "+ StringUtils.join(names," ")+" "+brand.getName());

        //获取spu下的所有sku价格
        goods.setPrice(prices);
        //获取spu下所有sku的json字符串
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        //获取所有查询规格参数{name:value}
        goods.setSpecs(specs);
        return goods;
    }

    //判断区间方法
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    //同步更新添加elasticSearch
    public void save(Long spud) throws IOException {

        Spu spu = goodClient.querySpuById(spud);//先查询出spu
        Goods goods = buidGoods(spu);//构建出goods
        goodsRepository.save(goods);//保存到elasticSearch
    }
    //同步删除elasticSearch
    public void delete(Long id) {
        goodsRepository.deleteById(id);
    }
}
